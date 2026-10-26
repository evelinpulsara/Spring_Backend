package com.spring_calendario.registros;

import com.spring_calendario.seguridad.TokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Registros de la usuaria con sesión iniciada. Cada usuaria solo ve y cambia
 * sus propios datos (el id sale del token, nunca de la petición).
 *
 * <pre>
 * GET    /api/period-days            PUT /api/period-days/{fecha}   DELETE /api/period-days/{fecha}
 * GET    /api/symptoms               PUT /api/symptoms/{fecha}  (reemplaza los síntomas del día)
 * GET    /api/intimacy               PUT /api/intimacy/{fecha}      DELETE /api/intimacy/{fecha}
 * </pre>
 */
@RestController
@RequestMapping("/api")
public class RegistrosController {

    private final DiaPeriodoRepository diasPeriodo;
    private final SintomaRepository sintomas;
    private final RelacionRepository relaciones;

    public RegistrosController(DiaPeriodoRepository diasPeriodo, SintomaRepository sintomas,
            RelacionRepository relaciones) {
        this.diasPeriodo = diasPeriodo;
        this.sintomas = sintomas;
        this.relaciones = relaciones;
    }

    // ── Días de periodo ──────────────────────────────────────────────────────

    public record PeriodDayDto(LocalDate date, String flow) {}

    public record PeriodDayBody(@NotNull @Pattern(regexp = "none|light|medium|heavy") String flow) {}

    @GetMapping("/period-days")
    public List<PeriodDayDto> periodDays(Authentication auth) {
        return diasPeriodo.findByUsuarioIdOrderByFechaAsc(user(auth)).stream()
                .map(d -> new PeriodDayDto(d.getFecha(), d.getFlujo())).toList();
    }

    @PutMapping("/period-days/{date}")
    @Transactional
    public PeriodDayDto savePeriodDay(Authentication auth, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody PeriodDayBody body) {
        UUID id = user(auth);
        DiaPeriodo dia = diasPeriodo.findByUsuarioIdAndFecha(id, date)
                .orElseGet(() -> new DiaPeriodo(id, date, body.flow()));
        dia.setFlujo(body.flow());
        diasPeriodo.save(dia);
        return new PeriodDayDto(date, body.flow());
    }

    @DeleteMapping("/period-days/{date}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePeriodDay(Authentication auth, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        diasPeriodo.deleteByUsuarioIdAndFecha(user(auth), date);
    }

    // ── Síntomas ─────────────────────────────────────────────────────────────
