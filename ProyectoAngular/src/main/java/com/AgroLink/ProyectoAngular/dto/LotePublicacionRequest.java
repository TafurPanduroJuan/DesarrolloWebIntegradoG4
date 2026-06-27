package com.AgroLink.ProyectoAngular.dto;

import com.AgroLink.ProyectoAngular.model.enums.CalidadLoteEnum;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * RF04 – Datos necesarios para publicar un lote comercial.
 * Todos los campos son obligatorios para poder publicar.
 */
public class LotePublicacionRequest {

    @NotNull(message = "El cultivo es obligatorio")
    private Long cultivoId;

    @NotNull(message = "La cantidad (kg) es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Double cantidadKg;

    @NotNull(message = "La calidad es obligatoria")
    private CalidadLoteEnum calidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private BigDecimal precioUnitario;

    @NotBlank(message = "La unidad de medida es obligatoria")
    private String unidadMedida;

    @NotNull(message = "La fecha de entrega estimada es obligatoria")
    @Future(message = "La fecha de entrega debe ser futura")
    private LocalDate fechaEntregaEstimada;

    private String condicionesEntrega;

    // ── Getters / Setters ─────────────────────────────────────
    public Long getCultivoId() { return cultivoId; }
    public void setCultivoId(Long cultivoId) { this.cultivoId = cultivoId; }
    public Double getCantidadKg() { return cantidadKg; }
    public void setCantidadKg(Double cantidadKg) { this.cantidadKg = cantidadKg; }
    public CalidadLoteEnum getCalidad() { return calidad; }
    public void setCalidad(CalidadLoteEnum calidad) { this.calidad = calidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public LocalDate getFechaEntregaEstimada() { return fechaEntregaEstimada; }
    public void setFechaEntregaEstimada(LocalDate fechaEntregaEstimada) { this.fechaEntregaEstimada = fechaEntregaEstimada; }
    public String getCondicionesEntrega() { return condicionesEntrega; }
    public void setCondicionesEntrega(String condicionesEntrega) { this.condicionesEntrega = condicionesEntrega; }
}
