package com.AgroLink.ProyectoAngular.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.AgroLink.ProyectoAngular.dto.AjusteStockRequest;
import com.AgroLink.ProyectoAngular.dto.LoteCatalogoResponse;
import com.AgroLink.ProyectoAngular.dto.LotePublicacionRequest;
import com.AgroLink.ProyectoAngular.model.Cultivo;
import com.AgroLink.ProyectoAngular.model.Lote;
import com.AgroLink.ProyectoAngular.model.Usuario;
import com.AgroLink.ProyectoAngular.service.CultivoService;
import com.AgroLink.ProyectoAngular.service.LoteService;
import com.AgroLink.ProyectoAngular.service.UsuarioService;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * RF04 – Publicación y gestión de lotes comerciales.
 * RF09 – Control de stock (ajustar).
 *
 * Todas las operaciones de escritura validan que el lote (a través de su
 * cultivo) pertenezca al agricultor autenticado.
 */
@RestController
@RequestMapping("/api/lotes")
public class LoteController {

    private final LoteService loteService;
    private final CultivoService cultivoService;
    private final UsuarioService usuarioService;

    public LoteController(LoteService loteService, CultivoService cultivoService, UsuarioService usuarioService) {
        this.loteService = loteService;
        this.cultivoService = cultivoService;
        this.usuarioService = usuarioService;
    }

    private Usuario usuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioService.buscarPorEmail(email);
    }

    /** Un lote pertenece al agricultor dueño del cultivo asociado. */
    private boolean esPropietarioDelCultivo(Long cultivoId, Usuario yo) {
        if (yo == null) return false;
        Cultivo cultivo = cultivoService.buscarPorId(cultivoId);
        return cultivo != null && cultivo.getAgricultorId().equals(yo.getId());
    }

    private boolean esPropietarioDelLote(Lote lote, Usuario yo) {
        return lote != null && esPropietarioDelCultivo(lote.getCultivoId(), yo);
    }

    // ── Consultas ────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<Lote>> listarTodos() {
        return ResponseEntity.ok(loteService.listarTodos());
    }

    @GetMapping("/publicados")
    public ResponseEntity<List<Lote>> listarPublicados() {
        return ResponseEntity.ok(loteService.listarPublicados());
    }

    /**
     * GET /api/lotes/publicados/buscar
     * RF05 – Catálogo con filtros opcionales.
     * RF25 – Búsqueda avanzada (busqueda, categoria, calidad, precio, ubicacion, fechas).
     * RF21 – El campo calidad filtra por PRIMERA / SEGUNDA / TERCERA.
     * RNF02 – El query usa índices y JOIN optimizado; responde en < 2 s.
     *
     * Todos los parámetros son opcionales. Sin parámetros devuelve el catálogo completo.
     */
    @GetMapping("/publicados/buscar")
    public ResponseEntity<List<LoteCatalogoResponse>> buscarCatalogo(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String calidad,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) String ubicacion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {

        List<LoteCatalogoResponse> resultado = loteService.buscarCatalogo(
            calidad, precioMin, precioMax, categoria, ubicacion, busqueda, fechaDesde, fechaHasta
        );
        return ResponseEntity.ok(resultado);
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

    // ── RF04: Publicar lote con validación ───────────────────────

    /**
     * POST /api/lotes/publicar
     * Crea y publica un lote comercial con validación de todos los campos.
     * Registra stock inicial automáticamente (transacción ACID).
     */
    @PostMapping("/publicar")
    public ResponseEntity<?> publicarLote(@Valid @RequestBody LotePublicacionRequest req) {
        Usuario yo = usuarioAutenticado();
        if (!esPropietarioDelCultivo(req.getCultivoId(), yo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Solo puedes publicar lotes de tus propios cultivos"));
        }
        try {
            Lote lote = loteService.publicarLote(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(lote);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── RF09: Control de stock ────────────────────────────────────
    //
    // NOTA DE SEGURIDAD: confirmarPedido()/cancelarPedido() de LoteService ya
    // NO se exponen como endpoints HTTP públicos. El único flujo legítimo para
    // descontar/devolver stock es a través de PedidoService (confirmar/rechazar/
    // cancelar un pedido), que invoca esos métodos internamente en Java. Un
    // endpoint público aquí permitía a cualquier usuario autenticado alterar el
    // stock de cualquier lote sin que existiera un pedido real detrás.

    /**
     * PATCH /api/lotes/{id}/stock
     * Ajuste manual de stock (ENTRADA / SALIDA / AJUSTE).
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<?> ajustarStock(@PathVariable Long id,
                                           @Valid @RequestBody AjusteStockRequest req) {
        Lote lote = loteService.buscarPorId(id);
        if (lote == null) return ResponseEntity.notFound().build();
        if (!esPropietarioDelLote(lote, usuarioAutenticado())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Este lote no te pertenece"));
        }
        try {
            return ResponseEntity.ok(loteService.ajustarStock(id, req));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── CRUD básico ──────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Lote lote) {
        Usuario yo = usuarioAutenticado();
        if (lote.getCultivoId() == null || !esPropietarioDelCultivo(lote.getCultivoId(), yo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Solo puedes crear lotes de tus propios cultivos"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(loteService.guardar(lote));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Lote lote) {
        Lote existente = loteService.buscarPorId(id);
        if (existente == null) return ResponseEntity.notFound().build();
        Usuario yo = usuarioAutenticado();
        if (!esPropietarioDelLote(existente, yo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Este lote no te pertenece"));
        }
        // El cultivoId de un lote no puede reasignarse a un cultivo ajeno por esta vía.
        lote.setId(id);
        lote.setCultivoId(existente.getCultivoId());
        return ResponseEntity.ok(loteService.guardar(lote));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        Lote existente = loteService.buscarPorId(id);
        if (existente == null) return ResponseEntity.notFound().build();
        if (!esPropietarioDelLote(existente, usuarioAutenticado())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Este lote no te pertenece"));
        }
        loteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/precio")
    public ResponseEntity<?> actualizarPrecio(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Lote existente = loteService.buscarPorId(id);
        if (existente == null) return ResponseEntity.notFound().build();
        Usuario yo = usuarioAutenticado();
        boolean autorizado = (yo != null && yo.getRol() != null && yo.getRol().name().equals("ADMINISTRADOR"))
            || esPropietarioDelLote(existente, yo);
        if (!autorizado) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Este lote no te pertenece"));
        }

        Object precioObj = body.get("precio");
        String motivo = (String) body.get("motivo");
        
        if (precioObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "El campo 'precio' es obligatorio"));
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El campo 'motivo' es obligatorio"));
        }
        
        BigDecimal nuevoPrecio;
        try {
            nuevoPrecio = new BigDecimal(precioObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "El precio debe ser un número válido"));
        }
        
        try {
            Lote updated = loteService.actualizarPrecio(id, nuevoPrecio, motivo);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/precio-historial")
    public ResponseEntity<?> obtenerPrecioHistorial(@PathVariable Long id) {
        if (loteService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(loteService.obtenerHistorialPrecios(id));
    }
}
