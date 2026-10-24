package com.spring_calendario.registros;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface SintomaRepository extends JpaRepository<Sintoma, UUID> {
    List<Sintoma> findByUsuarioIdOrderByFechaAsc(UUID usuarioId);
    @Transactional void deleteByUsuarioIdAndFecha(UUID usuarioId, LocalDate fecha);
    @Transactional void deleteByUsuarioId(UUID usuarioId);
}
