package com.spring_calendario.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring_calendario.ai.dto.InsightRequest.CycleDto;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class PromptBuilderTest {

    @Test
    void armaSeccionesConElementosOUnTextoSiEstaVacia() {
        String prompt = new PromptBuilder()
                .line("Hoy: ", LocalDate.of(2026, 11, 17))
                .section("CICLOS")
                .items(List.of(new CycleDto(LocalDate.of(2026, 10, 22), 5, null)), "Ninguno", PromptBuilder::cycle)
                .section("SÍNTOMAS")
                .items(List.<String>of(), "Ninguno", s -> s)
                .build();

        assertThat(prompt).isEqualTo("""
                Hoy: 2026-11-17

                CICLOS:
                - 2026-10-22, 5 días, en curso

                SÍNTOMAS:
                - Ninguno
                """);
    }

    @Test
    void quitaElBloqueDeCodigoQueAVecesDevuelveLaIa() {
        assertThat(AiJsonService.stripCodeFence("```json\n{\"a\":1}\n```")).isEqualTo("{\"a\":1}");
        assertThat(AiJsonService.stripCodeFence("  {\"a\":1} ")).isEqualTo("{\"a\":1}");
    }
}
