package com.AgroLink.ProyectoAngular.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.AgroLink.ProyectoAngular.model.MovimientoStock;
import com.AgroLink.ProyectoAngular.service.MovimientoStockService;

@RestController
@RequestMapping("/api/movimientos-stock")
@CrossOrigin(origins = "*")
public class MovimientoStockController {

    private final MovimientoStockService movimientoStockService;

    public MovimientoStockController(MovimientoStockService movimientoStockService) {
        this.movimientoStockService = movimientoStockService;
    }

    @GetMapping
    public ResponseEntity<List<MovimientoStock>> listarTodos() {
        return ResponseEntity.ok(movimientoStockService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimientoStock> buscarPorId(@PathVariable Long id) {
        MovimientoStock movimiento = movimientoStockService.buscarPorId(id);
        if (movimiento == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(movimiento);
    }

    @PostMapping
    public ResponseEntity<MovimientoStock> crear(@RequestBody MovimientoStock movimiento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movimientoStockService.guardar(movimiento));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovimientoStock> actualizar(@PathVariable Long id, @RequestBody MovimientoStock movimiento) {
        if (movimientoStockService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        movimiento.setId(id);
        return ResponseEntity.ok(movimientoStockService.guardar(movimiento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (movimientoStockService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        movimientoStockService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
