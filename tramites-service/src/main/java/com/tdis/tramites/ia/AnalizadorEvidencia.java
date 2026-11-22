package com.tdis.tramites.ia;

import com.tdis.common.dto.ActividadDTO;
import com.tdis.tramites.entity.Solicitud;

/**
 * Analiza la foto de evidencia de una solicitud. Hay una implementación por
 * proveedor y se elige con la propiedad tdis.ia.proveedor:
 *   ninguno (default) -> SinIAAnalizador
 *   gemini            -> GeminiAnalizador
 *   n8n               -> N8nAnalizador (el flujo anterior)
 *
 * Nunca debe lanzar excepción: si algo falla, regresa ResultadoIA.sinAnalisis(...).
 */
public interface AnalizadorEvidencia {

    ResultadoIA analizar(ActividadDTO actividad, Solicitud solicitud, byte[] imagen);
}
