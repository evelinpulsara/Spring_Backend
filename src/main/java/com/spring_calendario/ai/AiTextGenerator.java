package com.spring_calendario.ai;

import java.util.Map;

/**
 * Contrato para el modelo de IA. La implementación real es GeminiClient;
 * en los tests se usa una versión falsa para no gastar llamadas reales.
 */
public interface AiTextGenerator {

    /**
     * Envía las instrucciones y el mensaje al modelo y devuelve su respuesta
     * en JSON con la forma {"title": "...", "message": "..."}, junto con el
     * nombre del modelo que respondió.
     */
    GeneratedText generateJson(String systemInstruction, String userPrompt, Map<String, Object> responseSchema);

    /** Texto generado y modelo que lo generó. */
    record GeneratedText(String text, String model) {}
}
