package com.tdis.usuarios.client;

import com.tdis.common.dto.ActividadDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

// En local (url vacía) se resuelve por Eureka; en producción se usa la URL de Cloud Run.
@FeignClient(name = "catalogo-service", url = "${tdis.url.catalogo:}")
public interface CatalogoClient {

    @GetMapping("/api/catalogo/{id}")
    ActividadDTO obtenerActividad(@PathVariable("id") UUID id);
}
