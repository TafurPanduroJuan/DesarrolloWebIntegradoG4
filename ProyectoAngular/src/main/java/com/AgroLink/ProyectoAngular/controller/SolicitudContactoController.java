package com.AgroLink.ProyectoAngular.controller;

import com.AgroLink.ProyectoAngular.model.SolicitudContacto;
import com.AgroLink.ProyectoAngular.service.SolicitudContactoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Formulario público de contacto (landing page, /form). El POST es público
 * (no requiere login: es la vía de contacto para gente que todavía no tiene
 * cuenta en AgroLink). La lectura/administración queda restringida a
 * ADMINISTRADOR (ver SecurityConfig).
 */
@RestController
@RequestMapping("/api/solicitudes-contacto")
public class SolicitudContactoController {

    private final SolicitudContactoService service;

    public SolicitudContactoController(SolicitudContactoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody SolicitudContacto solicitud) {
        try {
            SolicitudContacto guardada = service.registrar(solicitud);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<SolicitudContacto>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @PatchMapping("/{id}/atender")
    public ResponseEntity<?> marcarAtendida(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.marcarAtendida(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
