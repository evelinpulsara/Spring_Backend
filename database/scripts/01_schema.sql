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
