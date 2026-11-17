package com.spring_calendario.ai;

import java.util.Map;

public interface AiTextGenerator {

    GeneratedText generateJson(String systemInstruction, String userPrompt, Map<String, Object> responseSchema);

    record GeneratedText(String text, String model) {}
}
