package com.AgroLink.ProyectoAngular.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AgroLink.ProyectoAngular.model.Cultivo;

public interface CultivoRepository extends JpaRepository<Cultivo, Long> {
    List<Cultivo> findByAgricultorId(Long agricultorId);
}
