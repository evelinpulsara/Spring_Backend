package com.spring_calendario.registros;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDate;
import java.util.UUID;

@MappedSuperclass
public abstract class RegistroDiario {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false)
    private LocalDate fecha;

    protected RegistroDiario() {}

    protected RegistroDiario(UUID usuarioId, LocalDate fecha) {
        this.id = UUID.randomUUID();
        this.usuarioId = usuarioId;
        this.fecha = fecha;
    }

    public UUID getId() { return id; }
    public UUID getUsuarioId() { return usuarioId; }
    public LocalDate getFecha() { return fecha; }
}
