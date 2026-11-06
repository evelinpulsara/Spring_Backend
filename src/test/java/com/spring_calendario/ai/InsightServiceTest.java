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
