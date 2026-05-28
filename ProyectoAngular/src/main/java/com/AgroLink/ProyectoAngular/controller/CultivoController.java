package com.AgroLink.ProyectoAngular.controller;

import com.AgroLink.ProyectoAngular.dto.SeguimientoRequest;
import com.AgroLink.ProyectoAngular.model.Cultivo;
import com.AgroLink.ProyectoAngular.service.CultivoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * RF02  - Registro de cultivos
 * RF03  - Seguimiento de producción (PATCH /{id}/seguimiento)
 * RF16  - Gestión de lotes agrícolas (campo nombreLote en Cultivo)
 */
@RestController
@RequestMapping("/api/cultivos")
@CrossOrigin(origins = "*")
public class CultivoController {

    @Autowired
    private CultivoService cultivoService;

    // Listar todos - cualquier autenticado
    @GetMapping
    public ResponseEntity<List<Cultivo>> listarTodos() {
        return ResponseEntity.ok(cultivoService.listarTodos());
    }

    // Listar por agricultor
    @GetMapping("/agricultor/{agricultorId}")
    public ResponseEntity<List<Cultivo>> listarPorAgricultor(@PathVariable Long agricultorId) {
        return ResponseEntity.ok(cultivoService.listarPorAgricultor(agricultorId));
    }

    // Buscar por id
    @GetMapping("/{id}")
    public ResponseEntity<Cultivo> buscarPorId(@PathVariable Long id) {
        Cultivo cultivo = cultivoService.buscarPorId(id);
        if (cultivo == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(cultivo);
    }

    // RF02 / RF16 - Crear cultivo (solo AGRICULTOR)
    @PostMapping
    @PreAuthorize("hasRole('AGRICULTOR')")
    public ResponseEntity<Cultivo> crear(@Valid @RequestBody Cultivo cultivo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cultivoService.crear(cultivo));
    }

    // RF02 / RF16 - Actualizar cultivo (solo AGRICULTOR)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AGRICULTOR')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Cultivo cultivo) {
        try {
            return ResponseEntity.ok(cultivoService.actualizar(id, cultivo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // RF03 - Actualizar seguimiento de producción (solo AGRICULTOR)
    @PatchMapping("/{id}/seguimiento")
    @PreAuthorize("hasRole('AGRICULTOR')")
    public ResponseEntity<?> actualizarSeguimiento(@PathVariable Long id,
                                                    @Valid @RequestBody SeguimientoRequest req) {
        try {
            return ResponseEntity.ok(cultivoService.actualizarSeguimiento(id, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Eliminar (solo AGRICULTOR)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('AGRICULTOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (cultivoService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        cultivoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
