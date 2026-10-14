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
    notif_registro_diario  BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_registro         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Días de menstruación registrados (uno por día).
CREATE TABLE dias_periodo (
    id          UUID         PRIMARY KEY,
    usuario_id  UUID         NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    fecha       DATE         NOT NULL,
    flujo       VARCHAR(10)  NOT NULL DEFAULT 'medium'
                CHECK (flujo IN ('none', 'light', 'medium', 'heavy')),
    CONSTRAINT uq_dia_periodo UNIQUE (usuario_id, fecha)
);

-- Síntomas por día con intensidad de 1 a 5.
CREATE TABLE sintomas (
    id          UUID         PRIMARY KEY,
    usuario_id  UUID         NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    fecha       DATE         NOT NULL,
    tipo        VARCHAR(20)  NOT NULL
                CHECK (tipo IN ('cramps', 'headache', 'mood', 'bloating',
                                'acne', 'fatigue', 'appetite', 'sleep')),
    intensidad  SMALLINT     NOT NULL CHECK (intensidad BETWEEN 1 AND 5),
    CONSTRAINT uq_sintoma_dia UNIQUE (usuario_id, fecha, tipo)
);

-- Relaciones sexuales registradas (privado) y el tipo de protección.
CREATE TABLE relaciones (
    id          UUID         PRIMARY KEY,
    usuario_id  UUID         NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    fecha       DATE         NOT NULL,
    proteccion  VARCHAR(15)  NOT NULL
                CHECK (proteccion IN ('none', 'condom', 'contraceptive', 'unsure')),
    CONSTRAINT uq_relacion_dia UNIQUE (usuario_id, fecha)
);

CREATE INDEX idx_dias_periodo_usuario ON dias_periodo (usuario_id, fecha);
CREATE INDEX idx_sintomas_usuario     ON sintomas (usuario_id, fecha);
CREATE INDEX idx_relaciones_usuario   ON relaciones (usuario_id, fecha);
