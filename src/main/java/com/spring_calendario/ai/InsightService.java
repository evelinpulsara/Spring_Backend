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

    /** Forma de la respuesta: {"title": "...", "message": "..."}. */
    static final Map<String, Object> RESPONSE_SCHEMA = Map.of(
            "type", "object",
            "properties", Map.of(
                    "title", Map.of("type", "string"),
                    "message", Map.of("type", "string")),
            "required", List.of("title", "message"));

    private final AiTextGenerator generator;
    private final ObjectMapper objectMapper;

    public InsightService(AiTextGenerator generator, ObjectMapper objectMapper) {
        this.generator = generator;
        this.objectMapper = objectMapper;
    }

    public InsightResponse generate(InsightRequest request) {
        boolean spanish = "es".equals(request.language());
        AiTextGenerator.GeneratedText generated =
                generator.generateJson(systemInstruction(spanish), userPrompt(request), RESPONSE_SCHEMA);
        AiOutput output = parse(generated.text());
        return new InsightResponse(
                output.title().trim(),
                output.message().trim(),
                request.symptoms().size(),
                generated.model(),
                Instant.now());
    }

    String systemInstruction(boolean spanish) {
        String language = spanish ? "español neutro" : "English";
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
                - Si hay una relación con riesgo "high" (sin protección o sin saberlo, dentro de la                 ventana fértil u ovulación) de hace 5 días o menos: el título debe indicar un posible                 riesgo de embarazo. Explica con calma que fue en sus días fértiles estimados y que, si no                 busca un embarazo, puede considerar la pastilla anticonceptiva de emergencia: es más eficaz                 cuanto antes se tome (idealmente dentro de las primeras 72 horas; algunas opciones, como el                 acetato de ulipristal o el DIU de cobre, sirven hasta 5 días). Se consigue en farmacias o                 centros de salud. Sugiere una prueba de embarazo si la menstruación se retrasa más de una semana.
                - Si fue hace más de 5 días con riesgo "high": la pastilla de emergencia podría ya no ser                 eficaz; sugiere prueba de embarazo si hay retraso y consultar a un profesional de salud.
                - Riesgo "moderate" (con protección en días fértiles): el riesgo es bajo; menciona que si el                 condón se rompió o olvidó su anticonceptivo, puede considerar la pastilla de emergencia.
                - Riesgo "low" (fuera de la ventana fértil): el riesgo es bajo pero no cero porque los ciclos varían.
                - Si hubo relaciones sin protección, recuerda en una frase breve que el condón también protege                 de infecciones de transmisión sexual.
                - Nunca juzgues ni regañes. Aclara que las fechas fértiles son una estimación y que el                 calendario no es un método anticonceptivo.
                - "title": máximo 6 palabras. "message": entre 2 y 5 oraciones, sin listas ni markdown.
                Devuelve solo JSON con los campos "title" y "message".
                """.formatted(language);
    }

    String userPrompt(InsightRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Idioma de respuesta: ").append(request.language()).append("\n\n");

        sb.append("PREDICCIÓN ACTUAL:\n");
        if (request.prediction() == null) {
            sb.append("- Sin predicción (aún no hay menstruaciones registradas).\n");
        } else {
            var p = request.prediction();
            sb.append("- Día del ciclo: ").append(p.cycleDay()).append(" de ").append(p.cycleLength()).append('\n');
            sb.append("- Fase actual: ").append(p.phase()).append('\n');
            sb.append("- Días para la próxima menstruación: ").append(p.daysUntilNextPeriod()).append('\n');
        }

        sb.append("\nCICLOS REGISTRADOS (inicio, días de sangrado, duración del ciclo):\n");
        if (request.cycles().isEmpty()) {
            sb.append("- Ninguno\n");
        } else {
            request.cycles().stream()
                    .sorted(Comparator.comparing(CycleDto::startDate))
                    .forEach(c -> sb.append("- ").append(c.startDate())
                            .append(", ").append(c.periodLength()).append(" días, ")
                            .append(c.cycleLength() == null ? "en curso" : c.cycleLength() + " días")
                            .append('\n'));
        }

        sb.append("\nRELACIONES REGISTRADAS (fecha, protección, ¿en ventana fértil?, ¿día de ovulación?, riesgo, hace cuántos días):\n");
        if (request.intimacyOrEmpty().isEmpty()) {
            sb.append("- Ninguna\n");
        } else {
            request.intimacyOrEmpty().stream()
                    .sorted(Comparator.comparing(IntimacyDto::date).reversed())
                    .limit(20)
                    .forEach(i -> sb.append("- ").append(i.date())
                            .append(", ").append(i.protection())
                            .append(", fértil=").append(i.inFertileWindow() ? "sí" : "no")
                            .append(", ovulación=").append(i.ovulationDay() ? "sí" : "no")
                            .append(", riesgo=").append(i.risk())
                            .append(", hace ").append(i.daysAgo()).append(" días\n"));
        }

        List<SymptomDto> symptoms = request.symptoms();
        sb.append("\nFRECUENCIA DE SÍNTOMAS (total de registros):\n");
        if (symptoms.isEmpty()) {
            sb.append("- Ninguno registrado\n");
        } else {
            Map<String, Long> counts = symptoms.stream()
                    .collect(Collectors.groupingBy(SymptomDto::type, Collectors.counting()));
            counts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(e -> sb.append("- ").append(e.getKey()).append(": ").append(e.getValue()).append('\n'));

            sb.append("\nSÍNTOMAS RECIENTES (fecha, síntoma, intensidad 1-5):\n");
            symptoms.stream()
                    .sorted(Comparator.comparing(SymptomDto::date).reversed())
                    .limit(MAX_SYMPTOMS_IN_PROMPT)
                    .forEach(s -> sb.append("- ").append(s.date()).append(", ")
                            .append(s.type()).append(", ").append(s.intensity()).append('\n'));
        }
        return sb.toString();
    }

    private AiOutput parse(String raw) {
        String json = raw.strip();
        // Por si el modelo envuelve el JSON en ```json ... ```
        if (json.startsWith("```")) {
            json = json.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
        }
        try {
