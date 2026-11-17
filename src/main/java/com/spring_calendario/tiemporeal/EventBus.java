package com.spring_calendario.tiemporeal;

import java.util.UUID;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface EventBus {

    SseEmitter subscribe(UUID userId);

    void publish(UUID userId, RealtimeEvent event);

    int connectionCount();
}
