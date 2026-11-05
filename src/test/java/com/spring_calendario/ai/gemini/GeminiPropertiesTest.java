package com.spring_calendario.ai.gemini;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class GeminiPropertiesTest {

    @Test
    void pruebaPrimeroElModeloPrincipalYLuegoLosDeRespaldoSinRepetir() {
        GeminiProperties props = new GeminiProperties("k", "gemini-a",
                List.of("gemini-b", "gemini-a", " ", "gemini-c"), "http://x", Duration.ofSeconds(1));

        assertThat(props.modelsToTry()).containsExactly("gemini-a", "gemini-b", "gemini-c");
    }

    @Test
    void sinClaveNoHayIa() {
        GeminiProperties props = new GeminiProperties(" ", "m", null, "http://x", Duration.ofSeconds(1));
        assertThat(props.hasApiKey()).isFalse();
        assertThat(props.modelsToTry()).containsExactly("m");
    }
}
