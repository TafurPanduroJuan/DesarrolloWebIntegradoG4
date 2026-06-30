package com.AgroLink.ProyectoAngular.dto;

public class ContactoPedidoResponse {

    private Long usuarioId;
    private String nombreUsuario;
    private String rolUsuario; // "AGRICULTOR" o "COMPRADOR" (el rol de la persona a la que pertenecen estos datos)
    private String direccion;
    private String referencia;
    private String emailContacto;
    private String telefonoAdicional;

    public ContactoPedidoResponse() {}

    public ContactoPedidoResponse(Long usuarioId, String nombreUsuario, String rolUsuario,
                                   String direccion, String referencia,
                                   String emailContacto, String telefonoAdicional) {
        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
        this.rolUsuario = rolUsuario;
        this.direccion = direccion;
        this.referencia = referencia;
        this.emailContacto = emailContacto;
        this.telefonoAdicional = telefonoAdicional;
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getRolUsuario() { return rolUsuario; }
    public void setRolUsuario(String rolUsuario) { this.rolUsuario = rolUsuario; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public String getEmailContacto() { return emailContacto; }
    public void setEmailContacto(String emailContacto) { this.emailContacto = emailContacto; }
    public String getTelefonoAdicional() { return telefonoAdicional; }
    public void setTelefonoAdicional(String telefonoAdicional) { this.telefonoAdicional = telefonoAdicional; }
}