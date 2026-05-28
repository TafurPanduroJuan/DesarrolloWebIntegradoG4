package com.AgroLink.ProyectoAngular.dto;

import com.AgroLink.ProyectoAngular.model.enums.RolEnum;
import com.AgroLink.ProyectoAngular.model.enums.TipoCompradorEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RegisterRequest {

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellido;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String telefono;

    @NotNull
    private RolEnum rol;

    // --- Agricultor (RF13) ---
    private String dni;
    private String region;
    private String productoresPrincipales;
    private String descripcionFinca;

    // --- Comprador (RF14) ---
    private String ruc;
    private String razonSocial;
    private String direccionComercial;
    private TipoCompradorEnum tipoComprador;

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
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getProductoresPrincipales() { return productoresPrincipales; }
    public void setProductoresPrincipales(String v) { this.productoresPrincipales = v; }
    public String getDescripcionFinca() { return descripcionFinca; }
    public void setDescripcionFinca(String descripcionFinca) { this.descripcionFinca = descripcionFinca; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public String getDireccionComercial() { return direccionComercial; }
    public void setDireccionComercial(String direccionComercial) { this.direccionComercial = direccionComercial; }
    public TipoCompradorEnum getTipoComprador() { return tipoComprador; }
    public void setTipoComprador(TipoCompradorEnum tipoComprador) { this.tipoComprador = tipoComprador; }
}
