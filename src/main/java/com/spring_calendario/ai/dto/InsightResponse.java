package com.spring_calendario.ai.dto;

import java.time.Instant;

/** Consejo generado por la IA. */
public record InsightResponse(
        String title,
        String message,
        int basedOnEntries,
        String model,
        Instant generatedAt) {}
