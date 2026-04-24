package com.AgroLink.ProyectoAngular.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.AgroLink.ProyectoAngular.model.enums.TipoMovimientoEnum;

@Entity
@Table(name = "movimiento_stock")
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long loteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private TipoMovimientoEnum tipo;

    private Double cantidad;
    private String motivo;
    private LocalDateTime fechaMovimiento = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }
    public TipoMovimientoEnum getTipo() { return tipo; }
    public void setTipo(TipoMovimientoEnum tipo) { this.tipo = tipo; }
    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public LocalDateTime getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDateTime fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }
}
