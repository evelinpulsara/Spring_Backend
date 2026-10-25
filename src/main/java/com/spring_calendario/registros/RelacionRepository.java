package com.spring_calendario.registros;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface RelacionRepository extends JpaRepository<Relacion, UUID> {
    List<Relacion> findByUsuarioIdOrderByFechaAsc(UUID usuarioId);
    Optional<Relacion> findByUsuarioIdAndFecha(UUID usuarioId, LocalDate fecha);
    @Transactional void deleteByUsuarioIdAndFecha(UUID usuarioId, LocalDate fecha);
    @Transactional void deleteByUsuarioId(UUID usuarioId);
}
