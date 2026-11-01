package com.spring_calendario.ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * Datos que envía la app para pedir un consejo. No incluye nombre ni correo:
 * solo síntomas, ciclos, relaciones registradas y la predicción actual.
 */
public record InsightRequest(
        @NotNull @Pattern(regexp = "es|en") String language,
        @NotNull @Size(max = 500) List<@Valid SymptomDto> symptoms,
        @NotNull @Size(max = 60) List<@Valid CycleDto> cycles,
        @Valid PredictionDto prediction,
        @Size(max = 200) List<@Valid IntimacyDto> intimacy) {

    /** Relaciones registradas; si la app no las envía, lista vacía. */
    public List<IntimacyDto> intimacyOrEmpty() {
        return intimacy == null ? List.of() : intimacy;
    }

