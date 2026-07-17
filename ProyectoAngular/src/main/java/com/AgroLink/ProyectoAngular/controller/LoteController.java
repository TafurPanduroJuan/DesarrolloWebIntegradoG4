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
import com.AgroLink.ProyectoAngular.model.Lote;
import com.AgroLink.ProyectoAngular.service.LoteService;

/**
 * RF04 – Publicación y gestión de lotes comerciales.
 * RF09 – Control de stock (confirmar / cancelar / ajustar).
 */
@RestController
@RequestMapping("/api/lotes")
public class LoteController {

    private final LoteService loteService;

    public LoteController(LoteService loteService) {
        this.loteService = loteService;
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
        try {
            Lote lote = loteService.publicarLote(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(lote);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── RF09: Control de stock ────────────────────────────────────

    /**
     * PATCH /api/lotes/{id}/confirmar?cantidad=X
     * Descuenta stock al confirmar un pedido.
     */
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmarPedido(@PathVariable Long id,
                                              @RequestParam Double cantidad) {
        try {
            return ResponseEntity.ok(loteService.confirmarPedido(id, cantidad));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/lotes/{id}/cancelar?cantidad=X
     * Devuelve stock al cancelar un pedido.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarPedido(@PathVariable Long id,
                                             @RequestParam Double cantidad) {
        try {
            return ResponseEntity.ok(loteService.cancelarPedido(id, cantidad));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/lotes/{id}/stock
     * Ajuste manual de stock (ENTRADA / SALIDA / AJUSTE).
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<?> ajustarStock(@PathVariable Long id,
                                           @Valid @RequestBody AjusteStockRequest req) {
        try {
            return ResponseEntity.ok(loteService.ajustarStock(id, req));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── CRUD básico ──────────────────────────────────────────────

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

    @PatchMapping("/{id}/precio")
    public ResponseEntity<?> actualizarPrecio(@PathVariable Long id, @RequestBody Map<String, Object> body) {
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
