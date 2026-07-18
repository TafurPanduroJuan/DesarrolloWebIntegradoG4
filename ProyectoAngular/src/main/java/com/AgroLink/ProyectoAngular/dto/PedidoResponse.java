package com.AgroLink.ProyectoAngular.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PedidoResponse {

    private Long id;
    private Long compradorId;
    private String estado;
    private String notasEspeciales;
    private LocalDateTime fechaPedido;
    private LocalDate fechaEntregaEstimada;
    private BigDecimal totalEstimado;
    private Boolean esParcial;
    private List<DetalleResponse> detalles;
    private List<HistorialResponse> historial;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCompradorId() { return compradorId; }
    public void setCompradorId(Long compradorId) { this.compradorId = compradorId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getNotasEspeciales() { return notasEspeciales; }
    public void setNotasEspeciales(String notasEspeciales) { this.notasEspeciales = notasEspeciales; }
    public LocalDateTime getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(LocalDateTime fechaPedido) { this.fechaPedido = fechaPedido; }
    public LocalDate getFechaEntregaEstimada() { return fechaEntregaEstimada; }
    public void setFechaEntregaEstimada(LocalDate fechaEntregaEstimada) { this.fechaEntregaEstimada = fechaEntregaEstimada; }
    public BigDecimal getTotalEstimado() { return totalEstimado; }
    public void setTotalEstimado(BigDecimal totalEstimado) { this.totalEstimado = totalEstimado; }
    public Boolean getEsParcial() { return esParcial; }
    public void setEsParcial(Boolean esParcial) { this.esParcial = esParcial; }
    public List<DetalleResponse> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleResponse> detalles) { this.detalles = detalles; }
    public List<HistorialResponse> getHistorial() { return historial; }
    public void setHistorial(List<HistorialResponse> historial) { this.historial = historial; }

    public static class DetalleResponse {
        private Long id;
        private Long loteId;
        private Long agricultorId;
        private Double cantidadSolicitada;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
        private String estadoDetalle;
        private String nombreProducto;
        private String unidadMedida;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getLoteId() { return loteId; }
        public void setLoteId(Long loteId) { this.loteId = loteId; }
        public Long getAgricultorId() { return agricultorId; }
        public void setAgricultorId(Long agricultorId) { this.agricultorId = agricultorId; }
        public Double getCantidadSolicitada() { return cantidadSolicitada; }
        public void setCantidadSolicitada(Double cantidadSolicitada) { this.cantidadSolicitada = cantidadSolicitada; }
        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
        public String getEstadoDetalle() { return estadoDetalle; }
        public void setEstadoDetalle(String estadoDetalle) { this.estadoDetalle = estadoDetalle; }
        public String getNombreProducto() { return nombreProducto; }
        public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
        public String getUnidadMedida() { return unidadMedida; }
        public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    }

    public static class HistorialResponse {
        private Long id;
        private String estadoAnterior;
        private String estadoNuevo;
        private String observacion;
        private LocalDateTime fechaCambio;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getEstadoAnterior() { return estadoAnterior; }
        public void setEstadoAnterior(String estadoAnterior) { this.estadoAnterior = estadoAnterior; }
        public String getEstadoNuevo() { return estadoNuevo; }
        public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }
        public String getObservacion() { return observacion; }
        public void setObservacion(String observacion) { this.observacion = observacion; }
        public LocalDateTime getFechaCambio() { return fechaCambio; }
        public void setFechaCambio(LocalDateTime fechaCambio) { this.fechaCambio = fechaCambio; }
    }
}