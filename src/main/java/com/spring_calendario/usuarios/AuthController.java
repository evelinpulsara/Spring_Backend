package com.spring_calendario.usuarios;

import com.spring_calendario.seguridad.TokenService;
import com.spring_calendario.usuarios.dto.AuthRequests;
import com.spring_calendario.usuarios.dto.AuthRequests.AuthResponse;
import com.spring_calendario.usuarios.dto.UserDto;
import jakarta.validation.Valid;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Registro e inicio de sesión: POST /api/auth/register y /api/auth/login */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;

    public AuthController(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, TokenService tokens) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody AuthRequests.Register request) {
        String email = normalize(request.email());
        if (usuarios.existsByCorreo(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "email_taken");
        }
        Usuario usuario = usuarios.save(new Usuario(
                request.name().trim(), email, passwordEncoder.encode(request.password())));
        return new AuthResponse(tokens.createToken(usuario.getId()), UserDto.from(usuario));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequests.Login request) {
        Usuario usuario = usuarios.findByCorreo(normalize(request.email()))
                .filter(u -> passwordEncoder.matches(request.password(), u.getContrasenaHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "invalid_credentials"));
        return new AuthResponse(tokens.createToken(usuario.getId()), UserDto.from(usuario));
    }

    static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
