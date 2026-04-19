package com.AgroLink.ProyectoAngular.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AgroLink.ProyectoAngular.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

}
