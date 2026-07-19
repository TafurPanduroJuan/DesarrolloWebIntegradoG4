package com.AgroLink.ProyectoAngular.repository;

import com.AgroLink.ProyectoAngular.model.SolicitudContacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudContactoRepository extends JpaRepository<SolicitudContacto, Long> {
    List<SolicitudContacto> findAllByOrderByFechaDesc();
}
