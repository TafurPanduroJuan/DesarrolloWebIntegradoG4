package com.AgroLink.ProyectoAngular.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AgroLink.ProyectoAngular.model.DatosContacto;

public interface DatosContactoRepository extends JpaRepository<DatosContacto, Long>{
    
}
