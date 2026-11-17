package com.spring_calendario.tiemporeal;

import java.time.Instant;

public record RealtimeEvent(String type, String resource, Instant at) {

    public static final String RECORDS_CHANGED = "records-changed";

    public static RealtimeEvent recordsChanged(Recurso recurso) {
        return new RealtimeEvent(RECORDS_CHANGED, recurso.nombre(), Instant.now());
    }
}
