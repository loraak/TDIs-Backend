package com.tdis.tramites.ia;

import com.tdis.common.dto.ActividadDTO;
import com.tdis.common.dto.AnalisisIAResponse;
import com.tdis.tramites.client.N8nClient;
import com.tdis.tramites.entity.Solicitud;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** El flujo anterior (webhook de n8n + Ollama), por si se quiere seguir usando en local. */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "tdis.ia.proveedor", havingValue = "n8n")
public class N8nAnalizador implements AnalizadorEvidencia {

    private final N8nClient n8nClient;

    @Override
    public ResultadoIA analizar(ActividadDTO actividad, Solicitud solicitud, byte[] imagen) {
        AnalisisIAResponse r = n8nClient.analizarEvidencia(
                actividad.getTitulo(), solicitud.getDescripcion(), imagen, solicitud.getNombreArchivo());
        ResultadoIA.Veredicto v = switch (r.getEstado() == null ? "" : r.getEstado()) {
            case "Aprobado" -> ResultadoIA.Veredicto.APROBAR;
            case "Rechazado" -> ResultadoIA.Veredicto.RECHAZAR;
            default -> ResultadoIA.Veredicto.REVISAR;
        };
        // n8n no reporta confianza: se toma como segura si dio un veredicto claro
        double confianza = v == ResultadoIA.Veredicto.REVISAR ? 0.0 : 1.0;
        return new ResultadoIA(v, confianza, r.getMotivo(), r.getDescripcion_analisis());
    }
}
