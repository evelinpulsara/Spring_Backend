package com.spring_calendario.ai.dto;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Predicción del próximo periodo.
 *
 * @param earliestStart primer día posible de inicio
 * @param likelyStart   día más probable de inicio
 * @param latestStart   último día posible de inicio
 * @param adjusted      true si el backend tuvo que corregir valores poco realistas de la IA
 */
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
