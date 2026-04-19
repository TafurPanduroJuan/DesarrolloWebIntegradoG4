package com.AgroLink.ProyectoAngular.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AgroLink.ProyectoAngular.model.EventoProduccion;

public interface EventoProduccionRepository extends JpaRepository<EventoProduccion, Long> {
    List<EventoProduccion> findByCultivoId(Long cultivoId);
}
