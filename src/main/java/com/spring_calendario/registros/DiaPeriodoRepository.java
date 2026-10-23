package com.spring_calendario.registros;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface DiaPeriodoRepository extends JpaRepository<DiaPeriodo, UUID> {
    List<DiaPeriodo> findByUsuarioIdOrderByFechaAsc(UUID usuarioId);
    Optional<DiaPeriodo> findByUsuarioIdAndFecha(UUID usuarioId, LocalDate fecha);
    @Transactional void deleteByUsuarioIdAndFecha(UUID usuarioId, LocalDate fecha);
    @Transactional void deleteByUsuarioId(UUID usuarioId);
}
