package com.spring_calendario.usuarios;

import com.spring_calendario.registros.DiaPeriodoRepository;
import com.spring_calendario.registros.RelacionRepository;
import com.spring_calendario.registros.SintomaRepository;
import com.spring_calendario.seguridad.TokenService;
import com.spring_calendario.usuarios.dto.AuthRequests;
import com.spring_calendario.usuarios.dto.UserDto;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Perfil de la usuaria con sesión iniciada: /api/me */
@RestController
@RequestMapping("/api/me")
public class PerfilController {

    private final UsuarioRepository usuarios;
    private final DiaPeriodoRepository diasPeriodo;
    private final SintomaRepository sintomas;
    private final RelacionRepository relaciones;

    public PerfilController(UsuarioRepository usuarios, DiaPeriodoRepository diasPeriodo,
            SintomaRepository sintomas, RelacionRepository relaciones) {
        this.usuarios = usuarios;
        this.diasPeriodo = diasPeriodo;
        this.sintomas = sintomas;
        this.relaciones = relaciones;
    }

    @GetMapping
    public UserDto me(Authentication auth) {
        return UserDto.from(current(auth));
    }

    @PutMapping
    @Transactional
    public UserDto update(Authentication auth, @Valid @RequestBody AuthRequests.UpdateProfile r) {
        Usuario u = current(auth);
        u.setNombre(r.name().trim());
        u.setDuracionCiclo(r.averageCycleLength());
        u.setDuracionPeriodo(r.averagePeriodDuration());
        u.setGrupoEdad(r.ageGroup());
        u.setRegularidad(r.regularity());
        u.setCuestionarioCompleto(r.cycleSetupCompleted());
        u.setNotifPeriodo(r.notifications().periodReminders());
        u.setNotifVentanaFertil(r.notifications().fertileWindowAlerts());
        u.setNotifRegistroDiario(r.notifications().dailyLogReminder());
        return UserDto.from(usuarios.save(u));
    }

