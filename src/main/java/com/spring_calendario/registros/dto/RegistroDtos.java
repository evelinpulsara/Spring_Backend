package com.spring_calendario.registros.dto;

import com.spring_calendario.registros.DiaPeriodo;
import com.spring_calendario.registros.Relacion;
import com.spring_calendario.registros.Sintoma;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public final class RegistroDtos {

    private RegistroDtos() {}

    public record PeriodDayDto(LocalDate date, String flow) {

        public static PeriodDayDto from(DiaPeriodo dia) {
            return new PeriodDayDto(dia.getFecha(), dia.getFlujo());
        }
    }

    public record PeriodDayBody(@NotNull @Pattern(regexp = "none|light|medium|heavy") String flow) {}

    public record SymptomDto(LocalDate date, String type, int intensity) {

        public static SymptomDto from(Sintoma sintoma) {
            return new SymptomDto(sintoma.getFecha(), sintoma.getTipo(), sintoma.getIntensidad());
        }
    }

    public record SymptomItem(
            @NotNull @Pattern(regexp = "cramps|headache|mood|bloating|acne|fatigue|appetite|sleep") String type,
            @Min(1) @Max(5) int intensity) {}

    public record IntimacyDto(LocalDate date, String protection) {

        public static IntimacyDto from(Relacion relacion) {
            return new IntimacyDto(relacion.getFecha(), relacion.getProteccion());
        }
    }

    public record IntimacyBody(
            @NotNull @Pattern(regexp = "none|condom|contraceptive|unsure") String protection) {}
}
