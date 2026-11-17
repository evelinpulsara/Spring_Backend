package com.spring_calendario.ai;

import com.spring_calendario.ai.dto.InsightRequest;
import com.spring_calendario.ai.dto.InsightRequest.CycleDto;
import com.spring_calendario.ai.dto.InsightRequest.IntimacyDto;
import com.spring_calendario.ai.dto.InsightRequest.PredictionDto;
import com.spring_calendario.ai.dto.InsightRequest.SymptomDto;
import com.spring_calendario.ai.dto.InsightResponse;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class InsightService extends AiJsonService<InsightRequest, InsightService.AiOutput, InsightResponse> {

    static final int MAX_SYMPTOMS_IN_PROMPT = 120;
    static final int MAX_INTIMACY_IN_PROMPT = 20;

    private static final Map<String, Object> RESPONSE_SCHEMA = Map.of(
            "type", "object",
            "properties", Map.of(
                    "title", Map.of("type", "string"),
                    "message", Map.of("type", "string")),
            "required", List.of("title", "message"));

    public InsightService(AiTextGenerator generator, ObjectMapper objectMapper) {
        super(generator, objectMapper, AiOutput.class);
    }

    public InsightResponse generate(InsightRequest request) {
        return execute(request);
    }

    @Override
    protected Map<String, Object> responseSchema() {
        return RESPONSE_SCHEMA;
    }

    @Override
    protected String systemInstruction(boolean spanish) {
        return """
                Eres el "Asistente Spring", el asistente de bienestar de una app de seguimiento \
                del ciclo menstrual (proyecto académico).
                Tu tarea: leer el resumen de ciclos y síntomas de la usuaria y darle UN consejo \
                breve y personalizado.
                Reglas:
                - Responde SIEMPRE en %s.
                - Tono cálido, cercano y sin juzgar. Habla en segunda persona.
                - Basa el consejo en los datos: menciona patrones concretos (por ejemplo, síntomas \
                que se repiten antes de la menstruación, el síntoma más frecuente o la fase actual).
                - Da 1 o 2 sugerencias prácticas de autocuidado (descanso, hidratación, sueño, \
                actividad suave, registrar más días...).
                - NO diagnostiques enfermedades, NO recetes medicamentos ni dosis.
                - Si hay síntomas de intensidad 5 repetidos o ciclos muy irregulares (menos de 21 \
                o más de 35 días), sugiere con suavidad consultar a un profesional de salud.
                - Si hay pocos datos, dilo y anima a seguir registrando.
                RELACIONES SEXUALES (sección "RELACIONES REGISTRADAS"), tienen PRIORIDAD sobre lo demás:
                - Si hay una relación con riesgo "high" (sin protección o sin saberlo, dentro de la \
                ventana fértil u ovulación) de hace 5 días o menos: el título debe indicar un posible \
                riesgo de embarazo. Explica con calma que fue en sus días fértiles estimados y que, si no \
                busca un embarazo, puede considerar la pastilla anticonceptiva de emergencia: es más eficaz \
                cuanto antes se tome (idealmente dentro de las primeras 72 horas; algunas opciones, como el \
                acetato de ulipristal o el DIU de cobre, sirven hasta 5 días). Se consigue en farmacias o \
                centros de salud. Sugiere una prueba de embarazo si la menstruación se retrasa más de una semana.
                - Si fue hace más de 5 días con riesgo "high": la pastilla de emergencia podría ya no ser \
                eficaz; sugiere prueba de embarazo si hay retraso y consultar a un profesional de salud.
                - Riesgo "moderate" (con protección en días fértiles): el riesgo es bajo; menciona que si el \
                condón se rompió o olvidó su anticonceptivo, puede considerar la pastilla de emergencia.
                - Riesgo "low" (fuera de la ventana fértil): el riesgo es bajo pero no cero porque los ciclos varían.
                - Si hubo relaciones sin protección, recuerda en una frase breve que el condón también protege \
                de infecciones de transmisión sexual.
                - Nunca juzgues ni regañes. Aclara que las fechas fértiles son una estimación y que el \
                calendario no es un método anticonceptivo.
                - "title": máximo 6 palabras. "message": entre 2 y 5 oraciones, sin listas ni markdown.
                Devuelve solo JSON con los campos "title" y "message".
                """.formatted(languageName(spanish));
    }

    @Override
    protected String userPrompt(InsightRequest request) {
        PromptBuilder prompt = new PromptBuilder().line("Idioma de respuesta: ", request.language());
        appendPrediction(prompt, request.prediction());

        prompt.section("CICLOS REGISTRADOS (inicio, días de sangrado, duración del ciclo)")
                .items(request.cycles().stream().sorted(Comparator.comparing(CycleDto::startDate)).toList(),
                        "Ninguno", PromptBuilder::cycle);

        prompt.section("RELACIONES REGISTRADAS (fecha, protección, ¿en ventana fértil?, ¿día de ovulación?, "
                        + "riesgo, hace cuántos días)")
                .items(request.intimacyOrEmpty().stream()
                                .sorted(Comparator.comparing(IntimacyDto::date).reversed())
                                .limit(MAX_INTIMACY_IN_PROMPT).toList(),
                        "Ninguna", InsightService::describe);

        appendSymptoms(prompt, request.symptoms());
        return prompt.build();
    }

    @Override
    protected InsightResponse toResponse(AiOutput output, InsightRequest request, String model) {
        if (output.isIncomplete()) {
            throw new AiUnavailableException("La IA devolvió un consejo incompleto.");
        }
        return new InsightResponse(output.title().trim(), output.message().trim(),
                request.symptoms().size(), model, Instant.now());
    }

    private static void appendPrediction(PromptBuilder prompt, PredictionDto p) {
        prompt.section("PREDICCIÓN ACTUAL");
        if (p == null) {
            prompt.item("Sin predicción (aún no hay menstruaciones registradas).");
            return;
        }
        prompt.item("Día del ciclo: ", p.cycleDay(), " de ", p.cycleLength())
                .item("Fase actual: ", p.phase())
                .item("Días para la próxima menstruación: ", p.daysUntilNextPeriod());
    }

    private static void appendSymptoms(PromptBuilder prompt, List<SymptomDto> symptoms) {
        prompt.section("FRECUENCIA DE SÍNTOMAS (total de registros)");
        if (symptoms.isEmpty()) {
            prompt.item("Ninguno registrado");
            return;
        }
        symptoms.stream()
                .collect(Collectors.groupingBy(SymptomDto::type, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> prompt.item(e.getKey(), ": ", e.getValue()));

        prompt.section("SÍNTOMAS RECIENTES (fecha, síntoma, intensidad 1-5)")
                .items(symptoms.stream()
                                .sorted(Comparator.comparing(SymptomDto::date).reversed())
                                .limit(MAX_SYMPTOMS_IN_PROMPT).toList(),
                        "Ninguno", s -> s.date() + ", " + s.type() + ", " + s.intensity());
    }

    private static String describe(IntimacyDto i) {
        return i.date() + ", " + i.protection()
                + ", fértil=" + yesNo(i.inFertileWindow())
                + ", ovulación=" + yesNo(i.ovulationDay())
                + ", riesgo=" + i.risk()
                + ", hace " + i.daysAgo() + " días";
    }

    private static String yesNo(boolean value) {
        return value ? "sí" : "no";
    }

    record AiOutput(String title, String message) {

        boolean isIncomplete() {
            return title == null || title.isBlank() || message == null || message.isBlank();
        }
    }
}
