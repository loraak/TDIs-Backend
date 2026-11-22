package com.tdis.documentos.client;

import com.tdis.common.dto.SolicitudDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Se usa para saber de qué alumno es una solicitud antes de subir/descargar su archivo.
 * Es una llamada interna (no lleva X-User-Type), así que tramites-service la permite.
 */
// En local (url vacía) se resuelve por Eureka; en producción se usa la URL de Cloud Run.
@FeignClient(name = "tramites-service", url = "${tdis.url.tramites:}")
public interface TramitesClient {

    @GetMapping("/api/solicitudes/{id}")
    SolicitudDTO obtenerSolicitud(@PathVariable("id") UUID id);
}
