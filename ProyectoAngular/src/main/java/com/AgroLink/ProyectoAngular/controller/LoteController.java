package com.AgroLink.ProyectoAngular.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.AgroLink.ProyectoAngular.model.Lote;
import com.AgroLink.ProyectoAngular.service.LoteService;

@RestController
@RequestMapping("/api/lotes")
@CrossOrigin(origins = "*")
public class LoteController {

    private final LoteService loteService;

    public LoteController(LoteService loteService) {
        this.loteService = loteService;
    }

    @GetMapping
    public ResponseEntity<List<Lote>> listarTodos() {
        return ResponseEntity.ok(loteService.listarTodos());
    }

    @GetMapping("/publicados")
    public ResponseEntity<List<Lote>> listarPublicados() {
        return ResponseEntity.ok(loteService.listarPublicados());
    }

    @GetMapping("/cultivo/{cultivoId}")
    public ResponseEntity<List<Lote>> listarPorCultivo(@PathVariable Long cultivoId) {
        return ResponseEntity.ok(loteService.listarPorCultivo(cultivoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lote> buscarPorId(@PathVariable Long id) {
        Lote lote = loteService.buscarPorId(id);
        if (lote == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(lote);
    }

    @PostMapping
    public ResponseEntity<Lote> crear(@RequestBody Lote lote) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loteService.guardar(lote));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lote> actualizar(@PathVariable Long id, @RequestBody Lote lote) {
        if (loteService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        lote.setId(id);
        return ResponseEntity.ok(loteService.guardar(lote));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (loteService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        loteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
