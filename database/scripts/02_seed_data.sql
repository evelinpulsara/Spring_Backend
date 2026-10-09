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
     '2024-10-02', '2024-10-07', NULL,           5, NULL, 'regular');

-- Registros diarios de Ana (primera semana del ciclo actual)
INSERT INTO registros_diarios
    (usuario_id, ciclo_id, fecha, nivel_energia, nivel_estres, calidad_sueno,
     horas_sueno, hay_sangrado, intensidad_sangrado, estado_animo_valor, estado_animo_etiq, notas)
VALUES
    ('a1b2c3d4-0000-0000-0000-000000000001',
     'c1000001-0000-0000-0000-000000000003',
     '2024-10-02', 4, 7, 5, 6.0, TRUE,  'moderado',  4, 'irritable',
     'Primer día del ciclo, calambres moderados en la mañana.'),
    ('a1b2c3d4-0000-0000-0000-000000000001',
     'c1000001-0000-0000-0000-000000000003',
     '2024-10-03', 5, 6, 6, 7.0, TRUE,  'abundante', 5, 'triste',
     'Flujo más intenso hoy.'),
    ('a1b2c3d4-0000-0000-0000-000000000001',
     'c1000001-0000-0000-0000-000000000003',
     '2024-10-04', 5, 5, 7, 7.5, TRUE,  'moderado',  6, 'cansada',
     NULL),
    ('a1b2c3d4-0000-0000-0000-000000000001',
     'c1000001-0000-0000-0000-000000000003',
     '2024-10-05', 6, 4, 7, 8.0, TRUE,  'leve',      7, 'tranquila',
     NULL),
    ('a1b2c3d4-0000-0000-0000-000000000001',
     'c1000001-0000-0000-0000-000000000003',
     '2024-10-06', 7, 3, 8, 8.0, TRUE,  'leve',      7, 'tranquila',
     'Mejorando poco a poco.'),
    ('a1b2c3d4-0000-0000-0000-000000000001',
     'c1000001-0000-0000-0000-000000000003',
     '2024-10-07', 8, 2, 9, 8.5, FALSE, NULL,        8, 'feliz',
     'Fin del sangrado. Mucho mejor.'),
    ('a1b2c3d4-0000-0000-0000-000000000001',
     'c1000001-0000-0000-0000-000000000003',
     '2024-10-08', 9, 2, 9, 8.0, FALSE, NULL,        9, 'motivada',
     NULL);

-- Síntomas de Ana (día 02/10)
WITH rd AS (
    SELECT id FROM registros_diarios
    WHERE usuario_id = 'a1b2c3d4-0000-0000-0000-000000000001'
      AND fecha = '2024-10-02'
)
INSERT INTO sintomas_registro (registro_id, tipo_sintoma_id, intensidad, momento_dia)
SELECT rd.id, ts.id, 4, 'manana'
FROM rd, tipos_sintoma ts WHERE ts.nombre = 'Calambres'
UNION ALL
SELECT rd.id, ts.id, 3, 'todo_el_dia'
FROM rd, tipos_sintoma ts WHERE ts.nombre = 'Irritabilidad'
UNION ALL
SELECT rd.id, ts.id, 2, 'tarde'
FROM rd, tipos_sintoma ts WHERE ts.nombre = 'Dolor de cabeza';

-- Predicción del próximo ciclo de Ana
INSERT INTO predicciones_ciclo
    (usuario_id, fecha_inicio_estimada, fecha_inicio_rango_min,
     fecha_inicio_rango_max, fecha_ovulacion_estimada, ciclos_analizados, confianza_pct)
VALUES (
    'a1b2c3d4-0000-0000-0000-000000000001',
    '2024-10-30',
    '2024-10-27',
    '2024-11-02',
    '2024-10-16',
    3,
