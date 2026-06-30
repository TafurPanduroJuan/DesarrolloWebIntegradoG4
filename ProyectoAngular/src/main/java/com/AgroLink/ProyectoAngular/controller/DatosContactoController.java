package com.AgroLink.ProyectoAngular.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.AgroLink.ProyectoAngular.dto.ContactoPedidoResponse;
import com.AgroLink.ProyectoAngular.model.DatosContacto;
import com.AgroLink.ProyectoAngular.service.DatosContactoService;

@RestController
@RequestMapping("/api/datos-contacto")

public class DatosContactoController {

    private final DatosContactoService datosContactoService;

    public DatosContactoController(DatosContactoService datosContactoService) {
        this.datosContactoService = datosContactoService;
    }

    @GetMapping
    public ResponseEntity<List<DatosContacto>> listarTodos() {
        return ResponseEntity.ok(datosContactoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatosContacto> buscarPorId(@PathVariable Long id) {
        DatosContacto contacto = datosContactoService.buscarPorId(id);
        if (contacto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(contacto);
    }

    @PostMapping
    public ResponseEntity<DatosContacto> crear(@RequestBody DatosContacto contacto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(datosContactoService.guardar(contacto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DatosContacto> actualizar(@PathVariable Long id, @RequestBody DatosContacto contacto) {
        if (datosContactoService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        contacto.setId(id);
        return ResponseEntity.ok(datosContactoService.guardar(contacto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (datosContactoService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        datosContactoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * RF11 — Devuelve el contacto de la contraparte de un pedido, solo si el
     * pedido existe, el solicitante es una de las dos partes, y el pedido
     * está en un estado de coordinación autorizada (confirmado en adelante).
     *
     * GET /api/datos-contacto/pedido/{pedidoId}?solicitanteId=123
     */
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<?> obtenerContactoPorPedido(
            @PathVariable Long pedidoId,
            @RequestParam Long solicitanteId) {
        try {
            ContactoPedidoResponse contacto = datosContactoService.obtenerContactoAutorizado(pedidoId, solicitanteId);
            return ResponseEntity.ok(contacto);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}