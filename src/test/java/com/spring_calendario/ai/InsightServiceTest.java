package com.spring_calendario.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spring_calendario.ai.dto.InsightRequest;
import com.spring_calendario.ai.dto.InsightRequest.CycleDto;
import com.spring_calendario.ai.dto.InsightRequest.IntimacyDto;
import com.spring_calendario.ai.dto.InsightRequest.PredictionDto;
import com.spring_calendario.ai.dto.InsightRequest.SymptomDto;
import com.spring_calendario.ai.dto.InsightResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class InsightServiceTest {

    private final AtomicReference<String> lastSystem = new AtomicReference<>();
    private final AtomicReference<String> lastPrompt = new AtomicReference<>();

    private InsightService serviceReturning(String json) {
        AiTextGenerator fake = (system, prompt, schema) -> {
            lastSystem.set(system);
            lastPrompt.set(prompt);
            return new AiTextGenerator.GeneratedText(json, "gemini-test");
        };
        return new InsightService(fake, JsonMapper.builder().build());
    }

    private static InsightRequest sampleRequest(String language) {
        return new InsightRequest(
                language,
                List.of(
                        new SymptomDto(LocalDate.of(2026, 10, 3), "cramps", 4),
                        new SymptomDto(LocalDate.of(2026, 10, 2), "cramps", 3),
                        new SymptomDto(LocalDate.of(2026, 10, 1), "headache", 2)),
                List.of(
                        new CycleDto(LocalDate.of(2026, 9, 5), 5, 28),
                        new CycleDto(LocalDate.of(2026, 10, 3), 4, null)),
                new PredictionDto(12, 28, "follicular", 17),
                null);
    }

    @Test
    void devuelveElConsejoDeLaIa() {
        InsightService service = serviceReturning(
                "{\"title\":\"Cólicos frecuentes\",\"message\":\"Notamos cólicos al inicio de tu ciclo.\"}");

        InsightResponse response = service.generate(sampleRequest("es"));

        assertThat(response.title()).isEqualTo("Cólicos frecuentes");
        assertThat(response.message()).isEqualTo("Notamos cólicos al inicio de tu ciclo.");
        assertThat(response.basedOnEntries()).isEqualTo(3);
        assertThat(response.model()).isEqualTo("gemini-test");
    }

    @Test
    void elPromptIncluyeLosDatosDelCicloYSintomas() {
