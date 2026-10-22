package com.spring_calendario.registros;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

/** Un día de menstruación registrado (tabla "dias_periodo"). */
@Entity
@Table(name = "dias_periodo")
public class DiaPeriodo {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 10)
    private String flujo;

    protected DiaPeriodo() {}

    public DiaPeriodo(UUID usuarioId, LocalDate fecha, String flujo) {
        this.id = UUID.randomUUID();
        this.usuarioId = usuarioId;
        this.fecha = fecha;
        this.flujo = flujo;
    }

    public UUID getId() { return id; }
    public UUID getUsuarioId() { return usuarioId; }
    public LocalDate getFecha() { return fecha; }
    public String getFlujo() { return flujo; }
    public void setFlujo(String flujo) { this.flujo = flujo; }
}
