package com.AgroLink.ProyectoAngular.dto.reporte;

import java.math.BigDecimal;

/**
 * RF-10 — Fila del reporte comercial agrupada por agricultor (solo vista ADMINISTRADOR).
 */
public class VentaPorAgricultorDTO {

    private Long agricultorId;
    private String nombreAgricultor;
    private Long numeroPedidos;
    private BigDecimal montoTotal;

    public VentaPorAgricultorDTO() {
    }

    public VentaPorAgricultorDTO(Long agricultorId, String nombreAgricultor, Long numeroPedidos, BigDecimal montoTotal) {
        this.agricultorId = agricultorId;
        this.nombreAgricultor = nombreAgricultor;
        this.numeroPedidos = numeroPedidos;
        this.montoTotal = montoTotal;
    }

    public Long getAgricultorId() {
        return agricultorId;
    }

    public void setAgricultorId(Long agricultorId) {
        this.agricultorId = agricultorId;
    }

    public String getNombreAgricultor() {
        return nombreAgricultor;
    }

    public void setNombreAgricultor(String nombreAgricultor) {
        this.nombreAgricultor = nombreAgricultor;
    }

    public Long getNumeroPedidos() {
        return numeroPedidos;
    }

    public void setNumeroPedidos(Long numeroPedidos) {
        this.numeroPedidos = numeroPedidos;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }
}
