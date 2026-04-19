package com.AgroLink.ProyectoAngular.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.AgroLink.ProyectoAngular.model.enums.CategoriaProductoEnum;
import com.AgroLink.ProyectoAngular.model.enums.EstadoCultivoEnum;

import jakarta.persistence.*;

@Entity
@Table(name = "cultivo")
public class Cultivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long agricultorId;
    private String nombreProducto;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "categoria_producto_enum")
    private CategoriaProductoEnum categoria;

    private Double areaTerrenoHa;
    private String ubicacion;
    private String descripcion;
    private LocalDate fechaSiembra;
    private LocalDate fechaCosechaEstimada;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "estado_cultivo_enum")
    private EstadoCultivoEnum estado;

    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAgricultorId() { return agricultorId; }
    public void setAgricultorId(Long agricultorId) { this.agricultorId = agricultorId; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public CategoriaProductoEnum getCategoria() { return categoria; }
    public void setCategoria(CategoriaProductoEnum categoria) { this.categoria = categoria; }
    public Double getAreaTerrenoHa() { return areaTerrenoHa; }
    public void setAreaTerrenoHa(Double areaTerrenoHa) { this.areaTerrenoHa = areaTerrenoHa; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDate getFechaSiembra() { return fechaSiembra; }
    public void setFechaSiembra(LocalDate fechaSiembra) { this.fechaSiembra = fechaSiembra; }
    public LocalDate getFechaCosechaEstimada() { return fechaCosechaEstimada; }
    public void setFechaCosechaEstimada(LocalDate fechaCosechaEstimada) { this.fechaCosechaEstimada = fechaCosechaEstimada; }
    public EstadoCultivoEnum getEstado() { return estado; }
    public void setEstado(EstadoCultivoEnum estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
