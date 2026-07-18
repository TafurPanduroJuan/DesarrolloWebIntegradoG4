package com.AgroLink.ProyectoAngular.repository;

import com.AgroLink.ProyectoAngular.model.HistorialPrecio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface HistorialPrecioRepository extends JpaRepository<HistorialPrecio, Long> {
    List<HistorialPrecio> findTop5ByLoteIdOrderByFechaCambioDesc(Long loteId);

    // RF-10 — Reporte comercial: cambios de precio dentro de un rango de fechas.
    List<HistorialPrecio> findByFechaCambioBetweenOrderByFechaCambioDesc(LocalDateTime desde, LocalDateTime hasta);

    List<HistorialPrecio> findByLoteIdInAndFechaCambioBetweenOrderByFechaCambioDesc(
            Collection<Long> loteIds, LocalDateTime desde, LocalDateTime hasta);
}
