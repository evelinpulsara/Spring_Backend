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

