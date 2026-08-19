package com.tdis.catalogo.service;

import com.tdis.catalogo.client.TramitesClient;
import com.tdis.catalogo.entity.Actividad;
import com.tdis.catalogo.repository.ActividadRepository;
import com.tdis.common.dto.ActividadDTO;
import com.tdis.common.dto.SolicitudDTO;
import com.tdis.common.enums.AsignaturaFormacion;
import com.tdis.common.enums.CompetenciaReforzada;
import com.tdis.common.enums.DimensionFormacion;
import com.tdis.common.enums.EstadoRevision;
import com.tdis.common.enums.EstadoSolicitud;
import com.tdis.common.enums.EjeFormativo;
import com.tdis.common.enums.NivelImpacto;
import com.tdis.common.enums.Periodicidad;
import com.tdis.common.enums.PublicoObjetivo;
import com.tdis.common.enums.TipoEvidenciaRequerida;
import com.tdis.common.enums.TipoLugar;
import com.tdis.common.exception.BadRequestException;
import com.tdis.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final ActividadRepository actividadRepository;
    private final TramitesClient tramitesClient;

    public List<ActividadDTO> listarActivas() {
        return actividadRepository.findByActivaTrueAndEstadoRevision(EstadoRevision.APROBADA).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ActividadDTO> listarTodas() {
        return actividadRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ActividadDTO> listarPorEstadoRevision(EstadoRevision estado) {
        return actividadRepository.findByEstadoRevision(estado).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ActividadDTO> listarPorEje(EjeFormativo eje) {
        return actividadRepository.findByEjeAndActivaTrueAndEstadoRevision(eje, EstadoRevision.APROBADA).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ActividadDTO> obtenerPorCreador(UUID creadorId) {
        return actividadRepository.findByCreadorId(creadorId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ActividadDTO obtenerPorId(UUID id) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));
        return toDTO(actividad);
    }

    public ActividadDTO crear(ActividadDTO dto) {
        if (actividadRepository.existsByTituloIgnoreCase(dto.getTitulo())) {
            throw new BadRequestException("Ya existe una actividad con ese nombre");
        }
        Actividad actividad = new Actividad();
        mapDTOToEntity(dto, actividad);
        actividad.setActiva(false);
        actividad.setEstadoRevision(EstadoRevision.PENDIENTE);
        actividad = actividadRepository.save(actividad);
        return toDTO(actividad);
    }

    public ActividadDTO actualizar(UUID id, ActividadDTO dto) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));
        if (actividadRepository.existsByTituloIgnoreCaseAndIdNot(dto.getTitulo(), id)) {
            throw new BadRequestException("Ya existe otra actividad con ese nombre");
        }
        mapDTOToEntity(dto, actividad);
        actividad = actividadRepository.save(actividad);
        return toDTO(actividad);
    }

    public void desactivar(UUID id) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));
        actividad.setActiva(false);
        actividadRepository.save(actividad);
    }

    public void activar(UUID id) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));
        if (actividad.getEstadoRevision() != EstadoRevision.APROBADA) {
            throw new BadRequestException("Solo se pueden activar actividades aprobadas");
        }
        actividad.setActiva(true);
        actividadRepository.save(actividad);
    }

    public ActividadDTO revisar(UUID id, EstadoRevision estado, String comentario) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));
        if (estado == EstadoRevision.RECHAZADA && (comentario == null || comentario.isBlank())) {
            throw new BadRequestException("Debe proporcionar un comentario al rechazar");
        }
        actividad.setEstadoRevision(estado);
        actividad.setComentarioRevision(comentario);
        if (estado == EstadoRevision.RECHAZADA) {
            actividad.setActiva(false);
        }
        actividad = actividadRepository.save(actividad);
        return toDTO(actividad);
    }

    public ActividadDTO crearDesdePrevia(UUID solicitudId, UUID creadorId, String creadorTipo, Integer puntosTdi) {
        SolicitudDTO solicitud = tramitesClient.obtenerSolicitud(solicitudId);

        if (solicitud == null) {
            throw new ResourceNotFoundException("Solicitud no encontrada");
        }
        if (EstadoSolicitud.RECHAZADA.name().equals(solicitud.getEstado())) {
            throw new BadRequestException("No se puede convertir una solicitud rechazada");
        }

        System.out.println("=== DEBUG crearDesdePrevia ===");
        System.out.println("solicitudId: " + solicitudId);
        System.out.println("solicitud.getEje(): " + solicitud.getEje());
        System.out.println("solicitud.getPeriodicidad(): " + solicitud.getPeriodicidad());
        System.out.println("solicitud.getFechaInicio(): " + solicitud.getFechaInicio());
        System.out.println("puntosTdi param: " + puntosTdi);

        if (solicitud.getEje() == null || puntosTdi == null) {
            StringBuilder missing = new StringBuilder();
            if (solicitud.getEje() == null) missing.append("eje");
            if (puntosTdi == null) {
                if (missing.length() > 0) missing.append(", ");
                missing.append("puntosTdi");
            }
            throw new BadRequestException("La solicitud no tiene los campos necesarios: " + missing);
        }

        Actividad actividad = new Actividad();
        actividad.setTitulo(solicitud.getNombreActividad());
        actividad.setDescripcion(solicitud.getDescripcion());
        actividad.setEje(solicitud.getEje());
        actividad.setPuntosTdi(puntosTdi);
        actividad.setPeriodicidad(solicitud.getPeriodicidad() != null ? Periodicidad.valueOf(solicitud.getPeriodicidad()) : Periodicidad.UNICA);
        actividad.setFechaInicio(solicitud.getFechaInicio() != null ? java.time.LocalDate.parse(solicitud.getFechaInicio()) : null);
        actividad.setFechaFin(solicitud.getFechaFin() != null ? java.time.LocalDate.parse(solicitud.getFechaFin()) : null);
        actividad.setHorasEfectivas(solicitud.getHorasEfectivas());
        actividad.setLugar(solicitud.getTipoLugar() != null ? solicitud.getTipoLugar() : TipoLugar.INTERNO);
        actividad.setDimensionesFormacion(parseDimensionesFormacion(solicitud.getDimensionesFormacion()));
        actividad.setNivelImpacto(parseNivelImpacto(solicitud.getNivelImpacto()));
        actividad.setPublicoObjetivo(parsePublicoObjetivo(solicitud.getPublicoObjetivo()));
        actividad.setAsignaturasRelacionadas(parseAsignaturasRelacionadas(solicitud.getAsignaturasRelacionadas()));
        actividad.setCompetenciasReforzar(parseCompetenciasReforzar(solicitud.getCompetenciasReforzar()));
        actividad.setTiposEvidenciaRequerida(parseTiposEvidenciaRequerida(solicitud.getEvidenciasRequeridas()));
        actividad.setCreadorId(creadorId);
        actividad.setCreadorTipo(creadorTipo);
        actividad.setActiva(false);
        actividad.setEstadoRevision(EstadoRevision.PENDIENTE);

        actividad = actividadRepository.save(actividad);
        return toDTO(actividad);
    }

    private void mapDTOToEntity(ActividadDTO dto, Actividad actividad) {
        actividad.setTitulo(dto.getTitulo());
        actividad.setDescripcion(dto.getDescripcion());
        actividad.setEje(dto.getEje());
        actividad.setPuntosTdi(dto.getPuntosTdi());
        actividad.setPeriodicidad(dto.getPeriodicidad());
        actividad.setFechaInicio(dto.getFechaInicio());
        actividad.setFechaFin(dto.getFechaFin());
        actividad.setHorasEfectivas(dto.getHorasEfectivas());
        actividad.setLugar(dto.getLugar());
        actividad.setDimensionesFormacion(stringListToString(dto.getDimensionesFormacion()));
        actividad.setNivelImpacto(dto.getNivelImpacto());
        actividad.setPublicoObjetivo(stringListToString(dto.getPublicoObjetivo()));
        actividad.setAsignaturasRelacionadas(stringListToString(dto.getAsignaturasRelacionadas()));
        actividad.setCompetenciasReforzar(stringListToString(dto.getCompetenciasReforzar()));
        actividad.setTiposEvidenciaRequerida(stringListToString(dto.getTiposEvidenciaRequerida()));
        actividad.setCreadorId(dto.getCreadorId());
        actividad.setCreadorTipo(dto.getCreadorTipo());
        actividad.setArea(dto.getArea());
        actividad.setNombreResponsable(dto.getNombreResponsable());
        actividad.setTelefonoResponsable(dto.getTelefonoResponsable());
    }

    private String stringListToString(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return String.join(",", list);
    }

    private <E extends Enum<E>> List<E> stringToEnumList(String value, Class<E> enumClass) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try {
                        return Enum.valueOf(enumClass, s);
                    } catch (IllegalArgumentException e) {
                        // Intentar parsear desde label
                        return parseEnumFromLabel(s, enumClass);
                    }
                })
                .collect(Collectors.toList());
    }

    private <E extends Enum<E>> E parseEnumFromLabel(String label, Class<E> enumClass) {
        if (enumClass == DimensionFormacion.class) {
            return (E) parseDimensionFormacionLabel(label);
        } else if (enumClass == PublicoObjetivo.class) {
            return (E) parsePublicoObjetivoLabel(label);
        } else if (enumClass == AsignaturaFormacion.class) {
            return (E) parseAsignaturaFormacionLabel(label);
        } else if (enumClass == CompetenciaReforzada.class) {
            return (E) parseCompetenciaReforzadaLabel(label);
        } else if (enumClass == TipoEvidenciaRequerida.class) {
            return (E) parseTipoEvidenciaRequeridaLabel(label);
        } else if (enumClass == NivelImpacto.class) {
            return (E) parseNivelImpacto(label);
        } else if (enumClass == TipoLugar.class) {
            return (E) parseTipoLugar(label);
        }
        return null;
    }

    private ActividadDTO toDTO(Actividad actividad) {
        ActividadDTO dto = new ActividadDTO();
        dto.setId(actividad.getId());
        dto.setTitulo(actividad.getTitulo());
        dto.setDescripcion(actividad.getDescripcion());
        dto.setEje(actividad.getEje());
        dto.setPuntosTdi(actividad.getPuntosTdi());
        dto.setPeriodicidad(actividad.getPeriodicidad());
        dto.setFechaInicio(actividad.getFechaInicio());
        dto.setFechaFin(actividad.getFechaFin());
        dto.setHorasEfectivas(actividad.getHorasEfectivas());
        dto.setLugar(actividad.getLugar());
        dto.setDimensionesFormacion(stringToList(actividad.getDimensionesFormacion()));
        dto.setNivelImpacto(actividad.getNivelImpacto());
        dto.setPublicoObjetivo(stringToList(actividad.getPublicoObjetivo()));
        dto.setAsignaturasRelacionadas(stringToList(actividad.getAsignaturasRelacionadas()));
        dto.setCompetenciasReforzar(stringToList(actividad.getCompetenciasReforzar()));
        dto.setTiposEvidenciaRequerida(stringToList(actividad.getTiposEvidenciaRequerida()));
        dto.setActiva(actividad.getActiva());
        dto.setEstadoRevision(actividad.getEstadoRevision());
        dto.setCreadorId(actividad.getCreadorId());
        dto.setCreadorTipo(actividad.getCreadorTipo());
        dto.setArea(actividad.getArea());
        dto.setNombreResponsable(actividad.getNombreResponsable());
        dto.setTelefonoResponsable(actividad.getTelefonoResponsable());
        dto.setComentarioRevision(actividad.getComentarioRevision());
        dto.setCreatedAt(actividad.getCreatedAt());
        dto.setUpdatedAt(actividad.getUpdatedAt());
        return dto;
    }

    private List<String> stringToList(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private NivelImpacto parseNivelImpacto(String value) {
        if (value == null) return null;
        try {
            return NivelImpacto.valueOf(value);
        } catch (IllegalArgumentException e) {
            if ("Sensibilizador (solo escucha)".equals(value)) return NivelImpacto.SENSIBILIZADOR;
            if ("Formativo (intercambio de ideas)".equals(value)) return NivelImpacto.FORMATIVO;
            if ("Aplicación (participación activa)".equals(value)) return NivelImpacto.APLICACION;
            if ("Implementador (dirige)".equals(value)) return NivelImpacto.IMPLEMENTADOR;
            return null;
        }
    }

    private String parseDimensionesFormacion(String value) {
        if (value == null) return null;
        String[] parts = value.split(",");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            String trimmed = part.trim();
            String enumValue;
            if ("Identidad personal (Aprender a conocer)".equals(trimmed)) enumValue = "IDENTIDAD_PERSONAL";
            else if ("Entorno social (Aprender a convivir)".equals(trimmed)) enumValue = "ENTORNO_SOCIAL";
            else if ("Entorno físico (Aprender a Hacer)".equals(trimmed)) enumValue = "ENTORNO_FISICO";
            else if ("Trascendencia (Aprender a Ser)".equals(trimmed)) enumValue = "TRASCENDENCIA";
            else enumValue = trimmed;
            if (result.length() > 0) result.append(",");
            result.append(enumValue);
        }
        return result.toString();
    }

    private String parsePublicoObjetivo(String value) {
        if (value == null) return null;
        String[] parts = value.split(",");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            String trimmed = part.trim();
            String enumValue;
            if ("Solo alumnas".equals(trimmed)) enumValue = "SOLO_ALUMNAS";
            else if ("Solo alumnos".equals(trimmed)) enumValue = "SOLO_ALUMNOS";
            else if ("Solo División Industrial".equals(trimmed)) enumValue = "SOLO_DIVISION_INDUSTRIAL_Y_NANOTECNOLOGIA";
            else if ("Solo División Económica-Administrativa".equals(trimmed)) enumValue = "SOLO_DIVISION_ECONOMICO_ADMINISTRATIVA";
            else if ("Solo División Tecnologías".equals(trimmed)) enumValue = "SOLO_DIVISION_TECNOLOGIAS";
            else if ("Solo División Idiomas".equals(trimmed)) enumValue = "SOLO_DIVISION_IDIOMAS";
            else if ("Todas las divisiones".equals(trimmed)) enumValue = "TODAS_LAS_DIVISIONES";
            else enumValue = trimmed;
            if (result.length() > 0) result.append(",");
            result.append(enumValue);
        }
        return result.toString();
    }

    private String parseAsignaturasRelacionadas(String value) {
        if (value == null) return null;
        String[] parts = value.split(",");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            String trimmed = part.trim();
            String enumValue;
            if ("1ro Desarrollo Humano y Valores".equals(trimmed)) enumValue = "DESARROLLO_HUMANO_Y_VALORES";
            else if ("2do Habilidades Socioemocionales".equals(trimmed)) enumValue = "HABILIDADES_SOCIOEMOCIONALES";
            else if ("3ra Desarrollo del Pensamiento Crítico".equals(trimmed)) enumValue = "DESARROLLO_DEL_PENSAMIENTO_CRITICO";
            else if ("4to Ética y Valores".equals(trimmed)) enumValue = "ETICA_Y_VALORES";
            else if ("5to Liderazgo de Equipos de Alto Desempeño".equals(trimmed)) enumValue = "LIDERAZGO_DE_EQUIPOS_DE_ALTO_DESEMPENO";
            else if ("7mo Habilidades Gerenciales".equals(trimmed)) enumValue = "HABILIDADES_GERENCIALES";
            else enumValue = trimmed;
            if (result.length() > 0) result.append(",");
            result.append(enumValue);
        }
        return result.toString();
    }

    private String parseCompetenciasReforzar(String value) {
        if (value == null) return null;
        String[] parts = value.split(",");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            String trimmed = part.trim();
            String enumValue;
            if ("Comunicación efectiva".equals(trimmed)) enumValue = "COMUNICACION_EFECTIVA";
            else if ("Trabajo en equipo".equals(trimmed)) enumValue = "TRABAJO_EN_EQUIPO";
            else if ("Liderazgo".equals(trimmed)) enumValue = "LIDERAZGO";
            else if ("Pensamiento crítico".equals(trimmed)) enumValue = "PENSAMIENTO_CRITICO";
            else if ("Responsabilidad y ética".equals(trimmed)) enumValue = "RESPONSABILIDAD_Y_ETICA";
            else if ("Toma de decisiones".equals(trimmed)) enumValue = "TOMA_DE_DECISIONES";
            else if ("Autogestión y disciplina".equals(trimmed)) enumValue = "AUTOGESTION_Y_DISCIPLINA";
            else if ("Participación social".equals(trimmed)) enumValue = "PARTICIPACION_SOCIAL";
            else enumValue = trimmed;
            if (result.length() > 0) result.append(",");
            result.append(enumValue);
        }
        return result.toString();
    }

    private String parseTiposEvidenciaRequerida(String value) {
        if (value == null) return null;
        String[] parts = value.split(",");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            String trimmed = part.trim();
            String enumValue;
            if ("Lista de Asistencia firmada por el responsable".equals(trimmed)) enumValue = "LISTA_ASISTENCIA_FIRMADA";
            else if ("Fotografía".equals(trimmed)) enumValue = "FOTOGRAFIA";
            else if ("Constancia / documento".equals(trimmed)) enumValue = "CONSTANCIA_DOCUMENTO";
            else if ("Producto o reporte elaborado".equals(trimmed)) enumValue = "PRODUCTO_REPORTE_ELABORADO";
            else if ("Otro".equals(trimmed)) enumValue = "OTRO";
            else enumValue = trimmed;
            if (result.length() > 0) result.append(",");
            result.append(enumValue);
        }
        return result.toString();
    }

    private TipoLugar parseTipoLugar(String value) {
        if (value == null) return null;
        if ("INTERNO".equals(value) || "EXTERNO".equals(value)) {
            return TipoLugar.valueOf(value);
        }
        if ("Interno (UTEQ)".equals(value)) return TipoLugar.INTERNO;
        if ("Externo".equals(value)) return TipoLugar.EXTERNO;
        return null;
    }

    private DimensionFormacion parseDimensionFormacionLabel(String value) {
        if (value == null) return null;
        if ("Identidad personal (Aprender a conocer)".equals(value)) return DimensionFormacion.IDENTIDAD_PERSONAL;
        if ("Entorno social (Aprender a convivir)".equals(value)) return DimensionFormacion.ENTORNO_SOCIAL;
        if ("Entorno físico (Aprender a Hacer)".equals(value)) return DimensionFormacion.ENTORNO_FISICO;
        if ("Trascendencia (Aprender a Ser)".equals(value)) return DimensionFormacion.TRASCENDENCIA;
        return null;
    }

    private PublicoObjetivo parsePublicoObjetivoLabel(String value) {
        if (value == null) return null;
        if ("Solo alumnas".equals(value)) return PublicoObjetivo.SOLO_ALUMNAS;
        if ("Solo alumnos".equals(value)) return PublicoObjetivo.SOLO_ALUMNOS;
        if ("Solo División Industrial".equals(value)) return PublicoObjetivo.SOLO_DIVISION_INDUSTRIAL_Y_NANOTECNOLOGIA;
        if ("Solo División Económica-Administrativa".equals(value)) return PublicoObjetivo.SOLO_DIVISION_ECONOMICO_ADMINISTRATIVA;
        if ("Solo División Tecnologías".equals(value)) return PublicoObjetivo.SOLO_DIVISION_TECNOLOGIAS;
        if ("Solo División Idiomas".equals(value)) return PublicoObjetivo.SOLO_DIVISION_IDIOMAS;
        if ("Todas las divisiones".equals(value)) return PublicoObjetivo.TODAS_LAS_DIVISIONES;
        return null;
    }

    private AsignaturaFormacion parseAsignaturaFormacionLabel(String value) {
        if (value == null) return null;
        if ("1ro Desarrollo Humano y Valores".equals(value)) return AsignaturaFormacion.DESARROLLO_HUMANO_Y_VALORES;
        if ("2do Habilidades Socioemocionales".equals(value)) return AsignaturaFormacion.HABILIDADES_SOCIOEMOCIONALES;
        if ("3ra Desarrollo del Pensamiento Crítico".equals(value)) return AsignaturaFormacion.DESARROLLO_DEL_PENSAMIENTO_CRITICO;
        if ("4to Ética y Valores".equals(value)) return AsignaturaFormacion.ETICA_Y_VALORES;
        if ("5to Liderazgo de Equipos de Alto Desempeño".equals(value)) return AsignaturaFormacion.LIDERAZGO_DE_EQUIPOS_DE_ALTO_DESEMPENO;
        if ("7mo Habilidades Gerenciales".equals(value)) return AsignaturaFormacion.HABILIDADES_GERENCIALES;
        return null;
    }

    private CompetenciaReforzada parseCompetenciaReforzadaLabel(String value) {
        if (value == null) return null;
        if ("Comunicación efectiva".equals(value)) return CompetenciaReforzada.COMUNICACION_EFECTIVA;
        if ("Trabajo en equipo".equals(value)) return CompetenciaReforzada.TRABAJO_EN_EQUIPO;
        if ("Liderazgo".equals(value)) return CompetenciaReforzada.LIDERAZGO;
        if ("Pensamiento crítico".equals(value)) return CompetenciaReforzada.PENSAMIENTO_CRITICO;
        if ("Responsabilidad y ética".equals(value)) return CompetenciaReforzada.RESPONSABILIDAD_Y_ETICA;
        if ("Toma de decisiones".equals(value)) return CompetenciaReforzada.TOMA_DE_DECISIONES;
        if ("Autogestión y disciplina".equals(value)) return CompetenciaReforzada.AUTOGESTION_Y_DISCIPLINA;
        if ("Participación social".equals(value)) return CompetenciaReforzada.PARTICIPACION_SOCIAL;
        return null;
    }

    private TipoEvidenciaRequerida parseTipoEvidenciaRequeridaLabel(String value) {
        if (value == null) return null;
        if ("Lista de Asistencia firmada por el responsable".equals(value)) return TipoEvidenciaRequerida.LISTA_ASISTENCIA_FIRMADA;
        if ("Fotografía".equals(value)) return TipoEvidenciaRequerida.FOTOGRAFIA;
        if ("Constancia / documento".equals(value)) return TipoEvidenciaRequerida.CONSTANCIA_DOCUMENTO;
        if ("Producto o reporte elaborado".equals(value)) return TipoEvidenciaRequerida.PRODUCTO_REPORTE_ELABORADO;
        if ("Otro".equals(value)) return TipoEvidenciaRequerida.OTRO;
        return null;
    }
}