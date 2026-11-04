package com.spring_calendario.config;

import com.spring_calendario.registros.DiaPeriodo;
import com.spring_calendario.registros.DiaPeriodoRepository;
import com.spring_calendario.registros.Sintoma;
import com.spring_calendario.registros.SintomaRepository;
import com.spring_calendario.usuarios.Usuario;
import com.spring_calendario.usuarios.UsuarioRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea la cuenta de prueba demo@spring.app (contraseña Spring1234) con unos
 * meses de datos de ejemplo, solo si todavía no existe.
 * Se desactiva con DEMO_ENABLED=false.
 */
@Component
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    public static final String DEMO_EMAIL = "demo@spring.app";
    public static final String DEMO_PASSWORD = "Spring1234";

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    /** Duración de cada ciclo pasado, del más nuevo al más viejo. */
    private static final int[] CYCLE_LENGTHS = {28, 29, 27, 30, 28};
    /** Días de sangrado de cada ciclo, del más nuevo al más viejo. */
    private static final int[] PERIOD_DAYS = {5, 5, 4, 5, 6, 5};

    private final UsuarioRepository usuarios;
    private final DiaPeriodoRepository diasPeriodo;
    private final SintomaRepository sintomas;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(UsuarioRepository usuarios, DiaPeriodoRepository diasPeriodo,
            SintomaRepository sintomas, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.diasPeriodo = diasPeriodo;
        this.sintomas = sintomas;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarios.existsByCorreo(DEMO_EMAIL)) return;

        // Como usuaria nueva: el cuestionario del ciclo aún no está respondido.
        Usuario demo = usuarios.save(new Usuario("Luna", DEMO_EMAIL, passwordEncoder.encode(DEMO_PASSWORD)));
        UUID id = demo.getId();
        LocalDate today = LocalDate.now();
