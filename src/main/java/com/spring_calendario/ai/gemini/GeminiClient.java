package com.spring_calendario.ai.gemini;

import com.spring_calendario.ai.AiTextGenerator;
import com.spring_calendario.ai.AiUnavailableException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Llama a la API de Gemini (generateContent) usando la clave guardada en el backend.
 * La clave nunca se envía a la app Flutter.
 */
@Component
public class GeminiClient implements AiTextGenerator {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private final GeminiProperties properties;
    private final RestClient restClient;

    public GeminiClient(GeminiProperties properties) {
        this.properties = properties;
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.timeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.timeout());
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public GeneratedText generateJson(
            String systemInstruction, String userPrompt, Map<String, Object> responseSchema) {
        if (!properties.hasApiKey()) {
            throw new AiUnavailableException("Falta configurar GEMINI_API_KEY en el backend.");
        }

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", systemInstruction))),
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", userPrompt)))),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "responseJsonSchema", responseSchema,
                        "thinkingConfig", Map.of("thinkingLevel", "LOW")));

        RuntimeException lastError = null;
        for (String model : properties.modelsToTry()) {
            try {
                GeminiResponse response = restClient.post()
                        .uri("/models/{model}:generateContent", model)
                        .header("x-goog-api-key", properties.apiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(GeminiResponse.class);
                String text = response == null ? null : response.firstText();
                if (text != null && !text.isBlank()) {
                    return new GeneratedText(text, model);
                }
                lastError = new AiUnavailableException("Gemini devolvió una respuesta vacía.");
                log.warn("Gemini ({}) devolvió una respuesta vacía", model);
            } catch (RestClientResponseException e) {
                if (!isRetryable(e.getStatusCode().value())) {
                    log.warn("Gemini ({}) rechazó la solicitud: HTTP {}", model, e.getStatusCode().value());
                    throw new AiUnavailableException("Gemini rechazó la solicitud.", e);
                }
                log.warn("Gemini ({}) no disponible: HTTP {}. Se prueba otro modelo.", model, e.getStatusCode().value());
                lastError = e;
            } catch (RestClientException e) {
                log.warn("Gemini ({}) no respondió: {}. Se prueba otro modelo.", model, e.getMessage());
                lastError = e;
            }
        }
        throw new AiUnavailableException("No se pudo contactar a Gemini.", lastError);
    }

    /** Errores temporales o de modelo no disponible: vale la pena probar otro modelo. */
    private static boolean isRetryable(int status) {
        return status == 404 || status == 408 || status == 429 || status >= 500;
    }

    /** Solo los campos de la respuesta de Gemini que usamos. */
    record GeminiResponse(List<Candidate> candidates) {
        String firstText() {
            if (candidates == null) return null;
            for (Candidate candidate : candidates) {
                if (candidate.content() == null || candidate.content().parts() == null) continue;
                StringBuilder text = new StringBuilder();
                for (Part part : candidate.content().parts()) {
                    if (part.text() != null && !Boolean.TRUE.equals(part.thought())) {
                        text.append(part.text());
                    }
                }
                if (!text.isEmpty()) return text.toString();
            }
            return null;
        }
    }

    record Candidate(Content content) {}

    record Content(List<Part> parts) {}

    record Part(String text, Boolean thought) {}
}
