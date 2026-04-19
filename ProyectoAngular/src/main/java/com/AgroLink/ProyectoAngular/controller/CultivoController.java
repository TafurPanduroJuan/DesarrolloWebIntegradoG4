package com.AgroLink.ProyectoAngular.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.AgroLink.ProyectoAngular.model.Cultivo;
import com.AgroLink.ProyectoAngular.service.CultivoService;

@RestController
@RequestMapping("/api/cultivos")
@CrossOrigin(origins = "*")
public class CultivoController {

    private final CultivoService cultivoService;

    public CultivoController(CultivoService cultivoService) {
        this.cultivoService = cultivoService;
    }

    @GetMapping
    public ResponseEntity<List<Cultivo>> listarTodos() {
        return ResponseEntity.ok(cultivoService.listarTodos());
    }

    @GetMapping("/agricultor/{agricultorId}")
    public ResponseEntity<List<Cultivo>> listarPorAgricultor(@PathVariable Long agricultorId) {
        return ResponseEntity.ok(cultivoService.listarPorAgricultor(agricultorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cultivo> buscarPorId(@PathVariable Long id) {
        Cultivo cultivo = cultivoService.buscarPorId(id);
        if (cultivo == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(cultivo);
    }

    @PostMapping
    public ResponseEntity<Cultivo> crear(@RequestBody Cultivo cultivo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cultivoService.guardar(cultivo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cultivo> actualizar(@PathVariable Long id, @RequestBody Cultivo cultivo) {
        if (cultivoService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        cultivo.setId(id);
        return ResponseEntity.ok(cultivoService.guardar(cultivo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (cultivoService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        cultivoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
