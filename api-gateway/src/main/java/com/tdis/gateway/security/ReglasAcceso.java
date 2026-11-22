package com.tdis.gateway.security;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Set;

/**
 * Tabla de qué ROL puede usar cada endpoint.
 *
 * Se revisan en orden y gana la PRIMERA regla que coincida con el método y la ruta.
 * Si ninguna coincide, se niega el acceso (403). Así, un endpoint nuevo queda
 * bloqueado hasta que se agregue aquí a propósito.
 *
 * Lo que depende de QUIÉN es el dueño del recurso (un alumno solo ve lo suyo)
 * se valida dentro de cada microservicio con la clase Acceso de common.
 *
 * Patrones: "*" = un segmento de la ruta, "**" = cualquier cosa (incluso nada).
 */
@Component
public class ReglasAcceso {

    private static final String ADMIN = "ADMINISTRADOR";
    private static final String ALUMNO = "ALUMNO";
    private static final String INTERNO = "INTERNO";
    private static final String EXTERNO = "EXTERNO";

    private static final Set<String> TODOS = Set.of(ADMIN, ALUMNO, INTERNO, EXTERNO);
    private static final Set<String> SOLO_ADMIN = Set.of(ADMIN);

    private record Regla(HttpMethod metodo, String patron, Set<String> roles) {
    }

    private static Regla regla(HttpMethod metodo, String patron, Set<String> roles) {
        return new Regla(metodo, patron, roles);
    }

    /** metodo = null significa "cualquier método". */
    private static final List<Regla> REGLAS = List.of(
            // ---------- usuarios-service ----------
            regla(null, "/api/admin/**", SOLO_ADMIN),
            regla(null, "/api/usuarios/**", SOLO_ADMIN),

            // ---------- catalogo-service ----------
            // Internos/externos ven sus propias propuestas (el servicio valida que sean suyas)
            regla(HttpMethod.GET, "/api/catalogo/creador/*", Set.of(ADMIN, INTERNO, EXTERNO)),
            // Consultar el catálogo: cualquiera ("?todas" y "?estadoRevision" los limita el servicio a admin)
            regla(HttpMethod.GET, "/api/catalogo/**", TODOS),
            // Proponer actividad
            regla(HttpMethod.POST, "/api/catalogo", Set.of(ADMIN, INTERNO, EXTERNO)),
            // Editar, activar, desactivar, revisar, convertir previa: solo admin
            regla(null, "/api/catalogo/**", SOLO_ADMIN),

            // ---------- tramites-service (solicitudes) ----------
            regla(HttpMethod.GET, "/api/solicitudes", SOLO_ADMIN),
            regla(HttpMethod.GET, "/api/solicitudes/estado/**", SOLO_ADMIN),
            // Por alumno: el servicio valida que sea el propio alumno (o admin)
            regla(HttpMethod.GET, "/api/solicitudes/alumno/**", TODOS),
            regla(HttpMethod.POST, "/api/solicitudes", Set.of(ALUMNO)),
            regla(HttpMethod.POST, "/api/solicitudes/*/analizar-ia", Set.of(ADMIN, ALUMNO)),
            regla(HttpMethod.PUT, "/api/solicitudes/*/nombre-archivo", Set.of(ADMIN, ALUMNO)),
            regla(HttpMethod.PUT, "/api/solicitudes/*/revisar", SOLO_ADMIN),
            // Una solicitud: el servicio valida que sea del alumno (o admin)
            regla(HttpMethod.GET, "/api/solicitudes/*", TODOS),

            // ---------- progreso-service ----------
            regla(HttpMethod.GET, "/api/progreso/**", Set.of(ADMIN, ALUMNO)),

            // ---------- documentos-service ----------
            regla(HttpMethod.POST, "/api/documentos/upload/*", Set.of(ALUMNO)),
            regla(HttpMethod.GET, "/api/documentos/download/*", Set.of(ADMIN, ALUMNO)),
            regla(HttpMethod.DELETE, "/api/documentos/*", Set.of(ADMIN, ALUMNO))
    );

    private final AntPathMatcher matcher = new AntPathMatcher();

    public boolean permite(HttpMethod metodo, String ruta, String rol) {
        if (rol == null) return false;
        for (Regla r : REGLAS) {
            boolean mismoMetodo = r.metodo() == null || r.metodo().equals(metodo);
            if (mismoMetodo && matcher.match(r.patron(), ruta)) {
                return r.roles().contains(rol);
            }
        }
        return false; // ninguna regla coincide: se niega
    }
}
