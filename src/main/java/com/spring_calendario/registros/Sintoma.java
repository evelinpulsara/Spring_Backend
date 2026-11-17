package com.spring_calendario.registros;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "sintomas")
public class Sintoma extends RegistroDiario {

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false)
    private short intensidad;

    protected Sintoma() {}

    public Sintoma(UUID usuarioId, LocalDate fecha, String tipo, int intensidad) {
        super(usuarioId, fecha);
        this.tipo = tipo;
        this.intensidad = (short) intensidad;
    }

    public String getTipo() { return tipo; }
    public int getIntensidad() { return intensidad; }
}
