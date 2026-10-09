-- =============================================================================
-- LunaFlow - Script 02: Datos de ejemplo ficticios
-- AVISO: Todos los datos son FICTICIOS para pruebas académicas.
-- Las contraseñas mostradas son ficticias; en producción usa Spring Security.
-- =============================================================================

-- Roles del sistema
INSERT INTO roles (nombre) VALUES
    ('ROLE_USER'),
    ('ROLE_ADMIN');

-- Tipos de síntoma (catálogo base)
INSERT INTO tipos_sintoma (categoria, nombre, descripcion, icono) VALUES
    ('dolor',       'Calambres',          'Dolor tipo cólico en parte baja del abdomen', 'cramp'),
    ('dolor',       'Dolor de cabeza',    'Cefalea tensional o migraña',                  'headache'),
    ('dolor',       'Dolor de espalda',   'Molestia en zona lumbar',                      'back_pain'),
    ('dolor',       'Sensibilidad senos', 'Tensión o sensibilidad mamaria',               'breast'),
    ('fisico',      'Hinchazón',          'Sensación de distensión abdominal',            'bloating'),
    ('fisico',      'Acné',               'Aparición de granos o brotes en piel',         'acne'),
    ('fisico',      'Fatiga',             'Cansancio generalizado o agotamiento',         'fatigue'),
    ('fisico',      'Náuseas',            'Malestar gástrico y ganas de vomitar',         'nausea'),
    ('estado_animo','Irritabilidad',      'Mayor sensibilidad emocional o irritación',    'irritable'),
    ('estado_animo','Ansiedad',           'Sensación de nerviosismo o inquietud',         'anxiety'),
    ('estado_animo','Tristeza',           'Estado de ánimo bajo o melancólico',           'sad'),
    ('estado_animo','Cambios de ánimo',   'Variaciones rápidas del estado emocional',     'mood_swing'),
    ('sueno',       'Insomnio',           'Dificultad para conciliar o mantener el sueño','insomnia'),
    ('sueno',       'Hipersomnia',        'Exceso de sueño o somnolencia diurna',         'oversleep'),
    ('fisico',      'Antojos',            'Deseos intensos de ciertos alimentos',         'craving'),
    ('fisico',      'Retención de líquidos','Sensación de pesadez o edema leve',          'water_retention');

-- ============================================================
-- Usuario ficticio 1: Ana García
-- Contraseña: "Password123!" -> En producción Spring Security
-- genera el hash. Este hash bcrypt es solo de ejemplo.
-- $2a$12$... = bcrypt con cost factor 12
-- ============================================================
INSERT INTO usuarios (id, nombre, correo, contrasena_hash, zona_horaria)
VALUES (
    'a1b2c3d4-0000-0000-0000-000000000001',
    'Ana García López',
    'ana.garcia@ejemplo.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8Pi3/5vY.oT3pTpU0S2',
    'America/Mexico_City'
);

INSERT INTO preferencias_usuario (usuario_id, duracion_ciclo_esperada, duracion_periodo_esperada, idioma)
VALUES ('a1b2c3d4-0000-0000-0000-000000000001', 28, 5, 'es');

INSERT INTO usuario_roles (usuario_id, rol_id) VALUES
    ('a1b2c3d4-0000-0000-0000-000000000001', 1); -- ROLE_USER

-- Ciclos de Ana (últimos 3)
INSERT INTO ciclos_menstruales
    (id, usuario_id, fecha_inicio, fecha_fin_periodo, fecha_fin_ciclo, duracion_periodo, duracion_ciclo, regularidad)
VALUES
    ('c1000001-0000-0000-0000-000000000001',
     'a1b2c3d4-0000-0000-0000-000000000001',
     '2024-08-05', '2024-08-10', '2024-09-02', 5, 28, 'regular'),
    ('c1000001-0000-0000-0000-000000000002',
     'a1b2c3d4-0000-0000-0000-000000000001',
     '2024-09-03', '2024-09-08', '2024-10-01', 5, 28, 'regular'),
    ('c1000001-0000-0000-0000-000000000003',
     'a1b2c3d4-0000-0000-0000-000000000001',
