package com.spring_calendario.usuarios;

import com.spring_calendario.seguridad.TokenService;
import com.spring_calendario.usuarios.dto.AuthRequests;
import com.spring_calendario.usuarios.dto.AuthRequests.AuthResponse;
import com.spring_calendario.usuarios.dto.UserDto;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, TokenService tokens) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    @Transactional
    public AuthResponse registrar(AuthRequests.Register request) {
        String correo = normalizarCorreo(request.email());
        if (usuarios.existsByCorreo(correo)) {
            throw new ApiException(HttpStatus.CONFLICT, "email_taken");
        }
        Usuario usuario = usuarios.save(new Usuario(
                request.name().trim(), correo, passwordEncoder.encode(request.password())));
        return sesionPara(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse iniciarSesion(AuthRequests.Login request) {
        return usuarios.findByCorreo(normalizarCorreo(request.email()))
                .filter(u -> u.tieneContrasena(request.password(), passwordEncoder::matches))
                .map(this::sesionPara)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "invalid_credentials"));
    }

    private AuthResponse sesionPara(Usuario usuario) {
        return new AuthResponse(tokens.createToken(usuario.getId()), UserDto.from(usuario));
    }

    static String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
