package com.AgroLink.ProyectoAngular.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.AgroLink.ProyectoAngular.dto.AjusteStockRequest;
import com.AgroLink.ProyectoAngular.dto.LotePublicacionRequest;
import com.AgroLink.ProyectoAngular.model.Lote;
import com.AgroLink.ProyectoAngular.model.MovimientoStock;
import com.AgroLink.ProyectoAngular.model.enums.EstadoLoteEnum;
import com.AgroLink.ProyectoAngular.model.enums.TipoMovimientoEnum;
import com.AgroLink.ProyectoAngular.repository.LoteRepository;
import com.AgroLink.ProyectoAngular.repository.MovimientoStockRepository;

/**
 * RF04 – Publicación de lotes comerciales.
 * RF09 – Control de stock con garantías ACID.
 *
 * Todas las operaciones que modifican stock son @Transactional para
 * asegurar atomicidad, consistencia, aislamiento y durabilidad.
 */
@Service
public class LoteService {

    private final LoteRepository loteRepository;
    private final MovimientoStockRepository movimientoRepository;

    public LoteService(LoteRepository loteRepository,
                       MovimientoStockRepository movimientoRepository) {
        this.loteRepository    = loteRepository;
        this.movimientoRepository = movimientoRepository;
    }

    // ── Consultas ────────────────────────────────────────────────

    public List<Lote> listarTodos() {
        return loteRepository.findAll();
    }

    public List<Lote> listarPublicados() {
        return loteRepository.findByPublicadoTrue();
    }

    public List<Lote> listarPorCultivo(Long cultivoId) {
        return loteRepository.findByCultivoId(cultivoId);
    }

    public Lote buscarPorId(Long id) {
        return loteRepository.findById(id).orElse(null);
    }

    // ── RF04: Publicar lote con validación completa ──────────────

    /**
     * Crea y publica un lote comercial en una sola transacción ACID.
     * Registra automáticamente el movimiento de stock ENTRADA inicial.
     */
    @Transactional
    public Lote publicarLote(LotePublicacionRequest req) {
        // ── Validaciones de negocio (RF04) ──
        if (req.getCantidadKg() == null || req.getCantidadKg() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (req.getPrecioUnitario() == null ||
                req.getPrecioUnitario().doubleValue() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        if (req.getFechaEntregaEstimada() == null) {
            throw new IllegalArgumentException("La fecha de entrega es obligatoria");
        }
        if (req.getCalidad() == null) {
            throw new IllegalArgumentException("La calidad del lote es obligatoria");
        }

        // ── Crear lote ──
        Lote lote = new Lote();
        lote.setCultivoId(req.getCultivoId());
        lote.setCantidadKg(req.getCantidadKg());
        lote.setCalidad(req.getCalidad());
        lote.setPrecioUnitario(req.getPrecioUnitario());
        lote.setUnidadMedida(req.getUnidadMedida());
        lote.setStockDisponible(req.getCantidadKg()); 
        lote.setFechaEntregaEstimada(req.getFechaEntregaEstimada());
        lote.setCondicionesEntrega(req.getCondicionesEntrega());
        lote.setPublicado(true);
        lote.setFechaPublicacion(LocalDateTime.now());
        lote.setEstado(EstadoLoteEnum.ACTIVO);

        Lote guardado = loteRepository.save(lote);

        
        registrarMovimiento(guardado.getId(), TipoMovimientoEnum.ENTRADA,
                req.getCantidadKg(), "Stock inicial al publicar lote");

        return guardado;
    }

    // ── RF09: Control de stock — operaciones atómicas ────────────

    /**
     * Descuenta stock al confirmar un pedido.
     * Lanza excepción si el resultado sería negativo (RNF05).
     */
    @Transactional
    public Lote confirmarPedido(Long loteId, Double cantidad) {
        Lote lote = obtenerLoteOFallar(loteId);
        double nuevoStock = lote.getStockDisponible() - cantidad;
        if (nuevoStock < 0) {
            throw new IllegalStateException(
                "Stock insuficiente. Disponible: " + lote.getStockDisponible() +
                ", solicitado: " + cantidad);
        }
        lote.setStockDisponible(nuevoStock);
        if (nuevoStock == 0) {
            lote.setEstado(EstadoLoteEnum.AGOTADO);
        }
        Lote actualizado = loteRepository.save(lote);
        registrarMovimiento(loteId, TipoMovimientoEnum.SALIDA, cantidad,
                "Salida por confirmación de pedido");
        return actualizado;
    }

    /**
     * Devuelve stock al cancelar un pedido.
     */
    @Transactional
    public Lote cancelarPedido(Long loteId, Double cantidad) {
        Lote lote = obtenerLoteOFallar(loteId);
        lote.setStockDisponible(lote.getStockDisponible() + cantidad);
        // Si estaba AGOTADO y se devuelve stock, volver a ACTIVO
        if (lote.getEstado() == EstadoLoteEnum.AGOTADO && lote.getStockDisponible() > 0) {
            lote.setEstado(EstadoLoteEnum.ACTIVO);
        }
        Lote actualizado = loteRepository.save(lote);
        registrarMovimiento(loteId, TipoMovimientoEnum.ENTRADA, cantidad,
                "Devolución por cancelación de pedido");
        return actualizado;
    }

    /**
     * Ajuste manual de stock (ENTRADA, SALIDA o AJUSTE).
     * Garantiza que el stock nunca quede negativo (RNF05).
     */
    @Transactional
    public Lote ajustarStock(Long loteId, AjusteStockRequest req) {
        Lote lote = obtenerLoteOFallar(loteId);

        double nuevoStock;
        switch (req.getTipo()) {
            case ENTRADA:
                nuevoStock = lote.getStockDisponible() + req.getCantidad();
                break;
            case SALIDA:
                nuevoStock = lote.getStockDisponible() - req.getCantidad();
                if (nuevoStock < 0) {
                    throw new IllegalStateException(
                        "El ajuste dejaría el stock en negativo. " +
                        "Disponible: " + lote.getStockDisponible());
                }
                break;
            case AJUSTE:
                // En AJUSTE, 'cantidad' es el nuevo valor absoluto del stock
                if (req.getCantidad() < 0) {
                    throw new IllegalArgumentException("El stock no puede ser negativo");
                }
                nuevoStock = req.getCantidad();
                break;
            default:
                throw new IllegalArgumentException("Tipo de movimiento desconocido");
        }

        lote.setStockDisponible(nuevoStock);
        actualizarEstadoPorStock(lote);
        Lote actualizado = loteRepository.save(lote);
        registrarMovimiento(loteId, req.getTipo(), req.getCantidad(), req.getMotivo());
        return actualizado;
    }

    // ── CRUD básico ──────────────────────────────────────────────

    public Lote guardar(Lote lote) {
        return loteRepository.save(lote);
    }

    public void eliminar(Long id) {
        loteRepository.deleteById(id);
    }

    // ── Helpers privados ─────────────────────────────────────────

    private Lote obtenerLoteOFallar(Long loteId) {
        Lote lote = loteRepository.findById(loteId).orElse(null);
        if (lote == null) {
            throw new IllegalArgumentException("Lote no encontrado: " + loteId);
        }
        return lote;
    }

    private void registrarMovimiento(Long loteId, TipoMovimientoEnum tipo,
                                     Double cantidad, String motivo) {
        MovimientoStock m = new MovimientoStock();
        m.setLoteId(loteId);
        m.setTipo(tipo);
        m.setCantidad(cantidad);
        m.setMotivo(motivo);
        m.setFechaMovimiento(LocalDateTime.now());
        movimientoRepository.save(m);
    }

    private void actualizarEstadoPorStock(Lote lote) {
        if (lote.getStockDisponible() == 0) {
            lote.setEstado(EstadoLoteEnum.AGOTADO);
        } else if (lote.getEstado() == EstadoLoteEnum.AGOTADO) {
            lote.setEstado(EstadoLoteEnum.ACTIVO);
        }
    }
}
