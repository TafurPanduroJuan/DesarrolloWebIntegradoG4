package com.AgroLink.ProyectoAngular.dto;

import com.AgroLink.ProyectoAngular.model.enums.TipoMovimientoEnum;
import jakarta.validation.constraints.*;

/**
 * RF09 – Petición para ajustar el stock de un lote.
 */
public class AjusteStockRequest {

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimientoEnum tipo;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Double cantidad;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    // ── Getters / Setters ─────────────────────────────────────
    public TipoMovimientoEnum getTipo() { return tipo; }
    public void setTipo(TipoMovimientoEnum tipo) { this.tipo = tipo; }
    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
