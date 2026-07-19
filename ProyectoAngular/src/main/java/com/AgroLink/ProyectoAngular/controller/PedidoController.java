package com.AgroLink.ProyectoAngular.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.AgroLink.ProyectoAngular.dto.CambioEstadoRequest;
import com.AgroLink.ProyectoAngular.dto.PedidoRequest;
import com.AgroLink.ProyectoAngular.dto.PedidoResponse;
import com.AgroLink.ProyectoAngular.dto.RechazoRequest;
import com.AgroLink.ProyectoAngular.model.HistorialEstadoPedido;
import com.AgroLink.ProyectoAngular.model.Usuario;
import com.AgroLink.ProyectoAngular.model.enums.RolEnum;
import com.AgroLink.ProyectoAngular.service.PedidoService;
import com.AgroLink.ProyectoAngular.service.UsuarioService;

/**
 * RF07 - Registro de pedidos.
 * RF08 - Seguimiento con historial de estados.
 * RF12 - Observaciones del pedido.
 * RF18 - Confirmacion / rechazo por el agricultor.
 *
 * IMPORTANTE (seguridad): ningún dato de "quién soy" se toma del cliente
 * (body/query). Siempre se obtiene el usuario autenticado desde el JWT
 * (SecurityContextHolder) y se valida la propiedad del recurso antes de
 * devolver o modificar cualquier pedido.
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;

    public PedidoController(PedidoService pedidoService, UsuarioService usuarioService) {
        this.pedidoService = pedidoService;
        this.usuarioService = usuarioService;
    }

    private Usuario usuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioService.buscarPorEmail(email);
        if (usuario == null) {
            throw new IllegalStateException("Usuario autenticado no encontrado");
        }
        return usuario;
    }

    @PostMapping
    public ResponseEntity<?> crearPedido(@RequestBody PedidoRequest request) {
        try {
            Usuario comprador = usuarioAutenticado();
            if (comprador.getRol() != RolEnum.COMPRADOR) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo un comprador puede registrar pedidos"));
            }
            // Se ignora cualquier compradorId que venga en el body: el pedido
            // siempre se crea a nombre del usuario autenticado.
            request.setCompradorId(comprador.getId());

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
            PedidoResponse pedido = pedidoService.obtenerConHistorial(id);
            Usuario yo = usuarioAutenticado();
            if (!puedeVerPedido(yo, pedido)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No tienes permiso para ver este pedido"));
            }
            return ResponseEntity.ok(pedido);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/comprador/{compradorId}")
    public ResponseEntity<?> listarPorComprador(@PathVariable Long compradorId) {
        Usuario yo = usuarioAutenticado();
        if (yo.getRol() != RolEnum.ADMINISTRADOR && !yo.getId().equals(compradorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "No puedes consultar los pedidos de otro comprador"));
        }
        return ResponseEntity.ok(pedidoService.listarPorComprador(compradorId));
    }

    @GetMapping("/agricultor/{agricultorId}")
    public ResponseEntity<?> listarPorAgricultor(@PathVariable Long agricultorId) {
        Usuario yo = usuarioAutenticado();
        if (yo.getRol() != RolEnum.ADMINISTRADOR && !yo.getId().equals(agricultorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "No puedes consultar los pedidos de otro agricultor"));
        }
        return ResponseEntity.ok(pedidoService.listarPorAgricultor(agricultorId));
    }

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmarPedido(@PathVariable Long id) {
        try {
            Usuario agricultor = usuarioAutenticado();
            if (agricultor.getRol() != RolEnum.AGRICULTOR) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo un agricultor puede confirmar pedidos"));
            }
            // El agricultorId ya no se recibe del cliente: siempre es el usuario autenticado.
            return ResponseEntity.ok(pedidoService.confirmarPedido(id, agricultor.getId()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazarPedido(@PathVariable Long id,
                                            @RequestBody RechazoRequest request) {
        try {
            Usuario agricultor = usuarioAutenticado();
            if (agricultor.getRol() != RolEnum.AGRICULTOR) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo un agricultor puede rechazar pedidos"));
            }
            return ResponseEntity.ok(
                pedidoService.rechazarPedido(id, agricultor.getId(), request.getMotivo()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id,
                                           @RequestBody CambioEstadoRequest request) {
        try {
            PedidoResponse pedidoActual = pedidoService.obtenerConHistorial(id);
            Usuario yo = usuarioAutenticado();
            if (!puedeVerPedido(yo, pedidoActual)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No tienes permiso para modificar este pedido"));
            }
            return ResponseEntity.ok(
                pedidoService.cambiarEstado(id, request.getNuevoEstado(), request.getObservacion()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<?> obtenerHistorial(@PathVariable Long id) {
        try {
            PedidoResponse pedido = pedidoService.obtenerConHistorial(id);
            Usuario yo = usuarioAutenticado();
            if (!puedeVerPedido(yo, pedido)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No tienes permiso para ver este pedido"));
            }
            return ResponseEntity.ok(pedidoService.obtenerHistorial(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Un pedido solo puede verlo/modificarlo: el comprador dueño, alguno de los
     *  agricultores con un detalle en ese pedido, o un administrador. */
    private boolean puedeVerPedido(Usuario yo, PedidoResponse pedido) {
        if (yo.getRol() == RolEnum.ADMINISTRADOR) return true;
        if (yo.getRol() == RolEnum.COMPRADOR) {
            return yo.getId().equals(pedido.getCompradorId());
        }
        if (yo.getRol() == RolEnum.AGRICULTOR) {
            return pedido.getDetalles() != null && pedido.getDetalles().stream()
                .anyMatch(d -> yo.getId().equals(d.getAgricultorId()));
        }
        return false;
    }

    // CRUD basico (List<Pedido>) legado no se expone por API para no reabrir
    // el acceso sin control de propiedad; usar los endpoints RF07/RF08 de arriba.
}
