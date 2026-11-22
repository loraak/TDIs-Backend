package com.tdis.progreso.controller;

import com.tdis.common.dto.ProgresoDTO;
import com.tdis.common.security.Acceso;
import com.tdis.progreso.service.ProgresoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin puede ver el progreso de cualquiera; un alumno solo el suyo.
 */
@RestController
@RequestMapping("/api/progreso")
@RequiredArgsConstructor
public class ProgresoController {

    private final ProgresoService progresoService;

    @GetMapping("/{alumnoId}")
    public ResponseEntity<ProgresoDTO> obtenerProgreso(
            @PathVariable UUID alumnoId,
            @RequestHeader(value = Acceso.HEADER_ID, required = false) String userId,
            @RequestHeader(value = Acceso.HEADER_TIPO, required = false) String tipo) {
        Acceso.exigirDuenoOAdmin(userId, tipo, alumnoId);
        return ResponseEntity.ok(progresoService.calcularProgreso(alumnoId));
    }

    @GetMapping("/matricula/{matricula}")
    public ResponseEntity<ProgresoDTO> obtenerProgresoPorMatricula(
            @PathVariable String matricula,
            @RequestHeader(value = Acceso.HEADER_ID, required = false) String userId,
            @RequestHeader(value = Acceso.HEADER_TIPO, required = false) String tipo) {
        ProgresoDTO progreso = progresoService.calcularProgresoPorMatricula(matricula);
        // Hasta tener el progreso sabemos de quién es la matrícula
        Acceso.exigirDuenoOAdmin(userId, tipo, progreso.getAlumnoId());
        return ResponseEntity.ok(progreso);
    }
}
