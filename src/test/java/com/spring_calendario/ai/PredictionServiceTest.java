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

    /** Ejemplo real: periodo el 22 de octubre de 2026, ciclos de ~29 días. */
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
