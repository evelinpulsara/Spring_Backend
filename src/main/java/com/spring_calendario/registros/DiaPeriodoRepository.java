package com.spring_calendario.registros;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DiaPeriodoRepository extends RegistroDiarioRepository<DiaPeriodo> {

    Optional<DiaPeriodo> findByUsuarioIdAndFecha(UUID usuarioId, LocalDate fecha);
}
