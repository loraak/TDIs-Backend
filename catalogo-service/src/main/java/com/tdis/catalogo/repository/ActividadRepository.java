package com.tdis.catalogo.repository;

import com.tdis.catalogo.entity.Actividad;
import com.tdis.common.enums.EstadoRevision;
import com.tdis.common.enums.EjeFormativo;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.UUID;


public interface ActividadRepository extends JpaRepository<Actividad, UUID> {
    List<Actividad> findByEje(EjeFormativo eje);
    List<Actividad> findByActivaTrue();
    List<Actividad> findByEjeAndActivaTrue(EjeFormativo eje);
    List<Actividad> findByEstadoRevision(EstadoRevision estado);
    List<Actividad> findByActivaTrueAndEstadoRevision(EstadoRevision estado);
    List<Actividad> findByEjeAndActivaTrueAndEstadoRevision(EjeFormativo eje, EstadoRevision estado);
    List<Actividad> findByCreadorId(UUID creadorId);

    boolean existsByTituloIgnoreCase(String titulo);
    boolean existsByTituloIgnoreCaseAndIdNot(String titulo, UUID id);
}
