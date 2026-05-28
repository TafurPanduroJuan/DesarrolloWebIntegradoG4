package com.AgroLink.ProyectoAngular.dto;

import com.AgroLink.ProyectoAngular.model.enums.EstadoValidacionEnum;
import jakarta.validation.constraints.NotNull;

public class ValidacionRequest {

    @NotNull
    private EstadoValidacionEnum estadoValidacion;

    private String motivoObservacion;

    public EstadoValidacionEnum getEstadoValidacion() { return estadoValidacion; }
    public void setEstadoValidacion(EstadoValidacionEnum estadoValidacion) { this.estadoValidacion = estadoValidacion; }

    public String getMotivoObservacion() { return motivoObservacion; }
    public void setMotivoObservacion(String motivoObservacion) { this.motivoObservacion = motivoObservacion; }
}
