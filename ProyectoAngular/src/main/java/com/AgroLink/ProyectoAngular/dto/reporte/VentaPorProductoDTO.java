package com.AgroLink.ProyectoAngular.dto.reporte;

import java.math.BigDecimal;

/**
 * RF-10 — Fila del reporte comercial agrupada por producto (cultivo).
 */
public class VentaPorProductoDTO {

    private String nombreProducto;
    private String categoria;
    private Double cantidadVendidaKg;
    private BigDecimal montoTotal;
    private Long numeroPedidos;

    public VentaPorProductoDTO() {
    }

    public VentaPorProductoDTO(String nombreProducto, String categoria, Double cantidadVendidaKg,
                                BigDecimal montoTotal, Long numeroPedidos) {
        this.nombreProducto = nombreProducto;
        this.categoria = categoria;
        this.cantidadVendidaKg = cantidadVendidaKg;
        this.montoTotal = montoTotal;
        this.numeroPedidos = numeroPedidos;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Double getCantidadVendidaKg() {
        return cantidadVendidaKg;
    }

    public void setCantidadVendidaKg(Double cantidadVendidaKg) {
        this.cantidadVendidaKg = cantidadVendidaKg;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public Long getNumeroPedidos() {
        return numeroPedidos;
    }

    public void setNumeroPedidos(Long numeroPedidos) {
        this.numeroPedidos = numeroPedidos;
    }
}
