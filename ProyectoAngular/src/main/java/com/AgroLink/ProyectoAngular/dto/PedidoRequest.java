package com.AgroLink.ProyectoAngular.dto;

import java.time.LocalDate;

public class PedidoRequest {

    private Long compradorId;
    private Long loteId;
    private Double cantidadSolicitada;
    private LocalDate fechaEntregaDeseada;
    private String notasEspeciales;

    public Long getCompradorId() { return compradorId; }
    public void setCompradorId(Long compradorId) { this.compradorId = compradorId; }
    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }
    public Double getCantidadSolicitada() { return cantidadSolicitada; }
    public void setCantidadSolicitada(Double cantidadSolicitada) { this.cantidadSolicitada = cantidadSolicitada; }
    public LocalDate getFechaEntregaDeseada() { return fechaEntregaDeseada; }
    public void setFechaEntregaDeseada(LocalDate fechaEntregaDeseada) { this.fechaEntregaDeseada = fechaEntregaDeseada; }
    public String getNotasEspeciales() { return notasEspeciales; }
    public void setNotasEspeciales(String notasEspeciales) { this.notasEspeciales = notasEspeciales; }
}
