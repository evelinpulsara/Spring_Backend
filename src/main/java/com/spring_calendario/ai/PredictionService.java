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

