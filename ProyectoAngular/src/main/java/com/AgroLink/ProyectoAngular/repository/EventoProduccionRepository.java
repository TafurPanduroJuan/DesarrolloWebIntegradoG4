package com.AgroLink.ProyectoAngular.repository;

import com.AgroLink.ProyectoAngular.model.EventoProduccion;
import com.AgroLink.ProyectoAngular.model.enums.TipoEventoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoProduccionRepository extends JpaRepository<EventoProduccion, Long> {
    List<EventoProduccion> findByCultivoId(Long cultivoId);
    List<EventoProduccion> findByCultivoIdAndTipo(Long cultivoId, TipoEventoEnum tipo);
}
