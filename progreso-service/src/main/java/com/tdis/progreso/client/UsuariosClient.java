package com.tdis.progreso.client;

import com.tdis.common.dto.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

// En local (url vacía) se resuelve por Eureka; en producción se usa la URL de Cloud Run.
@FeignClient(name = "usuarios-service", url = "${tdis.url.usuarios:}")
public interface UsuariosClient {

    @GetMapping("/api/usuarios/matricula/{matricula}")
    UsuarioDTO obtenerPorMatricula(@PathVariable("matricula") String matricula);

    @GetMapping("/api/usuarios/{id}")
    UsuarioDTO obtenerPorId(@PathVariable("id") UUID id);
}
