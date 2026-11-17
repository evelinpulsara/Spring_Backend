package com.spring_calendario.ai.gemini;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gemini")
public record GeminiProperties(
        String apiKey,
        String model,
        List<String> fallbackModels,
        String baseUrl,
        Duration timeout) {
    public List<String> modelsToTry() {
        List<String> models = new ArrayList<>();
        models.add(model);
        if (fallbackModels != null) {
            fallbackModels.stream()
                    .filter(m -> m != null && !m.isBlank() && !models.contains(m))
                    .forEach(models::add);
        }
        return models;
    }

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
