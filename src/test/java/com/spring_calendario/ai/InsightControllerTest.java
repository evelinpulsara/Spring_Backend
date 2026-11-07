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
