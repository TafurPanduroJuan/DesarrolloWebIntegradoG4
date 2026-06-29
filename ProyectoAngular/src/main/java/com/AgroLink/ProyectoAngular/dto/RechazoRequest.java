package com.AgroLink.ProyectoAngular.dto;

public class RechazoRequest {

    private Long agricultorId;
    private String motivo;

    public Long getAgricultorId() { return agricultorId; }
    public void setAgricultorId(Long agricultorId) { this.agricultorId = agricultorId; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
