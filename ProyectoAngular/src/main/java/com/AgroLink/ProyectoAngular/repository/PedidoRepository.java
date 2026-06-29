package com.AgroLink.ProyectoAngular.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.AgroLink.ProyectoAngular.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByCompradorId(Long compradorId);

    @Query("SELECT DISTINCT p FROM Pedido p WHERE p.id IN " +
           "(SELECT d.pedidoId FROM DetallePedido d WHERE d.agricultorId = :agricultorId)")
    List<Pedido> findByAgricultorId(@Param("agricultorId") Long agricultorId);
}
