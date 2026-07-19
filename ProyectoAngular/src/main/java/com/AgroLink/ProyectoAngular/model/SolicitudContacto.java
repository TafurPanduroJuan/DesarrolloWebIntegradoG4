package com.AgroLink.ProyectoAngular.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Solicitud enviada desde el formulario público de contacto del sitio
 * (landing page, /form). No requiere autenticación: cualquier visitante
 * puede dejar sus datos para que un administrador de AgroLink lo contacte.
 */
@Entity
@Table(name = "solicitud_contacto")
public class SolicitudContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 150)
    private String correo;

    @Column(length = 30)
    private String telefono;

    @Column(length = 50)
    private String tipo;

    @Column(nullable = false, length = 2000)
    private String mensaje;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private Boolean atendida = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Boolean getAtendida() { return atendida; }
    public void setAtendida(Boolean atendida) { this.atendida = atendida; }
}
