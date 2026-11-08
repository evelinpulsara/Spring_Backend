package com.spring_calendario.ai;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spring_calendario.ai.dto.InsightResponse;
import com.spring_calendario.config.ApiExceptionHandler;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class InsightControllerTest {

    private static final String VALID_BODY = """
            {"language":"es",
             "symptoms":[{"date":"2026-10-03","type":"cramps","intensity":4}],
             "cycles":[{"startDate":"2026-10-01","periodLength":5,"cycleLength":null}],
             "prediction":{"cycleDay":3,"cycleLength":28,"phase":"menstrual","daysUntilNextPeriod":25},
             "intimacy":[{"date":"2026-10-02","protection":"none","inFertileWindow":false,"ovulationDay":false,"risk":"low","daysAgo":1}]}
            """;

    private InsightService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(InsightService.class);
        mvc = MockMvcBuilders.standaloneSetup(new InsightController(service, mock(PredictionService.class)))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void devuelveElConsejo() throws Exception {
        when(service.generate(any())).thenReturn(
                new InsightResponse("Título", "Mensaje", 1, "gemini-test", Instant.now()));

        mvc.perform(post("/api/ai/insight").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Título"))
                .andExpect(jsonPath("$.basedOnEntries").value(1));
    }

    @Test
    void rechazaDatosInvalidos() throws Exception {
        String invalid = VALID_BODY.replace("\"intensity\":4", "\"intensity\":9");
        mvc.perform(post("/api/ai/insight").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    void devuelve503SiLaIaNoEstaDisponible() throws Exception {
        when(service.generate(any())).thenThrow(new AiUnavailableException("Falta configurar GEMINI_API_KEY."));

        mvc.perform(post("/api/ai/insight").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Falta configurar GEMINI_API_KEY."));
    }

    @Test
    void rechazaProteccionDesconocida() throws Exception {
        String invalid = VALID_BODY.replace("\"protection\":\"none\"", "\"protection\":\"x\"");
        mvc.perform(post("/api/ai/insight").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest());
    }
}
