package com.spring_calendario.registros;

import com.spring_calendario.registros.dto.RegistroDtos.IntimacyDto;
import com.spring_calendario.registros.dto.RegistroDtos.PeriodDayDto;
import com.spring_calendario.registros.dto.RegistroDtos.SymptomDto;
import com.spring_calendario.registros.dto.RegistroDtos.SymptomItem;
import com.spring_calendario.tiemporeal.RecordChangePublisher;
import com.spring_calendario.tiemporeal.Recurso;
import com.spring_calendario.usuarios.ApiException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegistrosService {

    private final DiaPeriodoRepository diasPeriodo;
    private final SintomaRepository sintomas;
    private final RelacionRepository relaciones;
    private final RecordChangePublisher cambios;

    public RegistrosService(DiaPeriodoRepository diasPeriodo, SintomaRepository sintomas,
            RelacionRepository relaciones, RecordChangePublisher cambios) {
        this.diasPeriodo = diasPeriodo;
        this.sintomas = sintomas;
        this.relaciones = relaciones;
        this.cambios = cambios;
    }

    @Transactional(readOnly = true)
    public List<PeriodDayDto> diasDePeriodo(UUID usuario) {
        return diasPeriodo.findByUsuarioIdOrderByFechaAsc(usuario).stream().map(PeriodDayDto::from).toList();
    }

    public PeriodDayDto guardarDiaDePeriodo(UUID usuario, LocalDate fecha, String flujo) {
        DiaPeriodo dia = diasPeriodo.findByUsuarioIdAndFecha(usuario, fecha)
                .orElseGet(() -> new DiaPeriodo(usuario, fecha, flujo));
        dia.cambiarFlujo(flujo);
        diasPeriodo.save(dia);
        cambios.recordsChanged(usuario, Recurso.PERIOD_DAYS);
        return PeriodDayDto.from(dia);
    }

    public void borrarDiaDePeriodo(UUID usuario, LocalDate fecha) {
        diasPeriodo.deleteByUsuarioIdAndFecha(usuario, fecha);
        cambios.recordsChanged(usuario, Recurso.PERIOD_DAYS);
    }

    @Transactional(readOnly = true)
    public List<SymptomDto> sintomas(UUID usuario) {
        return sintomas.findByUsuarioIdOrderByFechaAsc(usuario).stream().map(SymptomDto::from).toList();
    }

    public List<SymptomDto> reemplazarSintomas(UUID usuario, LocalDate fecha, List<SymptomItem> items) {
        exigirSinRepetidos(items);
        sintomas.deleteByUsuarioIdAndFecha(usuario, fecha);
        sintomas.flush();
        List<Sintoma> nuevos = items.stream()
                .map(item -> new Sintoma(usuario, fecha, item.type(), item.intensity()))
                .toList();
        sintomas.saveAll(nuevos);
        cambios.recordsChanged(usuario, Recurso.SYMPTOMS);
        return nuevos.stream().map(SymptomDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<IntimacyDto> relaciones(UUID usuario) {
        return relaciones.findByUsuarioIdOrderByFechaAsc(usuario).stream().map(IntimacyDto::from).toList();
    }

    public IntimacyDto guardarRelacion(UUID usuario, LocalDate fecha, String proteccion) {
        Relacion relacion = relaciones.findByUsuarioIdAndFecha(usuario, fecha)
                .orElseGet(() -> new Relacion(usuario, fecha, proteccion));
        relacion.cambiarProteccion(proteccion);
        relaciones.save(relacion);
        cambios.recordsChanged(usuario, Recurso.INTIMACY);
        return IntimacyDto.from(relacion);
    }

    public void borrarRelacion(UUID usuario, LocalDate fecha) {
        relaciones.deleteByUsuarioIdAndFecha(usuario, fecha);
        cambios.recordsChanged(usuario, Recurso.INTIMACY);
    }

    public void borrarTodo(UUID usuario) {
        diasPeriodo.deleteByUsuarioId(usuario);
        sintomas.deleteByUsuarioId(usuario);
        relaciones.deleteByUsuarioId(usuario);
        cambios.recordsChanged(usuario, Recurso.ALL);
    }

    private static void exigirSinRepetidos(List<SymptomItem> items) {
        Set<String> tipos = new HashSet<>();
        for (SymptomItem item : items) {
            if (!tipos.add(item.type())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "duplicate_symptom");
            }
        }
    }
}
