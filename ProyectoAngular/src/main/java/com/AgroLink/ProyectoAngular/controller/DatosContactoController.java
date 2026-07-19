package com.AgroLink.ProyectoAngular.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.AgroLink.ProyectoAngular.dto.ContactoPedidoResponse;
import com.AgroLink.ProyectoAngular.model.DatosContacto;
import com.AgroLink.ProyectoAngular.model.Usuario;
import com.AgroLink.ProyectoAngular.model.enums.RolEnum;
import com.AgroLink.ProyectoAngular.service.DatosContactoService;
import com.AgroLink.ProyectoAngular.service.UsuarioService;

@RestController
@RequestMapping("/api/datos-contacto")

public class DatosContactoController {

    private final DatosContactoService datosContactoService;
    private final UsuarioService usuarioService;

    public DatosContactoController(DatosContactoService datosContactoService, UsuarioService usuarioService) {
        this.datosContactoService = datosContactoService;
        this.usuarioService = usuarioService;
    }

    private Usuario usuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioService.buscarPorEmail(email);
    }

    // GET / y GET /{id} quedan restringidos a ADMINISTRADOR en SecurityConfig
    // (exponían direcciones, teléfonos y emails de todos los usuarios).

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

    /**
     * Un usuario solo puede crear/editar sus propios datos de contacto
     * (o un ADMINISTRADOR, en nombre de cualquiera).
     */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody DatosContacto contacto) {
        Usuario yo = usuarioAutenticado();
        if (yo == null) return ResponseEntity.status(401).build();
        if (yo.getRol() != RolEnum.ADMINISTRADOR) {
            // Se ignora cualquier usuarioId enviado por el cliente.
            contacto.setUsuarioId(yo.getId());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(datosContactoService.guardar(contacto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody DatosContacto contacto) {
        DatosContacto existente = datosContactoService.buscarPorId(id);
        if (existente == null) return ResponseEntity.notFound().build();

        Usuario yo = usuarioAutenticado();
        boolean esDueno = yo != null && yo.getId().equals(existente.getUsuarioId());
        boolean esAdmin = yo != null && yo.getRol() == RolEnum.ADMINISTRADOR;
        if (!esDueno && !esAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(java.util.Map.of("error", "Estos datos de contacto no te pertenecen"));
        }

        contacto.setId(id);
        contacto.setUsuarioId(existente.getUsuarioId()); // no se puede reasignar a otro usuario
        return ResponseEntity.ok(datosContactoService.guardar(contacto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        // Restringido a ADMINISTRADOR por SecurityConfig.
        if (datosContactoService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        datosContactoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * RF11 — Devuelve el contacto de la contraparte de un pedido, solo si el
     * pedido existe, el SOLICITANTE AUTENTICADO es una de las dos partes, y el
     * pedido está en un estado de coordinación autorizada (confirmado en adelante).
     *
     * GET /api/datos-contacto/pedido/{pedidoId}
     * (ya no recibe solicitanteId del cliente: se toma siempre del JWT)
     */
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<?> obtenerContactoPorPedido(@PathVariable Long pedidoId) {
        try {
            Usuario yo = usuarioAutenticado();
            if (yo == null) return ResponseEntity.status(401).build();
            ContactoPedidoResponse contacto = datosContactoService.obtenerContactoAutorizado(pedidoId, yo.getId());
            return ResponseEntity.ok(contacto);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
