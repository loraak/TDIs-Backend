package com.tdis.catalogo.entity;

import com.tdis.common.enums.AsignaturaFormacion;
import com.tdis.common.enums.CompetenciaReforzada;
import com.tdis.common.enums.DimensionFormacion;
import com.tdis.common.enums.EjeFormativo;
import com.tdis.common.enums.EstadoRevision;
import com.tdis.common.enums.NivelImpacto;
import com.tdis.common.enums.Periodicidad;
import com.tdis.common.enums.PublicoObjetivo;
import com.tdis.common.enums.TipoEvidenciaRequerida;
import com.tdis.common.enums.TipoLugar;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "actividades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(length = 1000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EjeFormativo eje;

    @Column(name = "puntos_tdi", nullable = false)
    private Integer puntosTdi;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Periodicidad periodicidad;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "horas_efectivas")
    private Integer horasEfectivas;

    @Enumerated(EnumType.STRING)
    @Column(name = "lugar", length = 20)
    private TipoLugar lugar;

    @Column(name = "dimensiones_formacion", columnDefinition = "TEXT")
    private String dimensionesFormacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_impacto", length = 20)
    private NivelImpacto nivelImpacto;

    @Column(name = "publico_objetivo", columnDefinition = "TEXT")
    private String publicoObjetivo;

    @Column(name = "asignaturas_relacionadas", columnDefinition = "TEXT")
    private String asignaturasRelacionadas;

    @Column(name = "competencias_reforzar", columnDefinition = "TEXT")
    private String competenciasReforzar;

    @Column(name = "tipos_evidencia_requerida", columnDefinition = "TEXT")
    private String tiposEvidenciaRequerida;

    @Column(nullable = false)
    private Boolean activa = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_revision", nullable = false, length = 20)
    private EstadoRevision estadoRevision = EstadoRevision.PENDIENTE;

    @Column(name = "creador_id")
    private UUID creadorId;

    @Column(name = "creador_tipo", length = 20)
    private String creadorTipo;

    @Column(name = "area", length = 100)
    private String area;

    @Column(name = "nombre_responsable", length = 200)
    private String nombreResponsable;

    @Column(name = "telefono_responsable", length = 50)
    private String telefonoResponsable;

    @Column(name = "comentario_revision", columnDefinition = "TEXT")
    private String comentarioRevision;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (estadoRevision == null) estadoRevision = EstadoRevision.PENDIENTE;
        if (activa == null) activa = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}