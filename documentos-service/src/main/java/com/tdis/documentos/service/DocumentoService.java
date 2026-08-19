package com.tdis.documentos.service;

import com.tdis.common.exception.BadRequestException;
import com.tdis.common.exception.ResourceNotFoundException;
import com.tdis.documentos.entity.Archivo;
import com.tdis.documentos.repository.ArchivoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentoService {

    private final ArchivoRepository archivoRepository;

    @Value("${documentos.upload-dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de uploads", e);
        }
    }

    private static final Set<String> MIME_PERMITIDOS = Set.of(
            "image/png",
            "image/jpeg"
    );

    @Transactional
    public String[] subirArchivo(UUID solicitudId, MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new BadRequestException("El archivo esta vacio");
        }

        String contentType = archivo.getContentType();
        if (contentType == null || !MIME_PERMITIDOS.contains(contentType)) {
            throw new BadRequestException("Solo se permiten imagenes PNG o JPG");
        }

        if (archivo.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("El archivo excede el tamano maximo de 10 MB");
        }

        if (archivoRepository.existsBySolicitudId(solicitudId)) {
            eliminarArchivo(solicitudId);
        }

        String nombreOriginal = archivo.getOriginalFilename();
        String extension = "";
        if (nombreOriginal != null && nombreOriginal.contains(".")) {
            extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
        }
        String nombreAlmacenado = solicitudId + "_" + UUID.randomUUID() + extension;

        try {
            Path destino = Paths.get(uploadDir).resolve(nombreAlmacenado);
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            Archivo archivoEntity = new Archivo();
            archivoEntity.setSolicitudId(solicitudId);
            archivoEntity.setNombreOriginal(nombreOriginal);
            archivoEntity.setNombreAlmacenado(nombreAlmacenado);
            archivoEntity.setTamano(archivo.getSize());
            archivoEntity.setMimeType(contentType);
            archivoRepository.save(archivoEntity);

            return new String[]{nombreAlmacenado, nombreOriginal};
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo", e);
        }
    }

    public Resource descargarArchivo(UUID solicitudId) {
        Archivo archivo = archivoRepository.findBySolicitudId(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("No hay archivo para esta solicitud"));

        try {
            Path ruta = Paths.get(uploadDir).resolve(archivo.getNombreAlmacenado());
            Resource resource = new UrlResource(ruta.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("No se pudo leer el archivo");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error al acceder al archivo", e);
        }
    }

    public String obtenerMimeType(UUID solicitudId) {
        Archivo archivo = archivoRepository.findBySolicitudId(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("No hay archivo para esta solicitud"));
        return archivo.getMimeType() != null ? archivo.getMimeType() : "application/octet-stream";
    }

    @Transactional
    public void eliminarArchivo(UUID solicitudId) {
        archivoRepository.findBySolicitudId(solicitudId).ifPresent(archivo -> {
            try {
                Path ruta = Paths.get(uploadDir).resolve(archivo.getNombreAlmacenado());
                Files.deleteIfExists(ruta);
            } catch (IOException e) {
                // Log but continue
            }
            archivoRepository.delete(archivo);
        });
    }
}
