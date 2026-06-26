package com.AgroLink.ProyectoAngular.model;

import com.AgroLink.ProyectoAngular.model.enums.TipoEventoEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "evento_produccion")
public class EventoProduccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "cultivo_id", nullable = false)
    private Long cultivoId;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "tipo", nullable = false)
    private TipoEventoEnum tipo;

    @Column(length = 1000)
    private String descripcion;

    @Column(name = "impacto_estimado_pct")
    private Double impactoEstimadoPct;

    @Column(name = "fecha")
    private LocalDate fecha = LocalDate.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCultivoId() { return cultivoId; }
    public void setCultivoId(Long cultivoId) { this.cultivoId = cultivoId; }

    public TipoEventoEnum getTipo() { return tipo; }
    public void setTipo(TipoEventoEnum tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getImpactoEstimadoPct() { return impactoEstimadoPct; }
    public void setImpactoEstimadoPct(Double v) { this.impactoEstimadoPct = v; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
}