-- Admin user (password: admin123)
INSERT INTO usuarios (id, email, password, nombre, apellidos, tipo_usuario, activo, created_at, updated_at)
SELECT * FROM (VALUES
    (gen_random_uuid(), 'admin@uteq.edu.mx', '$2a$10$A5ioBCADMpmR6l.WdwzuROy77Ezg8fWB1FDHgqSF1IS6I4ubyeAGK',
     'Admin', 'Sistema', 'ADMINISTRADOR', true, NOW(), NOW())
) AS v
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'admin@uteq.edu.mx');

-- Test students (password: 'password123' para todos)
INSERT INTO usuarios (id, matricula, email, password, nombre, apellidos, tipo_usuario, activo, created_at, updated_at)
SELECT * FROM (VALUES
    (gen_random_uuid(), '2026191005', 'juan.perez@uteq.edu.mx', '$2a$10$3Jtd8jvOWBSUXn9asjq38eEK4e6W29L78vo0qNtxB6EcKfISnC/KS', 'Juan Pablo', 'Pérez López', 'ALUMNO', true, NOW(), NOW()),
    (gen_random_uuid(), '2024396177', 'maria.garcia@uteq.edu.mx', '$2a$10$3Jtd8jvOWBSUXn9asjq38eEK4e6W29L78vo0qNtxB6EcKfISnC/KS', 'María', 'García Hernández', 'ALUMNO', true, NOW(), NOW()),
    (gen_random_uuid(), '2025192003', 'carlos.martinez@uteq.edu.mx', '$2a$10$3Jtd8jvOWBSUXn9asjq38eEK4e6W29L78vo0qNtxB6EcKfISnC/KS', 'Carlos', 'Martínez Ruiz', 'ALUMNO', true, NOW(), NOW()),
    (gen_random_uuid(), '2024185008', 'ana.torres@uteq.edu.mx', '$2a$10$3Jtd8jvOWBSUXn9asjq38eEK4e6W29L78vo0qNtxB6EcKfISnC/KS', 'Ana Sofía', 'Torres Mendoza', 'ALUMNO', true, NOW(), NOW())
) AS v
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE matricula = v.column2);

-- Test interno (password: 'interno123')
INSERT INTO usuarios (id, matricula, email, password, nombre, apellidos, tipo_usuario, activo, created_at, updated_at)
SELECT * FROM (VALUES
    (gen_random_uuid(), '2020100001', 'interno@uteq.edu.mx', '$2a$10$Tn2DpsgUgJxfotBU/Go/ReB5vlKi.cZTYb3u9T/hzVCB44zMeLNLu', 'Carlos', 'Ramírez Vega', 'INTERNO', true, NOW(), NOW())
) AS v
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'interno@uteq.edu.mx');

-- Test externo (password: 'externo123')
INSERT INTO usuarios (id, email, password, nombre, apellidos, tipo_usuario, activo, created_at, updated_at)
SELECT * FROM (VALUES
    (gen_random_uuid(), 'externo@organizacion.org', '$2a$10$B4BToLPtkgfp6ZxEO4RHhOuC0WGTwot.zBraig2n3ORRTfvI.r77m', 'Organización', 'Civil ABC', 'EXTERNO', true, NOW(), NOW())
) AS v
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'externo@organizacion.org');
