package com.spring_calendario.tiemporeal;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Bus de eventos en memoria: guarda las conexiones SSE abiertas por usuaria.
 *
 * <p>Estado de conexión: a diferencia de una petición HTTP normal (sin estado),
 * cada conexión SSE ocupa memoria y un descriptor de archivo mientras está
 * abierta (el problema C10K). Por eso hay un límite de conexiones por usuaria
 * y se limpian las conexiones cerradas.
 */
@Component
public class InMemoryEventBus implements EventBus {

    private static final Logger log = LoggerFactory.getLogger(InMemoryEventBus.class);

    /** Máximo de pestañas/dispositivos conectados por usuaria. */
    static final int MAX_CONNECTIONS_PER_USER = 5;

    /** 30 min; la app se vuelve a conectar sola cuando se cierra. */
    static final long EMITTER_TIMEOUT_MS = 30 * 60 * 1000L;

    private final Map<UUID, List<SseEmitter>> connections = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        List<SseEmitter> list = connections.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>());
        while (list.size() >= MAX_CONNECTIONS_PER_USER) {
            SseEmitter oldest = list.remove(0);
            oldest.complete();
        }
        list.add(emitter);

        Runnable remove = () -> remove(userId, emitter);
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(e -> remove.run());

        // Primer mensaje: confirma la conexión y dice cada cuánto reintentar.
        send(userId, emitter, SseEmitter.event().name("connected").data("ok").reconnectTime(5_000));
        return emitter;
    }

    @Override
    public void publish(UUID userId, RealtimeEvent event) {
        // Fan-out: el mismo evento a cada conexión de la usuaria.
        for (SseEmitter emitter : connections.getOrDefault(userId, List.of())) {
            send(userId, emitter, SseEmitter.event().name(event.type()).data(event));
        }
    }

    @Override
    public int connectionCount() {
        return connections.values().stream().mapToInt(List::size).sum();
    }

    /**
     * Latido cada 25 s: un comentario SSE que no ve la app pero evita que los
     * proxies (Render, navegadores) cierren la conexión por inactividad.
     */
    @Scheduled(fixedRate = 25_000)
    void heartbeat() {
        connections.forEach((userId, list) ->
                list.forEach(emitter -> send(userId, emitter, SseEmitter.event().comment("ping"))));
    }

    private void send(UUID userId, SseEmitter emitter, SseEmitter.SseEventBuilder event) {
        try {
            emitter.send(event);
        } catch (IOException | IllegalStateException e) {
            log.debug("Conexión SSE cerrada: {}", e.getMessage());
            remove(userId, emitter);
        }
    }

    private void remove(UUID userId, SseEmitter emitter) {
        connections.computeIfPresent(userId, (id, list) -> {
            list.remove(emitter);
            return list.isEmpty() ? null : list;
        });
    }
}
