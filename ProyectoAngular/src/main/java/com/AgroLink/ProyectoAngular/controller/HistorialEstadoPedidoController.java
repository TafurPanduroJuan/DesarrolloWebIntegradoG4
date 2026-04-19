package com.AgroLink.ProyectoAngular.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.AgroLink.ProyectoAngular.model.HistorialEstadoPedido;
import com.AgroLink.ProyectoAngular.service.HistorialEstadoPedidoService;

@RestController
@RequestMapping("/api/historial-pedidos")
@CrossOrigin(origins = "*")
public class HistorialEstadoPedidoController {

    private final HistorialEstadoPedidoService historialService;

    public HistorialEstadoPedidoController(HistorialEstadoPedidoService historialService) {
        this.historialService = historialService;
    }

    @GetMapping
    public ResponseEntity<List<HistorialEstadoPedido>> listarTodos() {
        return ResponseEntity.ok(historialService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistorialEstadoPedido> buscarPorId(@PathVariable Long id) {
        HistorialEstadoPedido historial = historialService.buscarPorId(id);
        if (historial == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(historial);
    }

    @PostMapping
    public ResponseEntity<HistorialEstadoPedido> crear(@RequestBody HistorialEstadoPedido historial) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historialService.guardar(historial));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HistorialEstadoPedido> actualizar(@PathVariable Long id, @RequestBody HistorialEstadoPedido historial) {
        if (historialService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        historial.setId(id);
        return ResponseEntity.ok(historialService.guardar(historial));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (historialService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        historialService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
