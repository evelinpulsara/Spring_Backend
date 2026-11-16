package com.spring_calendario.tiemporeal;

import java.time.Instant;

/**
 * Evento que recibe la app por SSE.
 *
 * @param type     nombre del evento SSE (por ejemplo {@code records-changed})
 * @param resource qué cambió: {@code period-days}, {@code symptoms}, {@code intimacy} o {@code all}
 * @param at       momento del cambio
 */
public record RealtimeEvent(String type, String resource, Instant at) {

    public static final String RECORDS_CHANGED = "records-changed";

    public static RealtimeEvent recordsChanged(String resource) {
        return new RealtimeEvent(RECORDS_CHANGED, resource, Instant.now());
    }
}
