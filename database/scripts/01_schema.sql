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
    descripcion TEXT,
    icono       VARCHAR(50)
);

-- =============================================================================
-- TABLA: registros_diarios
-- =============================================================================
CREATE TABLE registros_diarios (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id          UUID         NOT NULL,
    ciclo_id            UUID,
    fecha               DATE         NOT NULL,
    nivel_energia       SMALLINT     CHECK (nivel_energia BETWEEN 1 AND 10),
    nivel_estres        SMALLINT     CHECK (nivel_estres  BETWEEN 1 AND 10),
    calidad_sueno       SMALLINT     CHECK (calidad_sueno BETWEEN 1 AND 10),
    horas_sueno         NUMERIC(4,1) CHECK (horas_sueno  BETWEEN 0 AND 24),
    hay_sangrado        BOOLEAN      DEFAULT FALSE,
    intensidad_sangrado VARCHAR(20)  CHECK (intensidad_sangrado IN ('leve','moderado','abundante','muy_abundante')),
    estado_animo_valor  SMALLINT     CHECK (estado_animo_valor BETWEEN 1 AND 10),
    estado_animo_etiq   VARCHAR(50),
    notas               TEXT,
    fecha_creacion      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_rd_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_rd_ciclo FOREIGN KEY (ciclo_id)
        REFERENCES ciclos_menstruales(id) ON DELETE SET NULL,
    CONSTRAINT uq_registro_diario UNIQUE (usuario_id, fecha)
);

COMMENT ON TABLE registros_diarios IS 'Un registro de bienestar por usuaria por día.';

-- =============================================================================
-- TABLA: sintomas_registro
-- =============================================================================
CREATE TABLE sintomas_registro (
    id              UUID     PRIMARY KEY DEFAULT gen_random_uuid(),
    registro_id     UUID     NOT NULL,
    tipo_sintoma_id SMALLINT NOT NULL,
    intensidad      SMALLINT NOT NULL CHECK (intensidad BETWEEN 1 AND 5),
    momento_dia     VARCHAR(20) CHECK (momento_dia IN ('manana','tarde','noche','todo_el_dia')),
    CONSTRAINT fk_sr_registro  FOREIGN KEY (registro_id)
        REFERENCES registros_diarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_sr_tipo      FOREIGN KEY (tipo_sintoma_id)
        REFERENCES tipos_sintoma(id) ON DELETE RESTRICT,
    CONSTRAINT uq_sintoma_dia  UNIQUE (registro_id, tipo_sintoma_id, momento_dia)
);

COMMENT ON COLUMN sintomas_registro.intensidad IS '1=muy leve, 2=leve, 3=moderado, 4=intenso, 5=muy intenso.';

-- =============================================================================
-- TABLA: predicciones_ciclo
-- =============================================================================
CREATE TABLE predicciones_ciclo (
    id                          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id                  UUID        NOT NULL,
    fecha_generacion            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    fecha_inicio_estimada       DATE        NOT NULL,
    fecha_inicio_rango_min      DATE        NOT NULL,
    fecha_inicio_rango_max      DATE        NOT NULL,
    fecha_ovulacion_estimada    DATE,
    ciclos_analizados           SMALLINT    NOT NULL DEFAULT 0,
    confianza_pct               SMALLINT    CHECK (confianza_pct BETWEEN 0 AND 100),
    algoritmo_version           VARCHAR(20) DEFAULT 'v1.0',
    aviso_legal                 TEXT        NOT NULL DEFAULT
        'ESTIMACIÓN ORIENTATIVA basada en historial. NO es diagnóstico médico. Consulta a un profesional de salud.',
    CONSTRAINT fk_pred_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT chk_rango CHECK (
