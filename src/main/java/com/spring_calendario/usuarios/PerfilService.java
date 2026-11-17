package com.spring_calendario.usuarios;

import com.spring_calendario.registros.RegistrosService;
import com.spring_calendario.usuarios.dto.AuthRequests.UpdateProfile;
import com.spring_calendario.usuarios.dto.UserDto;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PerfilService {

    private final UsuarioRepository usuarios;
    private final RegistrosService registros;

    public PerfilService(UsuarioRepository usuarios, RegistrosService registros) {
        this.usuarios = usuarios;
        this.registros = registros;
    }

    @Transactional(readOnly = true)
    public UserDto perfil(UUID id) {
        return UserDto.from(buscar(id));
    }

    public UserDto actualizar(UUID id, UpdateProfile datos) {
        Usuario usuario = buscar(id);
        usuario.actualizarPerfil(datos.name(), datos.averageCycleLength(), datos.averagePeriodDuration(),
                datos.ageGroup(), datos.regularity(), datos.cycleSetupCompleted());
        var avisos = datos.notifications();
        usuario.configurarNotificaciones(
                avisos.periodReminders(), avisos.fertileWindowAlerts(), avisos.dailyLogReminder());
        return UserDto.from(usuarios.save(usuario));
    }

    public void borrarDatos(UUID id) {
        registros.borrarTodo(buscar(id).getId());
    }

    private Usuario buscar(UUID id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "session_expired"));
    }
}
