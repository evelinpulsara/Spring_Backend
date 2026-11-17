package com.spring_calendario.registros;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface RelacionRepository extends RegistroDiarioRepository<Relacion> {

    Optional<Relacion> findByUsuarioIdAndFecha(UUID usuarioId, LocalDate fecha);
}
