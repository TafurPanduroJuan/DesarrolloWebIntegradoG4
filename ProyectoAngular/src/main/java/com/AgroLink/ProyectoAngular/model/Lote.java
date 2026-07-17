package com.AgroLink.ProyectoAngular.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.AgroLink.ProyectoAngular.model.enums.CalidadLoteEnum;
import com.AgroLink.ProyectoAngular.model.enums.EstadoLoteEnum;

import jakarta.persistence.*;


@Entity
@Table(name = "lote")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cultivoId;

    private Double cantidadKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "calidad")
    private CalidadLoteEnum calidad;

    private BigDecimal precioUnitario;
    private String unidadMedida;
    private Double stockDisponible;
    private LocalDate fechaCosecha;

    
    @Column(name = "fecha_entrega_estimada")
    private LocalDate fechaEntregaEstimada;

    private String condicionesEntrega;

    @Column(name = "imagen_url", columnDefinition = "TEXT")
    private String imagenUrl;

    private Boolean publicado = false;
    private LocalDateTime fechaPublicacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoLoteEnum estado = EstadoLoteEnum.ACTIVO;

    // ── Getters / Setters ─────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public Double getStockDisponible() { return stockDisponible; }
    public void setStockDisponible(Double stockDisponible) { this.stockDisponible = stockDisponible; }
    public LocalDate getFechaCosecha() { return fechaCosecha; }
    public void setFechaCosecha(LocalDate fechaCosecha) { this.fechaCosecha = fechaCosecha; }
    public LocalDate getFechaEntregaEstimada() { return fechaEntregaEstimada; }
    public void setFechaEntregaEstimada(LocalDate fechaEntregaEstimada) { this.fechaEntregaEstimada = fechaEntregaEstimada; }
    public String getCondicionesEntrega() { return condicionesEntrega; }
    public void setCondicionesEntrega(String condicionesEntrega) { this.condicionesEntrega = condicionesEntrega; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public Boolean getPublicado() { return publicado; }
    public void setPublicado(Boolean publicado) { this.publicado = publicado; }
    public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDateTime fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
    public EstadoLoteEnum getEstado() { return estado; }
    public void setEstado(EstadoLoteEnum estado) { this.estado = estado; }
}
