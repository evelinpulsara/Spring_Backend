package com.spring_calendario.registros;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface RegistroDiarioRepository<T extends RegistroDiario> extends JpaRepository<T, UUID> {

    List<T> findByUsuarioIdOrderByFechaAsc(UUID usuarioId);

    @Transactional
    void deleteByUsuarioIdAndFecha(UUID usuarioId, LocalDate fecha);

    @Transactional
    void deleteByUsuarioId(UUID usuarioId);
}
