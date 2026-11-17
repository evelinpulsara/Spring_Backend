package com.spring_calendario.registros;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "dias_periodo")
public class DiaPeriodo extends RegistroDiario {

    @Column(nullable = false, length = 10)
    private String flujo;

    protected DiaPeriodo() {}

    public DiaPeriodo(UUID usuarioId, LocalDate fecha, String flujo) {
        super(usuarioId, fecha);
        this.flujo = flujo;
    }

    public String getFlujo() { return flujo; }

    public void cambiarFlujo(String flujo) {
        this.flujo = flujo;
    }
}
