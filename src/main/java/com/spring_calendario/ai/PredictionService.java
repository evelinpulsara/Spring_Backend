package com.spring_calendario.ai;

import com.spring_calendario.ai.dto.InsightRequest.CycleDto;
import com.spring_calendario.ai.dto.PredictionRequest;
import com.spring_calendario.ai.dto.PredictionRequest.BaselineDto;
import com.spring_calendario.ai.dto.PredictionResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class PredictionService
        extends AiJsonService<PredictionRequest, PredictionService.RawPrediction, PredictionResponse> {
    static final int MAX_DAYS_FROM_BASELINE = 10;
    static final int MAX_WINDOW_SIDE = 4;
    static final int LUTEAL_DAYS = 14;
    static final int MIN_PERIOD = 2;
    static final int MAX_PERIOD = 10;

    private static final String DEFAULT_CONFIDENCE = "medium";
    private static final List<String> CONFIDENCE_LEVELS = List.of("low", DEFAULT_CONFIDENCE, "high");
    private static final Set<String> CONFIDENCE = Set.copyOf(CONFIDENCE_LEVELS);

    private static final String TYPE = "type";
    private static final Map<String, Object> DATE_FIELD = Map.of(TYPE, "string", "description", "YYYY-MM-DD");
    private static final Map<String, Object> TEXT_FIELD = Map.of(TYPE, "string");

    private static final Map<String, Object> RESPONSE_SCHEMA = Map.of(
            TYPE, "object",
            "properties", Map.of(
                    "earliestStart", DATE_FIELD,
                    "likelyStart", DATE_FIELD,
                    "latestStart", DATE_FIELD,
                    "periodLength", Map.of(TYPE, "integer"),
                    "ovulationDate", DATE_FIELD,
                    "confidence", Map.of(TYPE, "string", "enum", CONFIDENCE_LEVELS),
                    "explanation", TEXT_FIELD),
            "required", List.of("earliestStart", "likelyStart", "latestStart", "periodLength",
                    "ovulationDate", "confidence", "explanation"));

    public PredictionService(AiTextGenerator generator, ObjectMapper objectMapper) {
        super(generator, objectMapper, RawPrediction.class);
    }

    public PredictionResponse predict(PredictionRequest request) {
        return execute(request);
    }

    @Override
    protected Map<String, Object> responseSchema() {
        return RESPONSE_SCHEMA;
    }

    @Override
    protected String systemInstruction(boolean spanish) {
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
                """.formatted(languageName(spanish));
    }

    @Override
    protected String userPrompt(PredictionRequest request) {
        var profile = request.profile();
        var baseline = request.baseline();
        return new PromptBuilder()
                .line("Hoy: ", request.today())
                .line("Idioma: ", request.language())
                .section("PERFIL (cuestionario)")
                .item("Grupo de edad: ", profile.ageGroup())
                .item("Regularidad: ", profile.regularity())
                .item("Duración típica del periodo: ", profile.typicalPeriodLength(), " días")
                .item("Duración típica del ciclo: ", profile.typicalCycleLength(), " días")
                .section("CICLOS REGISTRADOS (inicio, días de sangrado, duración del ciclo)")
                .items(request.cycles().stream().sorted(Comparator.comparing(CycleDto::startDate)).toList(),
                        "Ninguno", PromptBuilder::cycle)
                .section("CÁLCULO SIMPLE DE REFERENCIA (promedios)")
                .item("próximo inicio ", baseline.nextStart(), ", duración ", baseline.periodLength(),
                        " días, ovulación ", baseline.ovulation())
                .build();
    }

    @Override
    protected PredictionResponse toResponse(RawPrediction raw, PredictionRequest request, String model) {
        return sanitize(raw.toPrediction(), request, model);
    }

    PredictionResponse sanitize(AiPrediction ai, PredictionRequest request, String model) {
        BaselineDto baseline = request.baseline();
        Corrections corrections = new Corrections();
        LocalDate minStart = minimumStart(request);

        LocalDate likely = corrections.keepIf(ai.likely(),
                d -> daysBetween(baseline.nextStart(), d) <= MAX_DAYS_FROM_BASELINE && !d.isBefore(minStart),
                baseline.nextStart());
        LocalDate earliest = corrections.keepIf(ai.earliest(),
                d -> !d.isAfter(likely) && ChronoUnit.DAYS.between(d, likely) <= MAX_WINDOW_SIDE,
                likely.minusDays(1));
        LocalDate latest = corrections.keepIf(ai.latest(),
                d -> !d.isBefore(likely) && ChronoUnit.DAYS.between(likely, d) <= MAX_WINDOW_SIDE,
                likely.plusDays(1));
        int periodLength = corrections.keepIf(
                ai.periodLength() == null ? baseline.periodLength() : ai.periodLength(),
                n -> n >= MIN_PERIOD && n <= MAX_PERIOD,
                Math.clamp(baseline.periodLength(), MIN_PERIOD, MAX_PERIOD));
        LocalDate ovulation = corrections.keepIf(ai.ovulation(),
                d -> isLutealPhase(ChronoUnit.DAYS.between(d, likely)),
                likely.minusDays(LUTEAL_DAYS));

        String confidence = ai.confidence() != null && CONFIDENCE.contains(ai.confidence()) ? ai.confidence() : DEFAULT_CONFIDENCE;
        String explanation = ai.explanation() == null ? "" : ai.explanation().trim();

        return new PredictionResponse(
                earliest, likely, latest, periodLength, ovulation,
                ovulation.minusDays(5), ovulation.plusDays(1),
                confidence, explanation, model, corrections.applied(), Instant.now());
    }

    private static LocalDate minimumStart(PredictionRequest request) {
        return request.cycles().stream()
                .map(CycleDto::startDate)
                .max(Comparator.naturalOrder())
                .map(last -> last.plusDays(15))
                .orElse(request.today().minusDays(60));
    }

    private static long daysBetween(LocalDate a, LocalDate b) {
        return Math.abs(ChronoUnit.DAYS.between(a, b));
    }

    private static boolean isLutealPhase(long daysBeforePeriod) {
        return daysBeforePeriod >= 10 && daysBeforePeriod <= 17;
    }

    private static final class Corrections {

        private boolean applied;

        <T> T keepIf(T value, Predicate<T> isValid, T fallback) {
            if (value != null && isValid.test(value)) {
                return value;
            }
            applied = true;
            return fallback;
        }

        boolean applied() {
            return applied;
        }
    }

    record RawPrediction(String earliestStart, String likelyStart, String latestStart,
            Integer periodLength, String ovulationDate, String confidence, String explanation) {
        AiPrediction toPrediction() {
            return new AiPrediction(date(earliestStart), date(likelyStart), date(latestStart),
                    periodLength, date(ovulationDate), confidence, explanation);
        }

        private static LocalDate date(String value) {
            if (value == null) {
                return null;
            }
            try {
                return LocalDate.parse(value.strip());
            } catch (DateTimeParseException e) {
                return null;
            }
        }
    }

    record AiPrediction(LocalDate earliest, LocalDate likely, LocalDate latest,
            Integer periodLength, LocalDate ovulation, String confidence, String explanation) {}
}
