package com.tdis.catalogo.controller;

import com.tdis.common.dto.ActividadDTO;
import com.tdis.common.enums.EstadoRevision;
import com.tdis.common.enums.EjeFormativo;
import com.tdis.common.security.Acceso;
import com.tdis.catalogo.service.CatalogoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/catalogo")
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService catalogoService;

    @GetMapping
    public ResponseEntity<List<ActividadDTO>> listar(@RequestParam(required = false) Boolean todas,
                                                     @RequestParam(required = false) EstadoRevision estadoRevision,
                                                     @RequestHeader(value = Acceso.HEADER_TIPO, required = false) String tipo) {
        // Ver actividades pendientes, rechazadas o inactivas es solo para admin
        if (estadoRevision != null || Boolean.TRUE.equals(todas)) {
            Acceso.exigirRol(tipo, Acceso.ADMIN);
        }
        if (estadoRevision != null) {
            return ResponseEntity.ok(catalogoService.listarPorEstadoRevision(estadoRevision));
        }
        if (Boolean.TRUE.equals(todas)) {
            return ResponseEntity.ok(catalogoService.listarTodas());
        }
        return ResponseEntity.ok(catalogoService.listarActivas());
    }

    @GetMapping("/eje/{eje}")
    public ResponseEntity<List<ActividadDTO>> listarPorEje(@PathVariable EjeFormativo eje) {
        return ResponseEntity.ok(catalogoService.listarPorEje(eje));
    }

    @GetMapping("/creador/{creadorId}")
    public ResponseEntity<List<ActividadDTO>> listarPorCreador(
            @PathVariable UUID creadorId,
            @RequestHeader(value = Acceso.HEADER_ID, required = false) String userId,
            @RequestHeader(value = Acceso.HEADER_TIPO, required = false) String tipo) {
        Acceso.exigirDuenoOAdmin(userId, tipo, creadorId);
        return ResponseEntity.ok(catalogoService.obtenerPorCreador(creadorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActividadDTO> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(catalogoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<ActividadDTO> crear(
            @Valid @RequestBody ActividadDTO dto,
            @RequestHeader(value = Acceso.HEADER_ID, required = false) UUID userId,
            @RequestHeader(value = Acceso.HEADER_TIPO, required = false) String tipo) {
        // El creador sale del token, no de lo que mande el frontend
        if (!Acceso.esLlamadaInterna(tipo)) {
            dto.setCreadorId(userId);
            dto.setCreadorTipo(tipo);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogoService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActividadDTO> actualizar(@PathVariable UUID id,
                                                    @Valid @RequestBody ActividadDTO dto) {
        return ResponseEntity.ok(catalogoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable UUID id) {
        catalogoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable UUID id) {
        catalogoService.activar(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/revisar")
    public ResponseEntity<ActividadDTO> revisar(@PathVariable UUID id,
                                                 @RequestParam EstadoRevision estado,
                                                 @RequestParam(required = false) String comentario) {
        return ResponseEntity.ok(catalogoService.revisar(id, estado, comentario));
    }

    @PostMapping("/desde-previa/{solicitudId}")
    public ResponseEntity<ActividadDTO> crearDesdePrevia(@PathVariable UUID solicitudId,
                                                          @RequestParam(required = false) UUID creadorId,
                                                          @RequestParam(required = false) String creadorTipo,
                                                          @RequestParam Integer puntosTdi,
                                                          @RequestHeader(value = Acceso.HEADER_ID, required = false) UUID userId,
                                                          @RequestHeader(value = Acceso.HEADER_TIPO, required = false) String tipo) {
        // El creador es el admin que la convierte (sale del token)
        if (!Acceso.esLlamadaInterna(tipo)) {
            creadorId = userId;
            creadorTipo = tipo;
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogoService.crearDesdePrevia(solicitudId, creadorId, creadorTipo, puntosTdi));
    }
}
