package com.tdis.tramites.service;

import com.tdis.common.dto.ActividadDTO;
import com.tdis.common.dto.AnalisisIAResponse;
import com.tdis.common.dto.CrearSolicitudRequest;
import com.tdis.common.dto.RevisarSolicitudRequest;
import com.tdis.common.dto.SolicitudDTO;
import com.tdis.common.enums.EstadoSolicitud;
import com.tdis.common.enums.EjeFormativo;
import com.tdis.common.enums.TipoLugar;
import com.tdis.common.exception.BadRequestException;
import com.tdis.common.exception.ResourceNotFoundException;
import com.tdis.common.dto.UsuarioDTO;
import com.tdis.tramites.client.CatalogoClient;
import com.tdis.tramites.client.DocumentosClient;
import com.tdis.tramites.client.N8nClient;
import com.tdis.tramites.client.UsuariosClient;
import com.tdis.tramites.entity.Solicitud;
import com.tdis.tramites.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TramiteService {

    private final SolicitudRepository solicitudRepository;
    private final CatalogoClient catalogoClient;
    private final DocumentosClient documentosClient;
    private final N8nClient n8nClient;
    private final UsuariosClient usuariosClient;

    public List<SolicitudDTO> listarPorAlumno(UUID alumnoId) {
        List<Solicitud> entities = solicitudRepository.findByAlumnoIdOrderByCreatedAtDesc(alumnoId);
        log.info("listarPorAlumno: alumnoId={}, found {} entities", alumnoId, entities.size());
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<SolicitudDTO> listarPorEstado(EstadoSolicitud estado) {
        return solicitudRepository.findByEstadoOrderByCreatedAtDesc(estado).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<SolicitudDTO> listarTodas() {
        return solicitudRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public SolicitudDTO obtenerPorId(UUID id) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        return toDTO(solicitud);
    }

    @Transactional
    public SolicitudDTO crear(UUID alumnoId, CrearSolicitudRequest request) {
        UUID alumnoIdFinal = alumnoId;
        if (alumnoIdFinal == null) {
            throw new BadRequestException("El alumnoId es requerido");
        }

        boolean esPrevia = "PREVIA".equalsIgnoreCase(request.getTipoSolicitud());

        // EVIDENCIA: validar actividad del catálogo. PREVIA: no requiere actividad.
        if (!esPrevia) {
            if (request.getActividadId() == null) {
                throw new BadRequestException("La actividad es requerida para evidencias");
            }
            ActividadDTO actividad = catalogoClient.obtenerActividad(request.getActividadId());
            if (!actividad.getActiva()) {
                throw new BadRequestException("La actividad no esta disponible");
            }

            boolean yaEnviada = solicitudRepository
                    .existsByAlumnoIdAndActividadIdAndEstado(alumnoIdFinal, request.getActividadId(), EstadoSolicitud.EN_REVISION);
            if (yaEnviada) {
                throw new BadRequestException("Ya tienes una solicitud en revision para esta actividad");
            }
        }

        Solicitud solicitud = new Solicitud();
        solicitud.setAlumnoId(alumnoIdFinal);
        solicitud.setActividadId(esPrevia ? null : request.getActividadId());
        solicitud.setNombreActividad(request.getNombreActividad());
        solicitud.setTipoSolicitud(request.getTipoSolicitud());
        solicitud.setDescripcion(request.getDescripcion());
        solicitud.setReflexion(request.getReflexion());
        solicitud.setLugar(request.getLugar());
        solicitud.setHoras(request.getHoras());
        solicitud.setTipoActividad(request.getTipoActividad());
        solicitud.setMateriaRelacionada(request.getMateriaRelacionada());
        solicitud.setDivision(request.getDivision());
        solicitud.setPrograma(request.getPrograma());
        solicitud.setGrupo(request.getGrupo());
        solicitud.setCuatrimestre(request.getCuatrimestre());
        solicitud.setTurno(request.getTurno());
        solicitud.setTutor(request.getTutor());
        solicitud.setNombreResponsable(request.getNombreResponsable());
        solicitud.setCargoResponsable(request.getCargoResponsable());
        solicitud.setTelefonoResponsable(request.getTelefonoResponsable());
        solicitud.setCorreoResponsable(request.getCorreoResponsable());

        // Campos específicos de Solicitud Previa
        solicitud.setDimensionesFormacion(request.getDimensionesFormacion());
        solicitud.setNivelImpacto(request.getNivelImpacto());
        solicitud.setPublicoObjetivo(request.getPublicoObjetivo());
        solicitud.setAsignaturasRelacionadas(request.getAsignaturasRelacionadas());
        solicitud.setCompetenciasReforzar(request.getCompetenciasReforzar());
        solicitud.setEvidenciasRequeridas(request.getEvidenciasRequeridas());
        solicitud.setJustificacionPersonal(request.getJustificacionPersonal());
        solicitud.setImpactoAcademico(request.getImpactoAcademico());
        solicitud.setAsistenciaEsperada(request.getAsistenciaEsperada());
        solicitud.setAlumnosGeneranTdi(request.getAlumnosGeneranTdi());
        solicitud.setHorasEstimadas(request.getHorasEstimadas());
        // Para PREVIA, usar horasEfectivas como horasEstimadas si no hay horasEstimadas
        if (esPrevia && request.getHorasEfectivas() != null && (request.getHorasEstimadas() == null || request.getHorasEstimadas().isBlank())) {
            solicitud.setHorasEstimadas(String.valueOf(request.getHorasEfectivas()));
        }
        // Periodicidad y fechas para Solicitud Previa
        solicitud.setPeriodicidad(request.getPeriodicidad());
        solicitud.setFechaInicio(request.getFechaInicio());
        solicitud.setFechaFin(request.getFechaFin());

        // Campos de Actividad para PREVIA
        if (esPrevia) {
            solicitud.setEje(request.getEje());
            solicitud.setHorasEfectivas(request.getHorasEfectivas());
            solicitud.setTipoLugar(request.getTipoLugar());
        }

        solicitud.setEstado(EstadoSolicitud.EN_REVISION);

        solicitud = solicitudRepository.save(solicitud);
        return toDTO(solicitud);
    }

    @Transactional
    public SolicitudDTO actualizarNombreArchivo(UUID solicitudId, String nombreArchivo) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        solicitud.setNombreArchivo(nombreArchivo);
        solicitud = solicitudRepository.save(solicitud);
        return toDTO(solicitud);
    }

    @Transactional
    public SolicitudDTO analizarIA(UUID solicitudId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        if (!"EVIDENCIA".equals(solicitud.getTipoSolicitud())) {
            throw new BadRequestException("Solo se puede analizar solicitudes de evidencia");
        }

        byte[] imagenBytes;
        try {
            imagenBytes = documentosClient.descargarArchivoBytes(solicitudId);
        } catch (Exception e) {
            throw new BadRequestException("No hay evidencia adjunta para analizar");
        }

        try {
            ActividadDTO actividad = catalogoClient.obtenerActividad(solicitud.getActividadId());

            AnalisisIAResponse ia = n8nClient.analizarEvidencia(
                    actividad.getTitulo(),
                    solicitud.getDescripcion(),
                    imagenBytes,
                    solicitud.getNombreArchivo()
            );

            solicitud.setAiEstado(ia.getEstado());
            solicitud.setAiMotivo(ia.getMotivo());
            solicitud.setAiDescripcionAnalisis(ia.getDescripcion_analisis());

            String estadoIA = ia.getEstado();
            if ("Aprobado".equals(estadoIA)) {
                solicitud.setEstado(EstadoSolicitud.APROBADA);
            } else if ("Rechazado".equals(estadoIA)) {
                solicitud.setEstado(EstadoSolicitud.RECHAZADA);
            } else {
                solicitud.setEstado(EstadoSolicitud.REVISION_HUMANA);
            }

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Error en analisis IA: {}", e.getMessage());
            solicitud.setEstado(EstadoSolicitud.REVISION_HUMANA);
            solicitud.setAiMotivo("No fue posible conectar con el servicio de analisis IA.");
        }

        solicitud = solicitudRepository.save(solicitud);
        return toDTO(solicitud);
    }

    @Transactional
    public SolicitudDTO revisar(UUID id, RevisarSolicitudRequest request) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        if (solicitud.getEstado() != EstadoSolicitud.EN_REVISION
                && solicitud.getEstado() != EstadoSolicitud.REVISION_HUMANA) {
            throw new BadRequestException("La solicitud ya fue revisada");
        }

        if (request.getEstado() == EstadoSolicitud.RECHAZADA) {
            if (request.getComentario() == null || request.getComentario().isBlank()) {
                throw new BadRequestException("Debe proporcionar un comentario al rechazar");
            }
            solicitud.setComentarioRechazo(request.getComentario());
        }

        solicitud.setEstado(request.getEstado());
        solicitud = solicitudRepository.save(solicitud);
        return toDTO(solicitud);
    }

    public List<SolicitudDTO> listarPorAlumnoYEstado(UUID alumnoId, EstadoSolicitud estado) {
        return solicitudRepository.findByAlumnoIdAndEstado(alumnoId, estado).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private SolicitudDTO toDTO(Solicitud solicitud) {
        SolicitudDTO dto = new SolicitudDTO();
        dto.setId(solicitud.getId());
        dto.setAlumnoId(solicitud.getAlumnoId());
        dto.setTipoSolicitud(solicitud.getTipoSolicitud());
        dto.setActividadId(solicitud.getActividadId());
        dto.setDescripcion(solicitud.getDescripcion());
        dto.setReflexion(solicitud.getReflexion());
        dto.setLugar(solicitud.getLugar());
        dto.setHoras(solicitud.getHoras());
        dto.setTipoActividad(solicitud.getTipoActividad());
        dto.setMateriaRelacionada(solicitud.getMateriaRelacionada());
        dto.setDivision(solicitud.getDivision());
        dto.setPrograma(solicitud.getPrograma());
        dto.setGrupo(solicitud.getGrupo());
        dto.setCuatrimestre(solicitud.getCuatrimestre());
        dto.setTurno(solicitud.getTurno());
        dto.setTutor(solicitud.getTutor());
        dto.setNombreResponsable(solicitud.getNombreResponsable());
        dto.setCargoResponsable(solicitud.getCargoResponsable());
        dto.setTelefonoResponsable(solicitud.getTelefonoResponsable());
        dto.setCorreoResponsable(solicitud.getCorreoResponsable());
        dto.setEstado(solicitud.getEstado());
        dto.setComentarioRechazo(solicitud.getComentarioRechazo());
        dto.setArchivoPath(solicitud.getArchivoPath());
        dto.setNombreArchivo(solicitud.getNombreArchivo());
        dto.setAiEstado(solicitud.getAiEstado());
        dto.setAiMotivo(solicitud.getAiMotivo());
        dto.setAiDescripcionAnalisis(solicitud.getAiDescripcionAnalisis());
        dto.setDimensionesFormacion(solicitud.getDimensionesFormacion());
        dto.setNivelImpacto(solicitud.getNivelImpacto());
        dto.setPublicoObjetivo(solicitud.getPublicoObjetivo());
        dto.setAsignaturasRelacionadas(solicitud.getAsignaturasRelacionadas());
        dto.setCompetenciasReforzar(solicitud.getCompetenciasReforzar());
        dto.setEvidenciasRequeridas(solicitud.getEvidenciasRequeridas());
        dto.setJustificacionPersonal(solicitud.getJustificacionPersonal());
        dto.setImpactoAcademico(solicitud.getImpactoAcademico());
        dto.setAsistenciaEsperada(solicitud.getAsistenciaEsperada());
        dto.setAlumnosGeneranTdi(solicitud.getAlumnosGeneranTdi());
        dto.setHorasEstimadas(solicitud.getHorasEstimadas());
        dto.setPeriodicidad(solicitud.getPeriodicidad());
        dto.setFechaInicio(solicitud.getFechaInicio());
        dto.setFechaFin(solicitud.getFechaFin());
        dto.setNombreActividad(solicitud.getNombreActividad());
        // Campos de Actividad para PREVIA
        dto.setEje(solicitud.getEje());
        dto.setPuntosTdi(solicitud.getPuntosTdi());
        dto.setHorasEfectivas(solicitud.getHorasEfectivas());
        dto.setTipoLugar(solicitud.getTipoLugar());
        dto.setCreatedAt(solicitud.getCreatedAt());
        dto.setUpdatedAt(solicitud.getUpdatedAt());

        try {
            if (solicitud.getActividadId() != null) {
                ActividadDTO actividad = catalogoClient.obtenerActividad(solicitud.getActividadId());
                dto.setActividadTitulo(actividad.getTitulo());
                dto.setActividadEje(actividad.getEje().name());
                dto.setActividadPuntos(actividad.getPuntosTdi());
            } else if (solicitud.getNombreActividad() != null) {
                dto.setActividadTitulo(solicitud.getNombreActividad());
            }
        } catch (Exception e) {
            log.warn("toDTO: catalogo failed for actividadId={}: {}", solicitud.getActividadId(), e.getMessage());
            dto.setActividadTitulo("Actividad no disponible");
        }

        try {
            UsuarioDTO usuario = usuariosClient.obtenerPorId(solicitud.getAlumnoId());
            dto.setAlumnoNombre(usuario.getNombre() + " " + usuario.getApellidos());
            dto.setAlumnoMatricula(usuario.getMatricula());
        } catch (Exception e) {
            log.warn("toDTO: usuarios failed for alumnoId={}: {}", solicitud.getAlumnoId(), e.getMessage());
            dto.setAlumnoNombre("Alumno no disponible");
        }

        return dto;
    }
}
