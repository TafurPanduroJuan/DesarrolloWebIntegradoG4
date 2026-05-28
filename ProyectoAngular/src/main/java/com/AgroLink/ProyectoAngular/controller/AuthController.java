package com.AgroLink.ProyectoAngular.controller;

import com.AgroLink.ProyectoAngular.dto.JwtResponse;
import com.AgroLink.ProyectoAngular.dto.LoginRequest;
import com.AgroLink.ProyectoAngular.dto.RegisterRequest;
import com.AgroLink.ProyectoAngular.model.Usuario;
import com.AgroLink.ProyectoAngular.security.jwt.JwtUtils;
import com.AgroLink.ProyectoAngular.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * RF13 - Registro agricultor
 * RF14 - Registro comprador
 * RNF08 - JWT
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * POST /api/auth/login
     * Devuelve JWT token firmado con HMAC-SHA256
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            Usuario usuario = usuarioService.buscarPorEmail(loginRequest.getEmail());

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    usuario.getId(),
                    usuario.getNombre() + " " + usuario.getApellido(),
                    usuario.getEmail(),
                    usuario.getRol().name()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }
    }

    /**
     * POST /api/auth/register
     * RF13 - agricultor queda PENDIENTE
     * RF14 - comprador se activa automáticamente
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            Usuario usuario = usuarioService.registrar(registerRequest);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Usuario registrado exitosamente",
                    "id", usuario.getId(),
                    "rol", usuario.getRol().name(),
                    "activo", usuario.getActivo()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
