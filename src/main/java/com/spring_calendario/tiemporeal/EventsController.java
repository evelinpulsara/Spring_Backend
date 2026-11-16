package com.spring_calendario.tiemporeal;

import com.spring_calendario.seguridad.TokenService;
import com.spring_calendario.usuarios.ApiException;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Tiempo real con Server-Sent Events (SSE).
 *
 * <p>Se eligió SSE y no WebSockets porque la comunicación es de una sola vía
 * (servidor → app): la app guarda con REST normal y solo necesita enterarse de
 * los cambios. SSE funciona sobre HTTP normal, se reconecta solo y no necesita
 * el "Upgrade" a TCP de WebSockets. Comparado con <i>polling</i>, no hace
 * peticiones cada X segundos sin que haya cambios.
 *
 * <pre>
 * POST /api/events/ticket          (con token)  → { "ticket": "..." }
 * GET  /api/events/stream?ticket=  (público)    → text/event-stream
 * </pre>
 */
@RestController
@RequestMapping("/api/events")
public class EventsController {

    private final EventBus bus;
    private final StreamTicketService tickets;

    public EventsController(EventBus bus, StreamTicketService tickets) {
        this.bus = bus;
        this.tickets = tickets;
    }

    @PostMapping("/ticket")
    public Map<String, String> ticket(Authentication auth) {
        return Map.of("ticket", tickets.issue(TokenService.userId(auth)));
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam(required = false) String ticket) {
        UUID userId = tickets.redeem(ticket)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "invalid_ticket"));
        return bus.subscribe(userId);
    }
}
