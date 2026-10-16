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

    @Column(name = "notif_registro_diario", nullable = false)
    private boolean notifRegistroDiario = true;

    @Column(name = "fecha_registro", nullable = false)
    private OffsetDateTime fechaRegistro = OffsetDateTime.now();

    protected Usuario() {}

    public Usuario(String nombre, String correo, String contrasenaHash) {
        this.id = UUID.randomUUID();
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public String getContrasenaHash() { return contrasenaHash; }
    public String getGrupoEdad() { return grupoEdad; }
    public void setGrupoEdad(String grupoEdad) { this.grupoEdad = grupoEdad; }
    public String getRegularidad() { return regularidad; }
    public void setRegularidad(String regularidad) { this.regularidad = regularidad; }
    public int getDuracionCiclo() { return duracionCiclo; }
    public void setDuracionCiclo(int dias) { this.duracionCiclo = (short) dias; }
    public int getDuracionPeriodo() { return duracionPeriodo; }
    public void setDuracionPeriodo(int dias) { this.duracionPeriodo = (short) dias; }
    public boolean isCuestionarioCompleto() { return cuestionarioCompleto; }
    public void setCuestionarioCompleto(boolean completo) { this.cuestionarioCompleto = completo; }
    public boolean isNotifPeriodo() { return notifPeriodo; }
    public void setNotifPeriodo(boolean v) { this.notifPeriodo = v; }
    public boolean isNotifVentanaFertil() { return notifVentanaFertil; }
    public void setNotifVentanaFertil(boolean v) { this.notifVentanaFertil = v; }
    public boolean isNotifRegistroDiario() { return notifRegistroDiario; }
    public void setNotifRegistroDiario(boolean v) { this.notifRegistroDiario = v; }
    public OffsetDateTime getFechaRegistro() { return fechaRegistro; }
}
