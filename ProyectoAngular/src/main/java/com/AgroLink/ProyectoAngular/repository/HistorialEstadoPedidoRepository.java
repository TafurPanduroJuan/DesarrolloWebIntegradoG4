package com.AgroLink.ProyectoAngular.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AgroLink.ProyectoAngular.model.HistorialEstadoPedido;

public interface HistorialEstadoPedidoRepository extends JpaRepository <HistorialEstadoPedido, Long> {
    
}
