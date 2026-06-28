package com.AgroLink.ProyectoAngular.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * RF05 / RF25 — Respuesta del catálogo público.
 * Combina datos del Lote con datos del Cultivo para que el frontend
 * no tenga que hacer dos llamadas.
 */
public class LoteCatalogoResponse {

    private Long loteId;
    private Long cultivoId;

    // Datos del Cultivo
    private String nombreProducto;
    private String variedad;
    private String categoria;
    private String ubicacion;
    private Long agricultorId;

    // Datos del Lote
    private String calidad;
    private BigDecimal precioUnitario;
    private String unidadMedida;
    private Double stockDisponible;
    private LocalDate fechaEntregaEstimada;
    private LocalDate fechaCosecha;
    private String condicionesEntrega;
    private String estado;

    // ── Constructor ──────────────────────────────────────────────
    public LoteCatalogoResponse() {}

    // ── Getters y Setters ────────────────────────────────────────
    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }

    public Long getCultivoId() { return cultivoId; }
    public void setCultivoId(Long cultivoId) { this.cultivoId = cultivoId; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getVariedad() { return variedad; }
    public void setVariedad(String variedad) { this.variedad = variedad; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public Long getAgricultorId() { return agricultorId; }
    public void setAgricultorId(Long agricultorId) { this.agricultorId = agricultorId; }

    public String getCalidad() { return calidad; }
    public void setCalidad(String calidad) { this.calidad = calidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public Double getStockDisponible() { return stockDisponible; }
    public void setStockDisponible(Double stockDisponible) { this.stockDisponible = stockDisponible; }

    public LocalDate getFechaEntregaEstimada() { return fechaEntregaEstimada; }
    public void setFechaEntregaEstimada(LocalDate fechaEntregaEstimada) { this.fechaEntregaEstimada = fechaEntregaEstimada; }

    public LocalDate getFechaCosecha() { return fechaCosecha; }
    public void setFechaCosecha(LocalDate fechaCosecha) { this.fechaCosecha = fechaCosecha; }

    public String getCondicionesEntrega() { return condicionesEntrega; }
    public void setCondicionesEntrega(String condicionesEntrega) { this.condicionesEntrega = condicionesEntrega; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
