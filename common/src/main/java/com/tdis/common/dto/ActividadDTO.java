package com.tdis.common.dto;

import com.tdis.common.enums.AsignaturaFormacion;
import com.tdis.common.enums.DimensionFormacion;
import com.tdis.common.enums.EjeFormativo;
import com.tdis.common.enums.EstadoRevision;
import com.tdis.common.enums.NivelImpacto;
import com.tdis.common.enums.Periodicidad;
import com.tdis.common.enums.PublicoObjetivo;
import com.tdis.common.enums.TipoLugar;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActividadDTO {
    private UUID id;
    private String titulo;
    private String descripcion;
    private EjeFormativo eje;
    private Integer puntosTdi;
    private Periodicidad periodicidad;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer horasEfectivas;
    private TipoLugar lugar;
    private List<String> dimensionesFormacion;
    private NivelImpacto nivelImpacto;
    private List<String> publicoObjetivo;
    private List<String> asignaturasRelacionadas;
    private List<String> competenciasReforzar;
    private List<String> tiposEvidenciaRequerida;
    private Boolean activa;
    private EstadoRevision estadoRevision;
    private UUID creadorId;
    private String creadorNombre;
    private String creadorTipo;
    private String area;
    private String nombreResponsable;
    private String telefonoResponsable;
    private String comentarioRevision;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}