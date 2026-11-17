package com.spring_calendario.ai.dto;

import java.time.Instant;
import java.time.LocalDate;

public record PredictionResponse(
        LocalDate earliestStart,
        LocalDate likelyStart,
        LocalDate latestStart,
        int periodLength,
        LocalDate ovulationDate,
        LocalDate fertileStart,
        LocalDate fertileEnd,
        String confidence,
        String explanation,
        String model,
        boolean adjusted,
        Instant generatedAt) {}
