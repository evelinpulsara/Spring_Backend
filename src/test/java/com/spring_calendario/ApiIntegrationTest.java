package com.spring_calendario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.spring_calendario.usuarios.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:spring-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
        "gemini.api-key=",
})
class ApiIntegrationTest {

    @Autowired WebApplicationContext context;
    @Autowired UsuarioRepository usuarios;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private String register(String email) throws Exception {
        String body = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"" + email + "\",\"password\":\"secreto123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value(email.toLowerCase()))
                .andExpect(jsonPath("$.user.cycleSetupCompleted").value(false))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void registroInicioDeSesionYPerfil() throws Exception {
        String token = register("Ana@Correo.com");

        // La contraseña se guarda cifrada.
        assertThat(usuarios.findByCorreo("ana@correo.com").orElseThrow().getContrasenaHash())
                .startsWith("$2").doesNotContain("secreto123");

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"ana@correo.com\",\"password\":\"otra1234\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("email_taken"));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@correo.com\",\"password\":\"mal\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_credentials"));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\" ANA@correo.com \",\"password\":\"secreto123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        // Cuestionario: después de responderlo queda guardado.
        mvc.perform(put("/api/me").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ana","averageCycleLength":32,"averagePeriodDuration":6,
                                 "ageGroup":"teen","regularity":"irregular","cycleSetupCompleted":true,
                                 "notifications":{"periodReminders":true,"fertileWindowAlerts":true,
                                 "dailyLogReminder":false}}"""))
                .andExpect(status().isOk());
        mvc.perform(get("/api/me").header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.cycleSetupCompleted").value(true))
                .andExpect(jsonPath("$.ageGroup").value("teen"))
                .andExpect(jsonPath("$.averagePeriodDuration").value(6))
                .andExpect(jsonPath("$.notifications.fertileWindowAlerts").value(true));
    }

    @Test
    void sinSesionNoSePuedeEntrar() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/period-days")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/me").header("Authorization", "Bearer token-falso"))
