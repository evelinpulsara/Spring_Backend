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
