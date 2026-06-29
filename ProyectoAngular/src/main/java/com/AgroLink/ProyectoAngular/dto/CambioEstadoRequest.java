package com.AgroLink.ProyectoAngular.dto;

import com.AgroLink.ProyectoAngular.model.enums.EstadoPedidoEnum;

public class CambioEstadoRequest {

    private EstadoPedidoEnum nuevoEstado;
    private String observacion;

    public EstadoPedidoEnum getNuevoEstado() { return nuevoEstado; }
    public void setNuevoEstado(EstadoPedidoEnum nuevoEstado) { this.nuevoEstado = nuevoEstado; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
