package com.AgroLink.ProyectoAngular.controller;

import com.AgroLink.ProyectoAngular.model.EventoProduccion;
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
 * RF17 - Gestión de eventos de producción
 * Tipos: SIEMBRA, FERTILIZACION, RIEGO, CONTROL_PLAGAS, COSECHA, PERDIDA_PARCIAL
 */
@RestController
@RequestMapping("/api/eventos-produccion")

public class EventoProduccionController {

    @Autowired
    private CultivoService cultivoService;

    // Listar por cultivo - cualquier autenticado
    @GetMapping("/cultivo/{cultivoId}")
    public ResponseEntity<List<EventoProduccion>> listarPorCultivo(@PathVariable Long cultivoId) {
        return ResponseEntity.ok(cultivoService.listarEventosPorCultivo(cultivoId));
    }

    // Buscar por id
    @GetMapping("/{id}")
    public ResponseEntity<EventoProduccion> buscarPorId(@PathVariable Long id) {
        EventoProduccion evento = cultivoService.buscarEventoPorId(id);
        if (evento == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(evento);
    }

    // RF17 - Registrar evento (solo AGRICULTOR)
    @PostMapping
    @PreAuthorize("hasRole('AGRICULTOR')")
    public ResponseEntity<?> crear(@Valid @RequestBody EventoProduccion evento) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(cultivoService.registrarEvento(evento));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Actualizar evento (solo AGRICULTOR)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AGRICULTOR')")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @RequestBody EventoProduccion evento) {
        try {
            return ResponseEntity.ok(cultivoService.actualizarEvento(id, evento));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Eliminar (solo AGRICULTOR)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('AGRICULTOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (cultivoService.buscarEventoPorId(id) == null) return ResponseEntity.notFound().build();
        cultivoService.eliminarEvento(id);
        return ResponseEntity.noContent().build();
    }
}
