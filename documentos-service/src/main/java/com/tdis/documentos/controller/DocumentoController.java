package com.tdis.documentos.controller;

import com.tdis.documentos.service.DocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;

    @PostMapping("/upload/{solicitudId}")
    public ResponseEntity<Map<String, String>> subirArchivo(
            @PathVariable UUID solicitudId,
            @RequestParam("archivo") MultipartFile archivo) {
        String[] resultado = documentoService.subirArchivo(solicitudId, archivo);
        return ResponseEntity.ok(Map.of("nombreAlmacenado", resultado[0], "nombreOriginal", resultado[1]));
    }

    @GetMapping("/download/{solicitudId}")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable UUID solicitudId) {
        Resource resource = documentoService.descargarArchivo(solicitudId);
        String mimeType = documentoService.obtenerMimeType(solicitudId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{solicitudId}")
    public ResponseEntity<Void> eliminarArchivo(@PathVariable UUID solicitudId) {
        documentoService.eliminarArchivo(solicitudId);
        return ResponseEntity.noContent().build();
    }
}
