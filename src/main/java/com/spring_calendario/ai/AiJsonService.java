package com.spring_calendario.ai;

import com.spring_calendario.ai.dto.AiRequest;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public abstract class AiJsonService<Q extends AiRequest, R, S> {

    private static final String CODE_FENCE = "```";

    private final AiTextGenerator generator;
    private final ObjectMapper objectMapper;
    private final Class<R> rawType;

    protected AiJsonService(AiTextGenerator generator, ObjectMapper objectMapper, Class<R> rawType) {
        this.generator = generator;
        this.objectMapper = objectMapper;
        this.rawType = rawType;
    }

    protected final S execute(Q request) {
        AiTextGenerator.GeneratedText generated = generator.generateJson(
                systemInstruction(request.isSpanish()), userPrompt(request), responseSchema());
        return toResponse(read(generated.text()), request, generated.model());
    }

    protected abstract String systemInstruction(boolean spanish);

    protected abstract String userPrompt(Q request);

    protected abstract Map<String, Object> responseSchema();

    protected abstract S toResponse(R raw, Q request, String model);

    protected static String languageName(boolean spanish) {
        return spanish ? "español neutro" : "English";
    }

    private R read(String text) {
        try {
            R value = objectMapper.readValue(stripCodeFence(text), rawType);
            if (value == null) {
                throw new AiUnavailableException("La IA devolvió una respuesta vacía.");
            }
            return value;
        } catch (JacksonException e) {
            throw new AiUnavailableException("La IA devolvió un formato no válido.", e);
        }
    }

    static String stripCodeFence(String raw) {
        String json = raw.strip();
        if (!json.startsWith(CODE_FENCE)) {
            return json;
        }
        return json.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
    }
}
