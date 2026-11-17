package com.spring_calendario.usuarios;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.function.BiPredicate;

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
    private OffsetDateTime fechaRegistro = OffsetDateTime.now(ZoneOffset.UTC);

    protected Usuario() {}

    public Usuario(String nombre, String correo, String contrasenaHash) {
        this.id = UUID.randomUUID();
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
    }

    public void actualizarPerfil(String nombre, int duracionCiclo, int duracionPeriodo,
            String grupoEdad, String regularidad, boolean cuestionarioCompleto) {
        this.nombre = nombre.trim();
        this.duracionCiclo = (short) duracionCiclo;
        this.duracionPeriodo = (short) duracionPeriodo;
        this.grupoEdad = grupoEdad;
        this.regularidad = regularidad;
        this.cuestionarioCompleto = cuestionarioCompleto;
    }

    public void configurarNotificaciones(boolean periodo, boolean ventanaFertil, boolean registroDiario) {
        this.notifPeriodo = periodo;
        this.notifVentanaFertil = ventanaFertil;
        this.notifRegistroDiario = registroDiario;
    }

    public boolean tieneContrasena(String contrasena, BiPredicate<String, String> verificador) {
        return verificador.test(contrasena, contrasenaHash);
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getContrasenaHash() { return contrasenaHash; }
    public String getGrupoEdad() { return grupoEdad; }
    public String getRegularidad() { return regularidad; }
    public int getDuracionCiclo() { return duracionCiclo; }
    public int getDuracionPeriodo() { return duracionPeriodo; }
    public boolean isCuestionarioCompleto() { return cuestionarioCompleto; }
    public boolean isNotifPeriodo() { return notifPeriodo; }
    public boolean isNotifVentanaFertil() { return notifVentanaFertil; }
    public boolean isNotifRegistroDiario() { return notifRegistroDiario; }
    public OffsetDateTime getFechaRegistro() { return fechaRegistro; }
}
