package com.AgroLink.ProyectoAngular.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.AgroLink.ProyectoAngular.model.enums.EstadoPedidoEnum;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long compradorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoPedidoEnum estado;

    private String notasEspeciales;
    private LocalDateTime fechaPedido = LocalDateTime.now();
    private LocalDate fechaEntregaEstimada;
    private BigDecimal totalEstimado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCompradorId() { return compradorId; }
    public void setCompradorId(Long compradorId) { this.compradorId = compradorId; }
    public EstadoPedidoEnum getEstado() { return estado; }
    public void setEstado(EstadoPedidoEnum estado) { this.estado = estado; }
    public String getNotasEspeciales() { return notasEspeciales; }
    public void setNotasEspeciales(String notasEspeciales) { this.notasEspeciales = notasEspeciales; }
    public LocalDateTime getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(LocalDateTime fechaPedido) { this.fechaPedido = fechaPedido; }
    public LocalDate getFechaEntregaEstimada() { return fechaEntregaEstimada; }
    public void setFechaEntregaEstimada(LocalDate fechaEntregaEstimada) { this.fechaEntregaEstimada = fechaEntregaEstimada; }
    public BigDecimal getTotalEstimado() { return totalEstimado; }
    public void setTotalEstimado(BigDecimal totalEstimado) { this.totalEstimado = totalEstimado; }
}
