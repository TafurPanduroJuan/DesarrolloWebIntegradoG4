package com.AgroLink.ProyectoAngular.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.AgroLink.ProyectoAngular.model.DetallePedido;
import com.AgroLink.ProyectoAngular.service.DetallePedidoService;

@RestController
@RequestMapping("/api/detalle-pedidos")
@CrossOrigin(origins = "*")
public class DetallePedidoController {

    private final DetallePedidoService detallePedidoService;

    public DetallePedidoController(DetallePedidoService detallePedidoService) {
        this.detallePedidoService = detallePedidoService;
    }

    @GetMapping
    public ResponseEntity<List<DetallePedido>> listarTodos() {
        return ResponseEntity.ok(detallePedidoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetallePedido> buscarPorId(@PathVariable Long id) {
        DetallePedido detalle = detallePedidoService.buscarPorId(id);
        if (detalle == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(detalle);
    }

    @PostMapping
    public ResponseEntity<DetallePedido> crear(@RequestBody DetallePedido detalle) {
        return ResponseEntity.status(HttpStatus.CREATED).body(detallePedidoService.guardar(detalle));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DetallePedido> actualizar(@PathVariable Long id, @RequestBody DetallePedido detalle) {
        if (detallePedidoService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        detalle.setId(id);
        return ResponseEntity.ok(detallePedidoService.guardar(detalle));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (detallePedidoService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        detallePedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
