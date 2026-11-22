package com.tdis.progreso.client;

import com.tdis.common.dto.SolicitudDTO;
import com.tdis.common.enums.EstadoSolicitud;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

// En local (url vacía) se resuelve por Eureka; en producción se usa la URL de Cloud Run.
@FeignClient(name = "tramites-service", url = "${tdis.url.tramites:}")
public interface TramitesClient {

    @GetMapping("/api/solicitudes/alumno/{alumnoId}")
    List<SolicitudDTO> listarPorAlumno(@PathVariable("alumnoId") UUID alumnoId);

    @GetMapping("/api/solicitudes/alumno/{alumnoId}/estado/{estado}")
    List<SolicitudDTO> listarPorAlumnoYEstado(@PathVariable("alumnoId") UUID alumnoId,
                                               @PathVariable("estado") EstadoSolicitud estado);
}
