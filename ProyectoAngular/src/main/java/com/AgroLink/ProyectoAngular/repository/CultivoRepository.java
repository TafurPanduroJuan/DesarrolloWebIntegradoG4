package com.AgroLink.ProyectoAngular.repository;

import com.AgroLink.ProyectoAngular.model.Cultivo;
import com.AgroLink.ProyectoAngular.model.enums.EstadoCultivoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CultivoRepository extends JpaRepository<Cultivo, Long> {
    List<Cultivo> findByAgricultorId(Long agricultorId);
    List<Cultivo> findByAgricultorIdAndEstado(Long agricultorId, EstadoCultivoEnum estado);
}
