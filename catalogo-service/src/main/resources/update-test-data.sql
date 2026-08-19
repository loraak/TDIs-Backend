-- Actualizar datos de prueba para actividades existentes
UPDATE actividades SET 
    horas_efectivas = 8,
    lugar = 'INTERNO',
    dimensiones_formacion = 'ENTORNO_SOCIAL,TRASCENDENCIA',
    nivel_impacto = 'FORMATIVO',
    publico_objetivo = 'TODAS_LAS_DIVISIONES',
    asignaturas_relacionadas = 'DESARROLLO_HUMANO_Y_VALORES,HABILIDADES_SOCIOEMOCIONALES',
    competencias_reforzar = 'TRABAJO_EN_EQUIPO,PARTICIPACION_SOCIAL,RESPONSABILIDAD_Y_ETICA',
    tipos_evidencia_requerida = 'LISTA_ASISTENCIA_FIRMADA,FOTOGRAFIA'
WHERE titulo = 'Apoyo Social - Croquton y acciones comunitarias';

UPDATE actividades SET 
    horas_efectivas = 4,
    lugar = 'INTERNO',
    dimensiones_formacion = 'IDENTIDAD_PERSONAL,ENTORNO_SOCIAL',
    nivel_impacto = 'SENSIBILIZADOR',
    publico_objetivo = 'TODAS_LAS_DIVISIONES',
    asignaturas_relacionadas = 'DESARROLLO_HUMANO_Y_VALORES,HABILIDADES_SOCIOEMOCIONALES,ETICA_Y_VALORES',
    competencias_reforzar = 'COMUNICACION_EFECTIVA,PENSAMIENTO_CRITICO,AUTOGESTION_Y_DISCIPLINA',
    tipos_evidencia_requerida = 'FOTOGRAFIA,CONSTANCIA_DOCUMENTO'
WHERE titulo = 'Ponte en su Lugar: Museo de las Heridas no Visibles';

UPDATE actividades SET 
    horas_efectivas = 6,
    lugar = 'INTERNO',
    dimensiones_formacion = 'IDENTIDAD_PERSONAL,ENTORNO_FISICO',
    nivel_impacto = 'FORMATIVO',
    publico_objetivo = 'SOLO_ALUMNAS',
    asignaturas_relacionadas = 'DESARROLLO_HUMANO_Y_VALORES,HABILIDADES_SOCIOEMOCIONALES',
    competencias_reforzar = 'AUTOGESTION_Y_DISCIPLINA,LIDERAZGO,RESPONSABILIDAD_Y_ETICA',
    tipos_evidencia_requerida = 'CONSTANCIA_DOCUMENTO,FOTOGRAFIA'
WHERE titulo = 'Defensa Urbana Femenina';

UPDATE actividades SET 
    horas_efectivas = 3,
    lugar = 'INTERNO',
    dimensiones_formacion = 'IDENTIDAD_PERSONAL,TRASCENDENCIA',
    nivel_impacto = 'SENSIBILIZADOR',
    publico_objetivo = 'TODAS_LAS_DIVISIONES',
    asignaturas_relacionadas = 'DESARROLLO_HUMANO_Y_VALORES,ETICA_Y_VALORES,DESARROLLO_DEL_PENSAMIENTO_CRITICO',
    competencias_reforzar = 'PENSAMIENTO_CRITICO,COMUNICACION_EFECTIVA',
    tipos_evidencia_requerida = 'FOTOGRAFIA'
WHERE titulo = 'CineClub';

UPDATE actividades SET 
    horas_efectivas = 12,
    lugar = 'INTERNO',
    dimensiones_formacion = 'IDENTIDAD_PERSONAL,ENTORNO_FISICO',
    nivel_impacto = 'FORMATIVO',
    publico_objetivo = 'TODAS_LAS_DIVISIONES',
    asignaturas_relacionadas = 'DESARROLLO_HUMANO_Y_VALORES,HABILIDADES_SOCIOEMOCIONALES',
    competencias_reforzar = 'TRABAJO_EN_EQUIPO,COMUNICACION_EFECTIVA,AUTOGESTION_Y_DISCIPLINA',
    tipos_evidencia_requerida = 'FOTOGRAFIA,PRODUCTO_REPORTE_ELABORADO'
WHERE titulo = 'Taller de Arte Urbano';

UPDATE actividades SET 
    horas_efectivas = 2,
    lugar = 'INTERNO',
    dimensiones_formacion = 'IDENTIDAD_PERSONAL,TRASCENDENCIA',
    nivel_impacto = 'SENSIBILIZADOR',
    publico_objetivo = 'TODAS_LAS_DIVISIONES',
    asignaturas_relacionadas = 'DESARROLLO_HUMANO_Y_VALORES',
    competencias_reforzar = 'COMUNICACION_EFECTIVA,PARTICIPACION_SOCIAL',
    tipos_evidencia_requerida = 'FOTOGRAFIA,CONSTANCIA_DOCUMENTO'
WHERE titulo = 'Concierto Universitario';

UPDATE actividades SET 
    horas_efectivas = 8,
    lugar = 'INTERNO',
    dimensiones_formacion = 'ENTORNO_FISICO,ENTORNO_SOCIAL',
    nivel_impacto = 'APLICACION',
    publico_objetivo = 'TODAS_LAS_DIVISIONES',
    asignaturas_relacionadas = 'DESARROLLO_HUMANO_Y_VALORES,HABILIDADES_SOCIOEMOCIONALES',
    competencias_reforzar = 'TRABAJO_EN_EQUIPO,LIDERAZGO,TOMA_DE_DECISIONES',
    tipos_evidencia_requerida = 'LISTA_ASISTENCIA_FIRMADA,FOTOGRAFIA'
WHERE titulo = 'Torneo de Futbol Interuniversitario';

UPDATE actividades SET 
    horas_efectivas = 1,
    lugar = 'INTERNO',
    dimensiones_formacion = 'IDENTIDAD_PERSONAL,ENTORNO_FISICO',
    nivel_impacto = 'SENSIBILIZADOR',
    publico_objetivo = 'TODAS_LAS_DIVISIONES',
    asignaturas_relacionadas = 'DESARROLLO_HUMANO_Y_VALORES,HABILIDADES_SOCIOEMOCIONALES',
    competencias_reforzar = 'AUTOGESTION_Y_DISCIPLINA,RESPONSABILIDAD_Y_ETICA',
    tipos_evidencia_requerida = 'LISTA_ASISTENCIA_FIRMADA,FOTOGRAFIA'
WHERE titulo = 'Clases de Yoga al Aire Libre';

UPDATE actividades SET 
    horas_efectivas = 4,
    lugar = 'INTERNO',
    dimensiones_formacion = 'ENTORNO_FISICO,ENTORNO_SOCIAL',
    nivel_impacto = 'FORMATIVO',
    publico_objetivo = 'TODAS_LAS_DIVISIONES',
    asignaturas_relacionadas = 'DESARROLLO_HUMANO_Y_VALORES,ETICA_Y_VALORES',
    competencias_reforzar = 'RESPONSABILIDAD_Y_ETICA,PARTICIPACION_SOCIAL,COMUNICACION_EFECTIVA',
    tipos_evidencia_requerida = 'LISTA_ASISTENCIA_FIRMADA,CONSTANCIA_DOCUMENTO'
WHERE titulo = 'Jornada de Vacunacion Influenza';

UPDATE actividades SET 
    horas_efectivas = 16,
    lugar = 'EXTERNO',
    dimensiones_formacion = 'ENTORNO_SOCIAL,TRASCENDENCIA',
    nivel_impacto = 'IMPLEMENTADOR',
    publico_objetivo = 'TODAS_LAS_DIVISIONES',
    asignaturas_relacionadas = 'DESARROLLO_HUMANO_Y_VALORES,HABILIDADES_SOCIOEMOCIONALES,ETICA_Y_VALORES,LIDERAZGO_DE_EQUIPOS_DE_ALTO_DESEMPENO',
    competencias_reforzar = 'TRABAJO_EN_EQUIPO,LIDERAZGO,PARTICIPACION_SOCIAL,RESPONSABILIDAD_Y_ETICA,TOMA_DE_DECISIONES',
    tipos_evidencia_requerida = 'LISTA_ASISTENCIA_FIRMADA,FOTOGRAFIA,CONSTANCIA_DOCUMENTO,PRODUCTO_REPORTE_ELABORADO'
WHERE titulo = 'Voluntariado Social Externo';

UPDATE actividades SET 
    horas_efectivas = 2,
    lugar = 'INTERNO',
    dimensiones_formacion = 'IDENTIDAD_PERSONAL,TRASCENDENCIA',
    nivel_impacto = 'FORMATIVO',
    publico_objetivo = 'TODAS_LAS_DIVISIONES',
    asignaturas_relacionadas = 'LIDERAZGO_DE_EQUIPOS_DE_ALTO_DESEMPENO,HABILIDADES_GERENCIALES,DESARROLLO_DEL_PENSAMIENTO_CRITICO',
    competencias_reforzar = 'LIDERAZGO,COMUNICACION_EFECTIVA,PENSAMIENTO_CRITICO,TOMA_DE_DECISIONES',
    tipos_evidencia_requerida = 'FOTOGRAFIA,CONSTANCIA_DOCUMENTO'
WHERE titulo = 'Conferencia de Liderazgo';

UPDATE actividades SET 
    horas_efectivas = 20,
    lugar = 'INTERNO',
    dimensiones_formacion = 'IDENTIDAD_PERSONAL,ENTORNO_SOCIAL,TRASCENDENCIA',
    nivel_impacto = 'IMPLEMENTADOR',
    publico_objetivo = 'TODAS_LAS_DIVISIONES',
    asignaturas_relacionadas = 'DESARROLLO_HUMANO_Y_VALORES,HABILIDADES_SOCIOEMOCIONALES,LIDERAZGO_DE_EQUIPOS_DE_ALTO_DESEMPENO,HABILIDADES_GERENCIALES',
    competencias_reforzar = 'LIDERAZGO,TRABAJO_EN_EQUIPO,COMUNICACION_EFECTIVA,RESPONSABILIDAD_Y_ETICA,AUTOGESTION_Y_DISCIPLINA,TOMA_DE_DECISIONES,PARTICIPACION_SOCIAL',
    tipos_evidencia_requerida = 'LISTA_ASISTENCIA_FIRMADA,FOTOGRAFIA,CONSTANCIA_DOCUMENTO,PRODUCTO_REPORTE_ELABORADO'
WHERE titulo = 'Programa de Mentoria';