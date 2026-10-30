package com.spring_calendario.ai;

import com.spring_calendario.ai.dto.InsightRequest.CycleDto;
import com.spring_calendario.ai.dto.PredictionRequest;
import com.spring_calendario.ai.dto.PredictionResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Pide a la IA la predicción del próximo periodo y la valida: si la IA da
 * fechas poco realistas, se corrigen usando el cálculo de la app como referencia.
 */
@Service
public class PredictionService {

    /** Máximo que la IA puede alejarse del cálculo de referencia. */
    static final int MAX_DAYS_FROM_BASELINE = 10;
    /** Máximo de días entre el día probable y los extremos de la ventana. */
    static final int MAX_WINDOW_SIDE = 4;
    static final int LUTEAL_DAYS = 14;

    static final Map<String, Object> RESPONSE_SCHEMA = Map.of(
            "type", "object",
            "properties", Map.of(
                    "earliestStart", Map.of("type", "string", "description", "YYYY-MM-DD"),
                    "likelyStart", Map.of("type", "string", "description", "YYYY-MM-DD"),
                    "latestStart", Map.of("type", "string", "description", "YYYY-MM-DD"),
                    "periodLength", Map.of("type", "integer"),
                    "ovulationDate", Map.of("type", "string", "description", "YYYY-MM-DD"),
                    "confidence", Map.of("type", "string", "enum", List.of("low", "medium", "high")),
                    "explanation", Map.of("type", "string")),
            "required", List.of("earliestStart", "likelyStart", "latestStart", "periodLength",
                    "ovulationDate", "confidence", "explanation"));

    private static final Set<String> CONFIDENCE = Set.of("low", "medium", "high");

    private final AiTextGenerator generator;
    private final ObjectMapper objectMapper;

