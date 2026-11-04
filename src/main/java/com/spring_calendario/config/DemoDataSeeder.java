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
