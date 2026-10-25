package com.spring_calendario.registros;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

/** Una relación sexual registrada y su protección (tabla "relaciones"). */
@Entity
@Table(name = "relaciones")
public class Relacion {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 15)
    private String proteccion;

    protected Relacion() {}

    public Relacion(UUID usuarioId, LocalDate fecha, String proteccion) {
        this.id = UUID.randomUUID();
        this.usuarioId = usuarioId;
        this.fecha = fecha;
        this.proteccion = proteccion;
    }

    public UUID getId() { return id; }
    public UUID getUsuarioId() { return usuarioId; }
    public LocalDate getFecha() { return fecha; }
    public String getProteccion() { return proteccion; }
    public void setProteccion(String proteccion) { this.proteccion = proteccion; }
}
