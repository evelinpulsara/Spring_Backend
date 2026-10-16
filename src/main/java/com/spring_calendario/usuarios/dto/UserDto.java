package com.spring_calendario.usuarios.dto;

import com.spring_calendario.usuarios.Usuario;

/** Datos de la usuaria que ve la app (nunca incluye la contraseña). */
public record UserDto(
        String id,
        String name,
        String email,
        int averageCycleLength,
        int averagePeriodDuration,
        String ageGroup,
        String regularity,
        boolean cycleSetupCompleted,
        Notifications notifications) {

    public record Notifications(boolean periodReminders, boolean fertileWindowAlerts, boolean dailyLogReminder) {}

    public static UserDto from(Usuario u) {
        return new UserDto(
                u.getId().toString(), u.getNombre(), u.getCorreo(),
                u.getDuracionCiclo(), u.getDuracionPeriodo(),
                u.getGrupoEdad(), u.getRegularidad(), u.isCuestionarioCompleto(),
                new Notifications(u.isNotifPeriodo(), u.isNotifVentanaFertil(), u.isNotifRegistroDiario()));
    }
}
