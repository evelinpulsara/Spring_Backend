package com.spring_calendario.tiemporeal;

import java.util.UUID;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Bus de eventos en tiempo real (tema 5 del curso: tiempo real y escalabilidad).
 *
 * <p>Cada pestaña o dispositivo de una usuaria abre una conexión SSE
 * ({@code text/event-stream}). Cuando cambia un registro, el evento se envía a
 * TODAS sus conexiones abiertas (patrón <b>fan-out</b>), así el calendario se
 * actualiza solo en el celular y en la computadora a la vez.
 *
 * <p>Hoy hay una sola instancia del backend (Render, plan gratis), por eso basta
 * {@link InMemoryEventBus}. Con varias instancias, las conexiones quedan
 * repartidas en memorias distintas: la solución recomendada es otra
 * implementación de esta interfaz que publique en un <b>backplane Pub/Sub</b>
 * (Redis Pub/Sub o Kafka) y que cada instancia reenvíe a sus propias
 * conexiones. Las <i>sticky sessions</i> no se usan porque desbalancean la
 * carga y se pierden las conexiones si cae un nodo.
 */
public interface EventBus {

    /** Registra una conexión SSE nueva de la usuaria. */
    SseEmitter subscribe(UUID userId);

    /** Envía el evento a todas las conexiones abiertas de la usuaria. */
    void publish(UUID userId, RealtimeEvent event);

    /** Número de conexiones abiertas (todas las usuarias). */
    int connectionCount();
}
