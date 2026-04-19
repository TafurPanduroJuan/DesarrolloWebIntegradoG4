package com.AgroLink.ProyectoAngular.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.AgroLink.ProyectoAngular.model.enums.TipoEventoEnum;

@Entity
@Table(name = "evento_produccion")
public class EventoProduccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cultivoId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "tipo_evento_enum")
    private TipoEventoEnum tipo;

    private String descripcion;
    private Double impactoEstimadoPct;
    private LocalDateTime fecha = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCultivoId() { return cultivoId; }
    public void setCultivoId(Long cultivoId) { this.cultivoId = cultivoId; }
    public TipoEventoEnum getTipo() { return tipo; }
    public void setTipo(TipoEventoEnum tipo) { this.tipo = tipo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Double getImpactoEstimadoPct() { return impactoEstimadoPct; }
    public void setImpactoEstimadoPct(Double impactoEstimadoPct) { this.impactoEstimadoPct = impactoEstimadoPct; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
