package com.AgroLink.ProyectoAngular.dto;

public class JwtResponse {

    private String token;
    private final String tipo = "Bearer";
    private Long id;
    private String nombre;
    private String email;
    private String rol;

    public JwtResponse(String token, Long id, String nombre, String email, String rol) {
        this.token = token;
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
    }

    public String getToken() { return token; }
    public String getTipo() { return tipo; }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getRol() { return rol; }
}
