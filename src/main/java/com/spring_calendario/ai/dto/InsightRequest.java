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

    public record SymptomDto(
            @NotNull LocalDate date,
            @NotBlank @Size(max = 30) String type,
            @Min(1) @Max(5) int intensity) {}

    public record CycleDto(
            @NotNull LocalDate startDate,
            @Min(1) @Max(30) int periodLength,
            @Min(1) @Max(120) Integer cycleLength) {}

    /**
     * Relación sexual registrada, con el riesgo que ya calculó la app
     * según la ventana fértil estimada.
     */
    public record IntimacyDto(
            @NotNull LocalDate date,
            @NotNull @Pattern(regexp = "none|condom|contraceptive|unsure") String protection,
            boolean inFertileWindow,
            boolean ovulationDay,
            @NotNull @Pattern(regexp = "low|moderate|high") String risk,
            @Min(0) @Max(400) int daysAgo) {}

    public record PredictionDto(
            @Min(1) @Max(120) int cycleDay,
            @Min(1) @Max(120) int cycleLength,
            @NotBlank @Size(max = 30) String phase,
            @Min(-30) @Max(120) int daysUntilNextPeriod) {}
}
