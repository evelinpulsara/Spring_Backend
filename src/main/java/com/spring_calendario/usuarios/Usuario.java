package com.spring_calendario.usuarios;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Usuaria de la app (tabla "usuarios"). */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(name = "contrasena_hash", nullable = false, length = 100)
    private String contrasenaHash;

    @Column(name = "grupo_edad", nullable = false, length = 10)
    private String grupoEdad = "unknown";

    @Column(nullable = false, length = 10)
    private String regularidad = "unsure";

    @Column(name = "duracion_ciclo", nullable = false)
    private short duracionCiclo = 28;

    @Column(name = "duracion_periodo", nullable = false)
    private short duracionPeriodo = 5;

    @Column(name = "cuestionario_completo", nullable = false)
    private boolean cuestionarioCompleto;

    @Column(name = "notif_periodo", nullable = false)
    private boolean notifPeriodo = true;

    @Column(name = "notif_ventana_fertil", nullable = false)
    private boolean notifVentanaFertil;
