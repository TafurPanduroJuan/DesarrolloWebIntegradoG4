package com.AgroLink.ProyectoAngular.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.AgroLink.ProyectoAngular.dto.CambioEstadoRequest;
import com.AgroLink.ProyectoAngular.dto.PedidoRequest;
import com.AgroLink.ProyectoAngular.dto.PedidoResponse;
import com.AgroLink.ProyectoAngular.dto.RechazoRequest;
import com.AgroLink.ProyectoAngular.model.HistorialEstadoPedido;
import com.AgroLink.ProyectoAngular.service.PedidoService;

/**
 * RF07 - Registro de pedidos.
 * RF08 - Seguimiento con historial de estados.
 * RF12 - Observaciones del pedido.
 * RF18 - Confirmacion / rechazo por el agricultor.
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<?> crearPedido(@RequestBody PedidoRequest request) {
        try {
            PedidoResponse pedido = pedidoService.crearPedido(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPedido(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(pedidoService.obtenerConHistorial(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/comprador/{compradorId}")
    public ResponseEntity<List<PedidoResponse>> listarPorComprador(@PathVariable Long compradorId) {
        return ResponseEntity.ok(pedidoService.listarPorComprador(compradorId));
    }

    @GetMapping("/agricultor/{agricultorId}")
    public ResponseEntity<List<PedidoResponse>> listarPorAgricultor(@PathVariable Long agricultorId) {
        return ResponseEntity.ok(pedidoService.listarPorAgricultor(agricultorId));
    }

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmarPedido(@PathVariable Long id,
                                             @RequestParam Long agricultorId) {
        try {
            return ResponseEntity.ok(pedidoService.confirmarPedido(id, agricultorId));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazarPedido(@PathVariable Long id,
                                            @RequestBody RechazoRequest request) {
        try {
            return ResponseEntity.ok(
                pedidoService.rechazarPedido(id, request.getAgricultorId(), request.getMotivo()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id,
                                           @RequestBody CambioEstadoRequest request) {
        try {
            return ResponseEntity.ok(
                pedidoService.cambiarEstado(id, request.getNuevoEstado(), request.getObservacion()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialEstadoPedido>> obtenerHistorial(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerHistorial(id));
    }
}
