package com.AgroLink.ProyectoAngular.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.AgroLink.ProyectoAngular.model.enums.EstadoPedidoEnum;
@Entity
@Table(name = "historial_estado_pedido")
public class HistorialEstadoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long pedidoId;

    @Enumerated(EnumType.STRING)
    private EstadoPedidoEnum estadoAnterior;

    @Enumerated(EnumType.STRING)
    private EstadoPedidoEnum estadoNuevo;

    private String observacion;
    private LocalDateTime fechaCambio = LocalDateTime.now();
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getPedidoId() {
        return pedidoId;
    }
    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }
    public EstadoPedidoEnum getEstadoAnterior() {
        return estadoAnterior;
    }
    public void setEstadoAnterior(EstadoPedidoEnum estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }
    public EstadoPedidoEnum getEstadoNuevo() {
        return estadoNuevo;
    }
    public void setEstadoNuevo(EstadoPedidoEnum estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }
    public String getObservacion() {
        return observacion;
    }
    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
    public LocalDateTime getFechaCambio() {
        return fechaCambio;
    }
    public void setFechaCambio(LocalDateTime fechaCambio) {
        this.fechaCambio = fechaCambio;
    }

    
}