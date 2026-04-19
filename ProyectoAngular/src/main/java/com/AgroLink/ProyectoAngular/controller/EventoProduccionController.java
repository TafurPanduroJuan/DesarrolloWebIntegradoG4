package com.AgroLink.ProyectoAngular.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.AgroLink.ProyectoAngular.model.EventoProduccion;
import com.AgroLink.ProyectoAngular.service.EventoProduccionService;

@RestController
@RequestMapping("/api/eventos-produccion")
@CrossOrigin(origins = "*")
public class EventoProduccionController {

    private final EventoProduccionService eventoProduccionService;

    public EventoProduccionController(EventoProduccionService eventoProduccionService) {
        this.eventoProduccionService = eventoProduccionService;
    }

    @GetMapping
    public ResponseEntity<List<EventoProduccion>> listarTodos() {
        return ResponseEntity.ok(eventoProduccionService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoProduccion> buscarPorId(@PathVariable Long id) {
        EventoProduccion evento = eventoProduccionService.buscarPorId(id);
        if (evento == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(evento);
    }

    @PostMapping
    public ResponseEntity<EventoProduccion> crear(@RequestBody EventoProduccion evento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoProduccionService.guardar(evento));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoProduccion> actualizar(@PathVariable Long id, @RequestBody EventoProduccion evento) {
        if (eventoProduccionService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        evento.setId(id);
        return ResponseEntity.ok(eventoProduccionService.guardar(evento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (eventoProduccionService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        eventoProduccionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
