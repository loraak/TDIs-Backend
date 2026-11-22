package com.tdis.documentos.controller;

import com.tdis.common.exception.ResourceNotFoundException;
import com.tdis.common.security.Acceso;
import com.tdis.documentos.client.TramitesClient;
import com.tdis.documentos.service.DocumentoService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

/**
 * Un alumno solo puede subir, ver o borrar la evidencia de SUS solicitudes.
 * Admin puede ver y borrar cualquiera. Las llamadas internas (tramites-service
 * descargando la imagen para la IA) no traen X-User-Type y se permiten.
 */
@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;
    private final TramitesClient tramitesClient;

    @PostMapping("/upload/{solicitudId}")
    public ResponseEntity<Map<String, String>> subirArchivo(
            @PathVariable UUID solicitudId,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestHeader(value = Acceso.HEADER_ID, required = false) String userId,
            @RequestHeader(value = Acceso.HEADER_TIPO, required = false) String tipo) {
        validarDueno(solicitudId, userId, tipo);
        String[] resultado = documentoService.subirArchivo(solicitudId, archivo);
        return ResponseEntity.ok(Map.of("nombreAlmacenado", resultado[0], "nombreOriginal", resultado[1]));
    }

    @GetMapping("/download/{solicitudId}")
    public ResponseEntity<Resource> descargarArchivo(
            @PathVariable UUID solicitudId,
            @RequestHeader(value = Acceso.HEADER_ID, required = false) String userId,
            @RequestHeader(value = Acceso.HEADER_TIPO, required = false) String tipo) {
        validarDueno(solicitudId, userId, tipo);
        Resource resource = documentoService.descargarArchivo(solicitudId);
        String mimeType = documentoService.obtenerMimeType(solicitudId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{solicitudId}")
    public ResponseEntity<Void> eliminarArchivo(
            @PathVariable UUID solicitudId,
            @RequestHeader(value = Acceso.HEADER_ID, required = false) String userId,
            @RequestHeader(value = Acceso.HEADER_TIPO, required = false) String tipo) {
        validarDueno(solicitudId, userId, tipo);
        documentoService.eliminarArchivo(solicitudId);
        return ResponseEntity.noContent().build();
    }

    private void validarDueno(UUID solicitudId, String userId, String tipo) {
        // Admin y llamadas internas no necesitan consultar a tramites
        if (Acceso.esLlamadaInterna(tipo) || Acceso.esAdmin(tipo)) return;

        UUID alumnoId;
        try {
            alumnoId = tramitesClient.obtenerSolicitud(solicitudId).getAlumnoId();
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Solicitud no encontrada");
        }
        Acceso.exigirDuenoOAdmin(userId, tipo, alumnoId);
    }
}
