package com.spring_calendario.usuarios.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Cuerpos de las peticiones de cuenta. */
public final class AuthRequests {

    private AuthRequests() {}

    public record Register(
            @NotBlank @Size(min = 2, max = 100) String name,
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(min = 6, max = 72) String password) {}

    public record Login(@NotBlank String email, @NotBlank String password) {}

    /** Respuesta de registro / inicio de sesión. */
    public record AuthResponse(String token, UserDto user) {}

    /** Actualización del perfil y del cuestionario del ciclo. */
    public record UpdateProfile(
            @NotBlank @Size(min = 2, max = 100) String name,
            @Min(15) @Max(60) int averageCycleLength,
            @Min(1) @Max(15) int averagePeriodDuration,
            @NotNull @Pattern(regexp = "teen|adult|unknown") String ageGroup,
            @NotNull @Pattern(regexp = "regular|irregular|unsure") String regularity,
            boolean cycleSetupCompleted,
            @NotNull @Valid UserDto.Notifications notifications) {}
}
