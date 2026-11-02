package com.spring_calendario.ai;

import com.spring_calendario.ai.dto.InsightRequest;
import com.spring_calendario.ai.dto.InsightRequest.CycleDto;
import com.spring_calendario.ai.dto.InsightRequest.IntimacyDto;
import com.spring_calendario.ai.dto.InsightRequest.SymptomDto;
import com.spring_calendario.ai.dto.InsightResponse;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Arma la consulta para la IA con los datos del ciclo y los síntomas,
 * y convierte la respuesta en un consejo para la app.
 */
@Service
public class InsightService {

    /** Máximo de síntomas que se envían a la IA (los más recientes). */
    static final int MAX_SYMPTOMS_IN_PROMPT = 120;
