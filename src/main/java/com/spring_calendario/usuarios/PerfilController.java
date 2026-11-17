package com.spring_calendario.usuarios;

import com.spring_calendario.seguridad.TokenService;
import com.spring_calendario.usuarios.dto.AuthRequests;
import com.spring_calendario.usuarios.dto.UserDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class PerfilController {

    private final PerfilService perfiles;

    public PerfilController(PerfilService perfiles) {
        this.perfiles = perfiles;
    }

    @GetMapping
    public UserDto me(Authentication auth) {
        return perfiles.perfil(TokenService.userId(auth));
    }

    @PutMapping
    public UserDto update(Authentication auth, @Valid @RequestBody AuthRequests.UpdateProfile request) {
        return perfiles.actualizar(TokenService.userId(auth), request);
    }

    @DeleteMapping("/data")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteData(Authentication auth) {
        perfiles.borrarDatos(TokenService.userId(auth));
    }
}
