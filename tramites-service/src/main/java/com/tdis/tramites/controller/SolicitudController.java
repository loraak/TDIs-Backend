package com.tdis.tramites.controller;

import com.tdis.common.dto.CrearSolicitudRequest;
import com.tdis.common.dto.RevisarSolicitudRequest;
import com.tdis.common.dto.SolicitudDTO;
import com.tdis.common.enums.EstadoSolicitud;
import com.tdis.common.security.Acceso;
import com.tdis.tramites.service.TramiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Qué ROL puede usar cada endpoint lo decide el API Gateway (ReglasAcceso).
 * Aquí se valida que el alumno solo vea/modifique SUS solicitudes.
 */
@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final TramiteService tramiteService;

    @GetMapping
    public ResponseEntity<List<SolicitudDTO>> listarTodas() {
        return ResponseEntity.ok(tramiteService.listarTodas());
    }

    @GetMapping("/alumno/{alumnoId}")
    public ResponseEntity<List<SolicitudDTO>> listarPorAlumno(
            @PathVariable UUID alumnoId,
            @RequestHeader(value = Acceso.HEADER_ID, required = false) String userId,
            @RequestHeader(value = Acceso.HEADER_TIPO, required = false) String tipo) {
        Acceso.exigirDuenoOAdmin(userId, tipo, alumnoId);
        return ResponseEntity.ok(tramiteService.listarPorAlumno(alumnoId));
    }

    @GetMapping("/alumno/{alumnoId}/estado/{estado}")
    public ResponseEntity<List<SolicitudDTO>> listarPorAlumnoYEstado(
            @PathVariable UUID alumnoId, @PathVariable EstadoSolicitud estado,
            @RequestHeader(value = Acceso.HEADER_ID, required = false) String userId,
            @RequestHeader(value = Acceso.HEADER_TIPO, required = false) String tipo) {
        Acceso.exigirDuenoOAdmin(userId, tipo, alumnoId);
        return ResponseEntity.ok(tramiteService.listarPorAlumnoYEstado(alumnoId, estado));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<SolicitudDTO>> listarPorEstado(@PathVariable EstadoSolicitud estado) {
        return ResponseEntity.ok(tramiteService.listarPorEstado(estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudDTO> obtener(
            @PathVariable UUID id,
            @RequestHeader(value = Acceso.HEADER_ID, required = false) String userId,
            @RequestHeader(value = Acceso.HEADER_TIPO, required = false) String tipo) {
        Acceso.exigirDuenoOAdmin(userId, tipo, tramiteService.obtenerAlumnoId(id));
        return ResponseEntity.ok(tramiteService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<SolicitudDTO> crear(
            @RequestHeader(value = Acceso.HEADER_ID, required = false) UUID alumnoId,
            @Valid @RequestBody CrearSolicitudRequest request) {
        // El alumnoId sale del token (lo pone el gateway), nunca del body.
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tramiteService.crear(alumnoId, request));
    }

    @PostMapping("/{id}/analizar-ia")
    public ResponseEntity<SolicitudDTO> analizarIA(
            @PathVariable UUID id,
            @RequestHeader(value = Acceso.HEADER_ID, required = false) String userId,
            @RequestHeader(value = Acceso.HEADER_TIPO, required = false) String tipo) {
        Acceso.exigirDuenoOAdmin(userId, tipo, tramiteService.obtenerAlumnoId(id));
        return ResponseEntity.ok(tramiteService.analizarIA(id));
    }

    @PutMapping("/{id}/revisar")
    public ResponseEntity<SolicitudDTO> revisar(@PathVariable UUID id,
                                                @Valid @RequestBody RevisarSolicitudRequest request) {
        return ResponseEntity.ok(tramiteService.revisar(id, request));
    }

    @PutMapping("/{id}/nombre-archivo")
    public ResponseEntity<SolicitudDTO> actualizarNombreArchivo(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = Acceso.HEADER_ID, required = false) String userId,
            @RequestHeader(value = Acceso.HEADER_TIPO, required = false) String tipo) {
        Acceso.exigirDuenoOAdmin(userId, tipo, tramiteService.obtenerAlumnoId(id));
        return ResponseEntity.ok(tramiteService.actualizarNombreArchivo(id, body.get("nombreArchivo")));
    }
}
