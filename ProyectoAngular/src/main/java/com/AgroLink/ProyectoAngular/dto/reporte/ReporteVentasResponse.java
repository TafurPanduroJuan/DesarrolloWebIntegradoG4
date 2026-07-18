package com.AgroLink.ProyectoAngular.dto.reporte;

import com.AgroLink.ProyectoAngular.model.HistorialPrecio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * RF-10 — Historial y reportes comerciales.
 * Respuesta agregada: totales, ventas por producto, ventas por agricultor
 * (solo ADMINISTRADOR) y los cambios de precio (RF-20) ocurridos en el periodo.
 */
public class ReporteVentasResponse {

    private LocalDate desde;
    private LocalDate hasta;
    private String alcance; // GLOBAL, AGRICULTOR, COMPRADOR
    private long totalPedidos;
    private BigDecimal totalIngresos;
    private Map<String, Long> pedidosPorEstado;
    private List<VentaPorProductoDTO> ventasPorProducto;
    private List<VentaPorAgricultorDTO> ventasPorAgricultor;
    private List<HistorialPrecio> cambiosPrecio;

    public LocalDate getDesde() {
        return desde;
    }

    public void setDesde(LocalDate desde) {
        this.desde = desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public void setHasta(LocalDate hasta) {
        this.hasta = hasta;
    }

    public String getAlcance() {
        return alcance;
    }

    public void setAlcance(String alcance) {
        this.alcance = alcance;
    }

    public long getTotalPedidos() {
        return totalPedidos;
    }

    public void setTotalPedidos(long totalPedidos) {
        this.totalPedidos = totalPedidos;
    }

    public BigDecimal getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(BigDecimal totalIngresos) {
        this.totalIngresos = totalIngresos;
    }

    public Map<String, Long> getPedidosPorEstado() {
        return pedidosPorEstado;
    }

    public void setPedidosPorEstado(Map<String, Long> pedidosPorEstado) {
        this.pedidosPorEstado = pedidosPorEstado;
    }

    public List<VentaPorProductoDTO> getVentasPorProducto() {
        return ventasPorProducto;
    }

    public void setVentasPorProducto(List<VentaPorProductoDTO> ventasPorProducto) {
        this.ventasPorProducto = ventasPorProducto;
    }

    public List<VentaPorAgricultorDTO> getVentasPorAgricultor() {
        return ventasPorAgricultor;
    }

    public void setVentasPorAgricultor(List<VentaPorAgricultorDTO> ventasPorAgricultor) {
        this.ventasPorAgricultor = ventasPorAgricultor;
    }

    public List<HistorialPrecio> getCambiosPrecio() {
        return cambiosPrecio;
    }

    public void setCambiosPrecio(List<HistorialPrecio> cambiosPrecio) {
        this.cambiosPrecio = cambiosPrecio;
    }
}
