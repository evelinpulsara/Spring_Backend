package com.spring_calendario.config;

import com.spring_calendario.registros.DiaPeriodo;
import com.spring_calendario.registros.DiaPeriodoRepository;
import com.spring_calendario.registros.Sintoma;
import com.spring_calendario.registros.SintomaRepository;
import com.spring_calendario.usuarios.Usuario;
import com.spring_calendario.usuarios.UsuarioRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    public static final String DEMO_EMAIL = "demo@spring.app";

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private static final int[] CYCLE_LENGTHS = {28, 29, 27, 30, 28};
    private static final int[] PERIOD_DAYS = {5, 5, 4, 5, 6, 5};

    private final UsuarioRepository usuarios;
    private final DiaPeriodoRepository diasPeriodo;
    private final SintomaRepository sintomas;
    private final PasswordEncoder passwordEncoder;
    private final String claveDemo;

    public DemoDataSeeder(UsuarioRepository usuarios, DiaPeriodoRepository diasPeriodo,
            SintomaRepository sintomas, PasswordEncoder passwordEncoder,
            @Value("${app.demo.clave}") String claveDemo) {
        this.usuarios = usuarios;
        this.diasPeriodo = diasPeriodo;
        this.sintomas = sintomas;
        this.passwordEncoder = passwordEncoder;
        this.claveDemo = claveDemo;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarios.existsByCorreo(DEMO_EMAIL)) return;

        Usuario demo = usuarios.save(new Usuario("Luna", DEMO_EMAIL, passwordEncoder.encode(claveDemo)));
        UUID id = demo.getId();
        LocalDate today = LocalDate.now(ZONA);

        List<LocalDate> starts = new ArrayList<>();
        LocalDate start = today.minusDays(9);
        starts.add(start);
        for (int length : CYCLE_LENGTHS) {
            start = start.minusDays(length);
            starts.add(start);
        }

        for (int i = 0; i < starts.size(); i++) {
            for (int d = 0; d < PERIOD_DAYS[i]; d++) {
                diasPeriodo.save(new DiaPeriodo(id, starts.get(i).plusDays(d), "medium"));
            }
            sintomas.save(new Sintoma(id, starts.get(i), "cramps", 4));
            sintomas.save(new Sintoma(id, starts.get(i).plusDays(1), "cramps", 3));
            if (i > 0) {
                for (int d = 1; d <= 4; d++) {
                    LocalDate date = starts.get(i - 1).minusDays(d);
                    sintomas.save(new Sintoma(id, date, "bloating", 3));
                    sintomas.save(new Sintoma(id, date, "fatigue", 3));
                }
            }
        }
        sintomas.save(new Sintoma(id, today, "mood", 4));
        sintomas.save(new Sintoma(id, today, "sleep", 3));
        log.info("Cuenta de prueba creada: {}", DEMO_EMAIL);
    }
}
