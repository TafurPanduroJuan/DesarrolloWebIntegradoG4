package com.AgroLink.ProyectoAngular.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AgroLink.ProyectoAngular.model.Lote;

public interface LoteRepository extends JpaRepository<Lote, Long> {
    List<Lote> findByCultivoId(Long cultivoId);
    List<Lote> findByPublicadoTrue();
}
