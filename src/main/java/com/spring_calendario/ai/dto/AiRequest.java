package com.spring_calendario.ai.dto;

public interface AiRequest {

    String language();

    default boolean isSpanish() {
        return "es".equals(language());
    }
}
