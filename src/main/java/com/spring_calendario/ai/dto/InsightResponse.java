package com.spring_calendario.ai.dto;

import java.time.Instant;

public record InsightResponse(
        String title,
        String message,
        int basedOnEntries,
        String model,
        Instant generatedAt) {}
