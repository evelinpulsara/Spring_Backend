-- =============================================================================
-- LunaFlow - Gestión del Ciclo Menstrual y Bienestar Diario
-- Script 01: Esquema de Base de Datos (DDL)
-- Base de datos: PostgreSQL 15+
-- AVISO: Esta aplicación NO emite diagnósticos médicos.
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================================================
-- TABLA: usuarios
-- =============================================================================
CREATE TABLE usuarios (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre              VARCHAR(100)    NOT NULL,
    correo              VARCHAR(255)    NOT NULL UNIQUE,
    contrasena_hash     VARCHAR(255)    NOT NULL,
    zona_horaria        VARCHAR(50)     NOT NULL DEFAULT 'America/Mexico_City',
    activo              BOOLEAN         NOT NULL DEFAULT TRUE,
    fecha_registro      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    ultimo_acceso       TIMESTAMPTZ,
    CONSTRAINT chk_correo_formato CHECK (correo ~* '^[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_nombre_longitud CHECK (char_length(nombre) >= 2)
);

COMMENT ON TABLE  usuarios IS 'Cuentas de usuarias. Contraseña solo como hash bcrypt.';
COMMENT ON COLUMN usuarios.contrasena_hash IS 'Hash bcrypt generado por Spring Security. NUNCA texto plano.';

-- =============================================================================
-- TABLA: preferencias_usuario (1:1 con usuarios)
-- =============================================================================
CREATE TABLE preferencias_usuario (
    id                          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id                  UUID        NOT NULL UNIQUE,
    duracion_ciclo_esperada     SMALLINT    DEFAULT 28 CHECK (duracion_ciclo_esperada BETWEEN 15 AND 60),
    duracion_periodo_esperada   SMALLINT    DEFAULT 5  CHECK (duracion_periodo_esperada BETWEEN 1 AND 15),
    notificaciones_activas      BOOLEAN     DEFAULT TRUE,
    perfil_publico              BOOLEAN     NOT NULL DEFAULT FALSE,
    idioma                      VARCHAR(10) NOT NULL DEFAULT 'es',
    tema_interfaz               VARCHAR(20) DEFAULT 'dark' CHECK (tema_interfaz IN ('light','dark','system')),
    primera_vez_app             BOOLEAN     DEFAULT TRUE,
    fecha_actualizacion         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_pref_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE
);

-- =============================================================================
-- TABLA: roles (para Spring Security)
-- =============================================================================
CREATE TABLE roles (
    id      SMALLSERIAL PRIMARY KEY,
    nombre  VARCHAR(50) NOT NULL UNIQUE
);

-- =============================================================================
-- TABLA: usuario_roles (N:M)
-- =============================================================================
CREATE TABLE usuario_roles (
    usuario_id  UUID        NOT NULL,
    rol_id      SMALLINT    NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_ur_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_rol FOREIGN KEY (rol_id)
        REFERENCES roles(id) ON DELETE RESTRICT
);

-- =============================================================================
-- TABLA: ciclos_menstruales
-- =============================================================================
CREATE TABLE ciclos_menstruales (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id          UUID        NOT NULL,
    fecha_inicio        DATE        NOT NULL,
    fecha_fin_periodo   DATE,
    fecha_fin_ciclo     DATE,
    duracion_periodo    SMALLINT    CHECK (duracion_periodo BETWEEN 1 AND 20),
    duracion_ciclo      SMALLINT    CHECK (duracion_ciclo BETWEEN 15 AND 90),
    regularidad         VARCHAR(20) DEFAULT 'desconocida'
                            CHECK (regularidad IN ('regular','irregular','desconocida')),
    notas               TEXT,
    fecha_creacion      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_ciclo_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT chk_fechas_periodo CHECK (
        fecha_fin_periodo IS NULL OR fecha_fin_periodo >= fecha_inicio
    ),
    CONSTRAINT chk_fechas_ciclo CHECK (
        fecha_fin_ciclo IS NULL OR fecha_fin_ciclo > fecha_inicio
    ),
    CONSTRAINT uq_ciclo_inicio UNIQUE (usuario_id, fecha_inicio)
);

COMMENT ON TABLE ciclos_menstruales IS 'Historial de ciclos. Un registro por ciclo completo.';

-- =============================================================================
-- TABLA: tipos_sintoma (catálogo)
-- =============================================================================
CREATE TABLE tipos_sintoma (
    id          SMALLSERIAL  PRIMARY KEY,
    categoria   VARCHAR(50)  NOT NULL,
    nombre      VARCHAR(100) NOT NULL UNIQUE,
