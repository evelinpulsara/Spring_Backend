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

    public PredictionService(AiTextGenerator generator, ObjectMapper objectMapper) {
        this.generator = generator;
        this.objectMapper = objectMapper;
    }

    public PredictionResponse predict(PredictionRequest request) {
        boolean spanish = "es".equals(request.language());
        AiTextGenerator.GeneratedText generated =
                generator.generateJson(systemInstruction(spanish), userPrompt(request), RESPONSE_SCHEMA);
        AiPrediction raw = parse(generated.text());
        return sanitize(raw, request, generated.model());
    }

    String systemInstruction(boolean spanish) {
        String language = spanish ? "español neutro" : "English";
        return """
                Eres el motor de predicción del ciclo menstrual de la app "Spring" (proyecto académico).
                Tu tarea: predecir cuándo empezará el PRÓXIMO periodo, cuántos días durará y la ovulación.
                Reglas:
                - Usa los ciclos registrados; da más peso a los más recientes. Si hay pocos ciclos (0–2), \
                apóyate en el perfil (duración típica del ciclo y del periodo).
                - "likelyStart" es el día más probable. "earliestStart" y "latestStart" forman la ventana \
                de días posibles: 1–3 días de ancho total si el ciclo es regular y hay 3+ ciclos parecidos; \
                3–7 días si es irregular, si es adolescente (sus ciclos varían más: 21–45 días es normal) \
                o si hay pocos datos.
                - Rangos de referencia: adultas, ciclos de 24–38 días; adolescentes, 21–45 días; \
                el sangrado suele durar entre 2 y 7 días.
                - "periodLength": días que probablemente durará el sangrado (según historial y perfil).
                - "ovulationDate": normalmente unos 14 días ANTES de likelyStart (fase lútea de 12–16 días).
                - "confidence": high (regular con buen historial), medium, o low (irregular / pocos datos).
                - "explanation": 1 o 2 oraciones en %s, cálidas, explicando en qué te basaste. \
                Recuerda que es una estimación. Sin diagnósticos médicos.
                - Las fechas DEBEN ser posteriores al último inicio registrado y en formato YYYY-MM-DD.
                Devuelve solo JSON con los campos pedidos.
                """.formatted(language);
    }

    String userPrompt(PredictionRequest request) {
        var p = request.profile();
        var b = request.baseline();
        StringBuilder sb = new StringBuilder();
        sb.append("Hoy: ").append(request.today()).append('\n');
        sb.append("Idioma: ").append(request.language()).append("\n\n");
        sb.append("PERFIL (cuestionario):\n");
        sb.append("- Grupo de edad: ").append(p.ageGroup()).append('\n');
        sb.append("- Regularidad: ").append(p.regularity()).append('\n');
        sb.append("- Duración típica del periodo: ").append(p.typicalPeriodLength()).append(" días\n");
        sb.append("- Duración típica del ciclo: ").append(p.typicalCycleLength()).append(" días\n");

        sb.append("\nCICLOS REGISTRADOS (inicio, días de sangrado, duración del ciclo):\n");
        List<CycleDto> cycles = request.cycles().stream()
                .sorted(Comparator.comparing(CycleDto::startDate)).toList();
        if (cycles.isEmpty()) {
            sb.append("- Ninguno\n");
        } else {
            cycles.forEach(c -> sb.append("- ").append(c.startDate())
                    .append(", ").append(c.periodLength()).append(" días, ")
                    .append(c.cycleLength() == null ? "en curso" : c.cycleLength() + " días")
                    .append('\n'));
        }
        sb.append("\nCÁLCULO SIMPLE DE REFERENCIA (promedios): próximo inicio ").append(b.nextStart())
                .append(", duración ").append(b.periodLength()).append(" días, ovulación ")
                .append(b.ovulation()).append('\n');
        return sb.toString();
    }

    /** Corrige valores poco realistas usando el cálculo de referencia. */
    PredictionResponse sanitize(AiPrediction ai, PredictionRequest request, String model) {
        var baseline = request.baseline();
        boolean adjusted = false;

        LocalDate lastStart = request.cycles().stream()
                .map(CycleDto::startDate).max(Comparator.naturalOrder()).orElse(null);
        LocalDate minStart = lastStart == null ? request.today().minusDays(60) : lastStart.plusDays(15);

        LocalDate likely = ai.likely();
        if (likely == null
                || Math.abs(ChronoUnit.DAYS.between(baseline.nextStart(), likely)) > MAX_DAYS_FROM_BASELINE
                || likely.isBefore(minStart)) {
            likely = baseline.nextStart();
            adjusted = true;
        }

        LocalDate earliest = ai.earliest();
        if (earliest == null || earliest.isAfter(likely)
                || ChronoUnit.DAYS.between(earliest, likely) > MAX_WINDOW_SIDE) {
            earliest = likely.minusDays(1);
            adjusted = true;
        }
        LocalDate latest = ai.latest();
        if (latest == null || latest.isBefore(likely)
                || ChronoUnit.DAYS.between(likely, latest) > MAX_WINDOW_SIDE) {
            latest = likely.plusDays(1);
            adjusted = true;
        }

