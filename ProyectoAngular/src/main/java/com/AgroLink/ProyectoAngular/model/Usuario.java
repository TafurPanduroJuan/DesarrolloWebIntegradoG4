package com.AgroLink.ProyectoAngular.model;

import com.AgroLink.ProyectoAngular.model.enums.RolEnum;
import com.AgroLink.ProyectoAngular.model.enums.TipoUsuarioEnum;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellido;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol")          
    private RolEnum rol;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private TipoUsuarioEnum tipo;

    private String dni;
    private String ruc;
    private String region;
    private Boolean activo = true;
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public RolEnum getRol() { return rol; }
    public void setRol(RolEnum rol) { this.rol = rol; }
    public TipoUsuarioEnum getTipo() { return tipo; }
    public void setTipo(TipoUsuarioEnum tipo) { this.tipo = tipo; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
