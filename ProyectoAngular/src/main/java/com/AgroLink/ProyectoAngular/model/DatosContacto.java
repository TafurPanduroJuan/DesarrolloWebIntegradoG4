package com.AgroLink.ProyectoAngular.model;
import jakarta.persistence.*;

@Entity
@Table(name = "datos_contacto")
public class DatosContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long usuarioId;

    private String direccion;
    private String referencia;
    private String emailContacto;
    private String telefonoAdicional;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getUsuarioId() {
        return usuarioId;
    }
    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
    public String getDireccion() {
        return direccion;
    }
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    public String getReferencia() {
        return referencia;
    }
    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
    public String getEmailContacto() {
        return emailContacto;
    }
    public void setEmailContacto(String emailContacto) {
        this.emailContacto = emailContacto;
    }
    public String getTelefonoAdicional() {
        return telefonoAdicional;
    }
    public void setTelefonoAdicional(String telefonoAdicional) {
        this.telefonoAdicional = telefonoAdicional;
    }

    
}