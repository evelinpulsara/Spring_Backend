package com.spring_calendario.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spring_calendario.ai.dto.InsightRequest.CycleDto;
import com.spring_calendario.ai.dto.PredictionRequest;
import com.spring_calendario.ai.dto.PredictionRequest.BaselineDto;
import com.spring_calendario.ai.dto.PredictionRequest.ProfileDto;
import com.spring_calendario.ai.dto.PredictionResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class PredictionServiceTest {

    private final AtomicReference<String> lastSystem = new AtomicReference<>();
    private final AtomicReference<String> lastPrompt = new AtomicReference<>();

    private PredictionService serviceReturning(String json) {
        AiTextGenerator fake = (system, prompt, schema) -> {
            lastSystem.set(system);
            lastPrompt.set(prompt);
            return new AiTextGenerator.GeneratedText(json, "gemini-test");
        };
        return new PredictionService(fake, JsonMapper.builder().build());
    }

    private static PredictionRequest request() {
        return new PredictionRequest(
                "es",
                LocalDate.of(2026, 11, 13),
                new ProfileDto("adult", "regular", 5, 29),
                List.of(
                        new CycleDto(LocalDate.of(2026, 9, 23), 5, 29),
                        new CycleDto(LocalDate.of(2026, 10, 22), 5, null)),
                new BaselineDto(LocalDate.of(2026, 11, 20), 5, LocalDate.of(2026, 11, 6)));
    }

    @Test
    void devuelveLaPrediccionDeLaIa() {
        PredictionResponse r = serviceReturning("""
                {"earliestStart":"2026-11-20","likelyStart":"2026-11-21","latestStart":"2026-11-22",
                 "periodLength":5,"ovulationDate":"2026-11-07","confidence":"high",
                 "explanation":"Tus ciclos duran unos 29 días."}""").predict(request());

        assertThat(r.earliestStart()).isEqualTo(LocalDate.of(2026, 11, 20));
        assertThat(r.likelyStart()).isEqualTo(LocalDate.of(2026, 11, 21));
        assertThat(r.latestStart()).isEqualTo(LocalDate.of(2026, 11, 22));
        assertThat(r.ovulationDate()).isEqualTo(LocalDate.of(2026, 11, 7));
        assertThat(r.fertileStart()).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(r.fertileEnd()).isEqualTo(LocalDate.of(2026, 11, 8));
        assertThat(r.confidence()).isEqualTo("high");
        assertThat(r.model()).isEqualTo("gemini-test");
        assertThat(r.adjusted()).isFalse();
    }

    @Test
    void elPromptIncluyePerfilCiclosYReferencia() {
        serviceReturning("""
                {"earliestStart":"2026-11-20","likelyStart":"2026-11-20","latestStart":"2026-11-21",
                 "periodLength":5,"ovulationDate":"2026-11-06","confidence":"high","explanation":"x"}""")
                .predict(request());
        assertThat(lastPrompt.get())
                .contains("Grupo de edad: adult")
                .contains("Duración típica del ciclo: 29 días")
                .contains("2026-10-22, 5 días, en curso")
                .contains("próximo inicio 2026-11-20");
        assertThat(lastSystem.get()).contains("adolescentes, 21–45 días").contains("español");
    }

    @Test
    void corrigeFechasPocoRealistas() {
        PredictionResponse r = serviceReturning("""
                {"earliestStart":"2027-03-01","likelyStart":"2027-03-05","latestStart":"2027-03-20",
                 "periodLength":20,"ovulationDate":"2027-01-01","confidence":"rarísima","explanation":"x"}""")
                .predict(request());

        assertThat(r.likelyStart()).isEqualTo(LocalDate.of(2026, 11, 20));
        assertThat(r.earliestStart()).isEqualTo(LocalDate.of(2026, 11, 19));
        assertThat(r.latestStart()).isEqualTo(LocalDate.of(2026, 11, 21));
        assertThat(r.periodLength()).isEqualTo(5);
        assertThat(r.ovulationDate()).isEqualTo(LocalDate.of(2026, 11, 6));
        assertThat(r.confidence()).isEqualTo("medium");
        assertThat(r.adjusted()).isTrue();
    }

    @Test
    void noAceptaUnInicioAntesDelUltimoPeriodo() {
        PredictionResponse r = serviceReturning("""
                {"earliestStart":"2026-10-24","likelyStart":"2026-10-25","latestStart":"2026-10-26",
                 "periodLength":5,"ovulationDate":"2026-10-11","confidence":"low","explanation":"x"}""")
                .predict(request());
        assertThat(r.likelyStart()).isEqualTo(LocalDate.of(2026, 11, 20));
        assertThat(r.adjusted()).isTrue();
    }

    @Test
    void fallaSiNoEsJson() {
        assertThatThrownBy(() -> serviceReturning("hola").predict(request()))
                .isInstanceOf(AiUnavailableException.class);
    }
}
