package com.tdis.tramites.ia;

/**
 * Lo que opina la IA sobre una evidencia.
 *
 * @param veredicto         APROBAR, RECHAZAR o REVISAR (pasar a un humano)
 * @param confianza         0.0 a 1.0
 * @param motivo            explicación corta en español (la puede leer el alumno)
 * @param descripcionImagen qué ve la IA en la foto (para el admin)
 */
public record ResultadoIA(Veredicto veredicto, double confianza, String motivo, String descripcionImagen) {

    public enum Veredicto { APROBAR, RECHAZAR, REVISAR }

    /** Cuando no hay IA o falló: directo a revisión del admin. */
    public static ResultadoIA sinAnalisis(String motivo) {
        return new ResultadoIA(Veredicto.REVISAR, 0.0, motivo, null);
    }
}
