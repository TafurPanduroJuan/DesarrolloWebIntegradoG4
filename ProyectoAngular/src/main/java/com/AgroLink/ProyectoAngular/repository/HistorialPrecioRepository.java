package com.AgroLink.ProyectoAngular.repository;

import com.AgroLink.ProyectoAngular.model.HistorialPrecio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialPrecioRepository extends JpaRepository<HistorialPrecio, Long> {
    List<HistorialPrecio> findTop5ByLoteIdOrderByFechaCambioDesc(Long loteId);
}
