package com.spring_calendario.ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import com.spring_calendario.ai.dto.InsightRequest.CycleDto;
import java.util.List;

/**
 * Datos para que la IA prediga el próximo periodo. Sin nombre ni correo.
 *
 * @param profile  respuestas del cuestionario inicial
 * @param cycles   ciclos registrados (inicio, días de sangrado, duración)
 * @param baseline cálculo simple de la app, usado como referencia y respaldo
 */
public record PredictionRequest(
        @NotNull @Pattern(regexp = "es|en") String language,
        @NotNull LocalDate today,
        @NotNull @Valid ProfileDto profile,
        @NotNull @Size(max = 60) List<@Valid CycleDto> cycles,
        @NotNull @Valid BaselineDto baseline) {

    public record ProfileDto(
            @NotNull @Pattern(regexp = "teen|adult|unknown") String ageGroup,
            @NotNull @Pattern(regexp = "regular|irregular|unsure") String regularity,
            @Min(1) @Max(15) int typicalPeriodLength,
            @Min(15) @Max(60) int typicalCycleLength) {}

    public record BaselineDto(
            @NotNull LocalDate nextStart,
            @Min(1) @Max(15) int periodLength,
            @NotNull LocalDate ovulation) {}
}
