package com.spring_calendario.registros;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

/** Un síntoma registrado en un día (tabla "sintomas"). */
@Entity
@Table(name = "sintomas")
public class Sintoma {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false)
    private short intensidad;

    protected Sintoma() {}

    public Sintoma(UUID usuarioId, LocalDate fecha, String tipo, int intensidad) {
        this.id = UUID.randomUUID();
        this.usuarioId = usuarioId;
        this.fecha = fecha;
        this.tipo = tipo;
        this.intensidad = (short) intensidad;
    }

    public UUID getId() { return id; }
    public UUID getUsuarioId() { return usuarioId; }
    public LocalDate getFecha() { return fecha; }
    public String getTipo() { return tipo; }
    public int getIntensidad() { return intensidad; }
}
