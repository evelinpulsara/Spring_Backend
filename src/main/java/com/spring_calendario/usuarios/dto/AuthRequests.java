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
