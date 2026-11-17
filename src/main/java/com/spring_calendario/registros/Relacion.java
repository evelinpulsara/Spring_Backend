package com.spring_calendario.registros;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "relaciones")
public class Relacion extends RegistroDiario {

    @Column(nullable = false, length = 15)
    private String proteccion;

    protected Relacion() {}

    public Relacion(UUID usuarioId, LocalDate fecha, String proteccion) {
        super(usuarioId, fecha);
        this.proteccion = proteccion;
    }

    public String getProteccion() { return proteccion; }

    public void cambiarProteccion(String proteccion) {
        this.proteccion = proteccion;
    }
}
