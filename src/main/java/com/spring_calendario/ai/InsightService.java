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
