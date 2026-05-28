package com.AgroLink.ProyectoAngular.model;

import com.AgroLink.ProyectoAngular.model.enums.CategoriaProductoEnum;
import com.AgroLink.ProyectoAngular.model.enums.EstadoCultivoEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cultivo")
public class Cultivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "agricultor_id", nullable = false)
    private Long agricultorId;

    @NotBlank
    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto;

    private String variedad;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria")
    private CategoriaProductoEnum categoria;

    // RF16 - lote agrícola
    @Column(name = "nombre_lote")
    private String nombreLote;

    @Column(name = "area_ha")
    private Double areaHa;

    private String ubicacion;
    private String descripcion;

    @Column(name = "fecha_siembra")
    private LocalDate fechaSiembra;

    @Column(name = "fecha_cosecha_estimada")
    private LocalDate fechaCosechaEstimada;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoCultivoEnum estado = EstadoCultivoEnum.SEMBRADO;

    // RF03 - seguimiento: etapa productiva y observación
    @Column(name = "etapa_productiva")
    private String etapaProductiva;

    @Column(name = "observacion_seguimiento", length = 1000)
    private String observacionSeguimiento;

    @Column(name = "fecha_ultimo_seguimiento")
    private LocalDateTime fechaUltimoSeguimiento;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // ---------- Getters & Setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAgricultorId() { return agricultorId; }
    public void setAgricultorId(Long agricultorId) { this.agricultorId = agricultorId; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getVariedad() { return variedad; }
    public void setVariedad(String variedad) { this.variedad = variedad; }

    public CategoriaProductoEnum getCategoria() { return categoria; }
    public void setCategoria(CategoriaProductoEnum categoria) { this.categoria = categoria; }

    public String getNombreLote() { return nombreLote; }
    public void setNombreLote(String nombreLote) { this.nombreLote = nombreLote; }

    public Double getAreaHa() { return areaHa; }
    public void setAreaHa(Double areaHa) { this.areaHa = areaHa; }

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

    public String getEtapaProductiva() { return etapaProductiva; }
    public void setEtapaProductiva(String etapaProductiva) { this.etapaProductiva = etapaProductiva; }

    public String getObservacionSeguimiento() { return observacionSeguimiento; }
    public void setObservacionSeguimiento(String observacionSeguimiento) { this.observacionSeguimiento = observacionSeguimiento; }

    public LocalDateTime getFechaUltimoSeguimiento() { return fechaUltimoSeguimiento; }
    public void setFechaUltimoSeguimiento(LocalDateTime fechaUltimoSeguimiento) { this.fechaUltimoSeguimiento = fechaUltimoSeguimiento; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
