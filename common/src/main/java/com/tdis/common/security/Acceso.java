package com.tdis.common.security;

import com.tdis.common.exception.ForbiddenException;

import java.util.Arrays;
import java.util.UUID;

/**
 * Validaciones de permisos dentro de cada microservicio.
 *
 * El API Gateway valida el JWT y agrega dos encabezados a cada petición:
 *   X-User-Id   -> id del usuario (UUID)
 *   X-User-Type -> ADMINISTRADOR | ALUMNO | INTERNO | EXTERNO
 *
 * El gateway ya decide QUÉ ROL puede usar cada endpoint. Aquí se valida lo que
 * el gateway no puede saber: si el recurso es DEL usuario (por ejemplo, que un
 * alumno solo vea sus propias solicitudes).
 *
 * Si X-User-Type no viene, la petición es una llamada interna entre servicios
 * (Feign), que no pasa por el gateway, y se permite. Por eso, en producción los
 * microservicios NO deben estar expuestos a internet: solo el gateway.
 */
public final class Acceso {

    public static final String HEADER_ID = "X-User-Id";
    public static final String HEADER_TIPO = "X-User-Type";

    public static final String ADMIN = "ADMINISTRADOR";
    public static final String ALUMNO = "ALUMNO";
    public static final String INTERNO = "INTERNO";
    public static final String EXTERNO = "EXTERNO";

    private Acceso() {
    }

    /** Llamada entre microservicios (no pasó por el gateway). */
    public static boolean esLlamadaInterna(String tipo) {
        return tipo == null || tipo.isBlank();
    }

    public static boolean esAdmin(String tipo) {
        return ADMIN.equals(tipo);
    }

    /** Lanza 403 si el usuario no tiene ninguno de los roles indicados. */
    public static void exigirRol(String tipo, String... roles) {
        if (esLlamadaInterna(tipo)) return;
        if (Arrays.asList(roles).contains(tipo)) return;
        throw new ForbiddenException("No tienes permiso para realizar esta accion");
    }

    /** Lanza 403 si el usuario no es admin y no es el dueño del recurso. */
    public static void exigirDuenoOAdmin(String userId, String tipo, UUID duenoId) {
        if (esLlamadaInterna(tipo) || esAdmin(tipo)) return;
        if (userId == null || duenoId == null || !duenoId.toString().equals(userId)) {
            throw new ForbiddenException("No tienes permiso para acceder a este recurso");
        }
    }
}
