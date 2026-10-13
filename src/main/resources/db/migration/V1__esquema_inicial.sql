-- =============================================================================
-- Spring Calendario - Esquema de la base de datos (PostgreSQL / Supabase)
-- Lo aplica Flyway automáticamente al iniciar el backend.
-- AVISO: la app NO emite diagnósticos médicos.
-- =============================================================================

-- Usuarias y las respuestas del cuestionario inicial del ciclo.
CREATE TABLE usuarios (
    id                     UUID          PRIMARY KEY,
    nombre                 VARCHAR(100)  NOT NULL,
    correo                 VARCHAR(255)  NOT NULL UNIQUE,
    contrasena_hash        VARCHAR(100)  NOT NULL,           -- BCrypt, nunca texto plano
    grupo_edad             VARCHAR(10)   NOT NULL DEFAULT 'unknown'
                           CHECK (grupo_edad IN ('teen', 'adult', 'unknown')),
    regularidad            VARCHAR(10)   NOT NULL DEFAULT 'unsure'
                           CHECK (regularidad IN ('regular', 'irregular', 'unsure')),
    duracion_ciclo         SMALLINT      NOT NULL DEFAULT 28 CHECK (duracion_ciclo BETWEEN 15 AND 60),
    duracion_periodo       SMALLINT      NOT NULL DEFAULT 5  CHECK (duracion_periodo BETWEEN 1 AND 15),
    cuestionario_completo  BOOLEAN       NOT NULL DEFAULT FALSE,
    notif_periodo          BOOLEAN       NOT NULL DEFAULT TRUE,
    notif_ventana_fertil   BOOLEAN       NOT NULL DEFAULT FALSE,
