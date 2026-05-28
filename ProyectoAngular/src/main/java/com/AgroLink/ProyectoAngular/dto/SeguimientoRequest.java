package com.AgroLink.ProyectoAngular.dto;

import com.AgroLink.ProyectoAngular.model.enums.EstadoCultivoEnum;
import jakarta.validation.constraints.NotNull;

public class SeguimientoRequest {

    @NotNull
    private EstadoCultivoEnum estado;

    private String etapaProductiva;
    private String observacion;

    public EstadoCultivoEnum getEstado() { return estado; }
    public void setEstado(EstadoCultivoEnum estado) { this.estado = estado; }

    public String getEtapaProductiva() { return etapaProductiva; }
    public void setEtapaProductiva(String etapaProductiva) { this.etapaProductiva = etapaProductiva; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
