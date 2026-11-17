package com.spring_calendario.registros;

import com.spring_calendario.registros.dto.RegistroDtos.IntimacyBody;
import com.spring_calendario.registros.dto.RegistroDtos.IntimacyDto;
import com.spring_calendario.registros.dto.RegistroDtos.PeriodDayBody;
import com.spring_calendario.registros.dto.RegistroDtos.PeriodDayDto;
import com.spring_calendario.registros.dto.RegistroDtos.SymptomDto;
import com.spring_calendario.registros.dto.RegistroDtos.SymptomItem;
import com.spring_calendario.seguridad.TokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RegistrosController {

    private final RegistrosService registros;

    public RegistrosController(RegistrosService registros) {
        this.registros = registros;
    }

    @GetMapping("/period-days")
    public List<PeriodDayDto> periodDays(Authentication auth) {
        return registros.diasDePeriodo(TokenService.userId(auth));
    }

    @PutMapping("/period-days/{date}")
    public PeriodDayDto savePeriodDay(Authentication auth,
            @PathVariable @DateTimeFormat(iso = ISO.DATE) LocalDate date,
            @Valid @RequestBody PeriodDayBody body) {
        return registros.guardarDiaDePeriodo(TokenService.userId(auth), date, body.flow());
    }

    @DeleteMapping("/period-days/{date}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePeriodDay(Authentication auth, @PathVariable @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
        registros.borrarDiaDePeriodo(TokenService.userId(auth), date);
    }

    @GetMapping("/symptoms")
    public List<SymptomDto> symptoms(Authentication auth) {
        return registros.sintomas(TokenService.userId(auth));
    }

    @PutMapping("/symptoms/{date}")
    public List<SymptomDto> saveSymptoms(Authentication auth,
            @PathVariable @DateTimeFormat(iso = ISO.DATE) LocalDate date,
            @RequestBody @Size(max = 8) List<@Valid SymptomItem> items) {
        return registros.reemplazarSintomas(TokenService.userId(auth), date, items);
    }

    @GetMapping("/intimacy")
    public List<IntimacyDto> intimacy(Authentication auth) {
        return registros.relaciones(TokenService.userId(auth));
    }

    @PutMapping("/intimacy/{date}")
    public IntimacyDto saveIntimacy(Authentication auth,
            @PathVariable @DateTimeFormat(iso = ISO.DATE) LocalDate date,
            @Valid @RequestBody IntimacyBody body) {
        return registros.guardarRelacion(TokenService.userId(auth), date, body.protection());
    }

    @DeleteMapping("/intimacy/{date}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIntimacy(Authentication auth, @PathVariable @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
        registros.borrarRelacion(TokenService.userId(auth), date);
    }
}
