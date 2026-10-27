package com.spring_calendario.ai.gemini;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuración de Gemini leída de application.properties (prefijo "gemini"). */
@ConfigurationProperties(prefix = "gemini")
public record GeminiProperties(
        String apiKey,
        String model,
        List<String> fallbackModels,
        String baseUrl,
        Duration timeout) {

