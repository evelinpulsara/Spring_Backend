package com.spring_calendario.tiemporeal;

public enum Recurso {
    PERIOD_DAYS("period-days"),
    SYMPTOMS("symptoms"),
    INTIMACY("intimacy"),
    ALL("all");

    private final String nombre;

    Recurso(String nombre) {
        this.nombre = nombre;
    }

    public String nombre() {
        return nombre;
    }
}
