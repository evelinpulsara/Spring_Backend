package com.spring_calendario.tiemporeal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:realtime-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
        "gemini.api-key=",
})
class RealtimeTest {

    @Autowired WebApplicationContext context;
    @Autowired EventBus bus;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private String register(String email) throws Exception {
        String body = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"" + email + "\",\"password\":\"secreto123\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.token");
    }

    private MockHttpServletResponse openStream(String bearer) throws Exception {
        String ticketJson = mvc.perform(post("/api/events/ticket").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String ticket = JsonPath.read(ticketJson, "$.ticket");
        return mvc.perform(get("/api/events/stream").param("ticket", ticket))
                .andExpect(request().asyncStarted())
                .andReturn().getResponse();
    }

    @Test
    void elCambioLlegaATodasLasConexionesDeLaUsuariaYSoloAEllas() throws Exception {
        String ana = register("ana-rt@correo.com");
        String bea = register("bea-rt@correo.com");

        MockHttpServletResponse celular = openStream(ana);
        MockHttpServletResponse computadora = openStream(ana);
        MockHttpServletResponse otra = openStream(bea);

        mvc.perform(put("/api/period-days/2026-11-01").header("Authorization", ana)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"flow\":\"heavy\"}"))
                .andExpect(status().isOk());

        assertThat(celular.getContentAsString())
                .contains("event:connected")
                .contains("event:records-changed")
                .contains("\"resource\":\"period-days\"");
        assertThat(computadora.getContentAsString()).contains("event:records-changed");
        assertThat(otra.getContentAsString()).doesNotContain("records-changed");
    }

    @Test
    void sinTicketValidoNoSeAbreElStream() throws Exception {
        mvc.perform(get("/api/events/stream").param("ticket", "inventado"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/events/ticket")).andExpect(status().isUnauthorized());
    }

    @Test
    void elTicketSirveUnaSolaVezYVence() {
        UUID id = UUID.randomUUID();
        var tickets = new StreamTicketService();
        String t = tickets.issue(id);
        assertThat(tickets.redeem(t)).contains(id);
        assertThat(tickets.redeem(t)).isEmpty();

        Instant[] ahora = {Instant.parse("2026-01-01T00:00:00Z")};
        var conReloj = new StreamTicketService(() -> ahora[0]);
        String viejo = conReloj.issue(id);
        ahora[0] = ahora[0].plusSeconds(31);
        assertThat(conReloj.redeem(viejo)).isEmpty();
    }
}
