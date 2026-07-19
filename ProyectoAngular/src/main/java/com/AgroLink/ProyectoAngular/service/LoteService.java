package com.AgroLink.ProyectoAngular.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.AgroLink.ProyectoAngular.dto.AjusteStockRequest;
import com.AgroLink.ProyectoAngular.dto.LoteCatalogoResponse;
import com.AgroLink.ProyectoAngular.dto.LotePublicacionRequest;
import com.AgroLink.ProyectoAngular.model.Cultivo;
import com.AgroLink.ProyectoAngular.model.Lote;
import com.AgroLink.ProyectoAngular.model.MovimientoStock;
import com.AgroLink.ProyectoAngular.model.enums.CalidadLoteEnum;
import com.AgroLink.ProyectoAngular.model.enums.EstadoLoteEnum;
import com.AgroLink.ProyectoAngular.model.enums.TipoMovimientoEnum;
import com.AgroLink.ProyectoAngular.repository.CultivoRepository;
import com.AgroLink.ProyectoAngular.repository.LoteRepository;
import com.AgroLink.ProyectoAngular.repository.MovimientoStockRepository;
import com.AgroLink.ProyectoAngular.repository.HistorialPrecioRepository;
import com.AgroLink.ProyectoAngular.repository.UsuarioRepository;
import com.AgroLink.ProyectoAngular.model.HistorialPrecio;
import com.AgroLink.ProyectoAngular.model.Usuario;
import com.AgroLink.ProyectoAngular.model.enums.RolEnum;
import org.springframework.security.core.context.SecurityContextHolder;

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
    private final CultivoRepository cultivoRepository;
    private final HistorialPrecioRepository historialPrecioRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final NotificacionService notificacionService;

    public LoteService(LoteRepository loteRepository,
                       MovimientoStockRepository movimientoRepository,
                       CultivoRepository cultivoRepository,
                       HistorialPrecioRepository historialPrecioRepository,
                       UsuarioRepository usuarioRepository,
                       AuditoriaService auditoriaService,
                       NotificacionService notificacionService) {
        this.loteRepository            = loteRepository;
        this.movimientoRepository      = movimientoRepository;
        this.cultivoRepository         = cultivoRepository;
        this.historialPrecioRepository = historialPrecioRepository;
        this.usuarioRepository         = usuarioRepository;
        this.auditoriaService          = auditoriaService;
        this.notificacionService       = notificacionService;
    }

    // ── Consultas ────────────────────────────────────────────────

    public List<Lote> listarTodos() {
        return loteRepository.findAll();
    }

    public List<Lote> listarPublicados() {
        return loteRepository.findByPublicadoTrue();
    }

    /**
     * RF05 / RF25 — Búsqueda del catálogo con filtros opcionales.
     * Devuelve LoteCatalogoResponse enriquecido con datos del Cultivo
     * para que el frontend no necesite hacer llamadas extra.
     */
    public List<LoteCatalogoResponse> buscarCatalogo(
            String calidad,
            BigDecimal precioMin,
            BigDecimal precioMax,
            String categoria,
            String ubicacion,
            String busqueda,
            LocalDate fechaDesde,
            LocalDate fechaHasta) {

        CalidadLoteEnum calidadEnum = null;
        if (calidad != null && !calidad.isBlank()) {
            try { calidadEnum = CalidadLoteEnum.valueOf(calidad.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }

        // Con nativeQuery=true los parámetros deben ser String/primitivos,
        // no enums ni LocalDate — los casteamos aquí antes de llamar al repo.
        String calidadStr   = calidadEnum != null ? calidadEnum.name() : null;
        String categoriaStr = (categoria != null && !categoria.isBlank())
                              ? categoria.toUpperCase() : null;
        String fechaDesdeStr = fechaDesde != null ? fechaDesde.toString() : null;
        String fechaHastaStr = fechaHasta != null ? fechaHasta.toString() : null;

        List<Lote> lotes = loteRepository.buscarConFiltros(
            calidadStr, precioMin, precioMax,
            categoriaStr, ubicacion, busqueda,
            fechaDesdeStr, fechaHastaStr
        );

        return lotes.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private LoteCatalogoResponse toResponse(Lote l) {
        LoteCatalogoResponse r = new LoteCatalogoResponse();
        r.setLoteId(l.getId());
        r.setCultivoId(l.getCultivoId());
        r.setCalidad(l.getCalidad() != null ? l.getCalidad().name() : null);
        r.setPrecioUnitario(l.getPrecioUnitario());
        r.setUnidadMedida(l.getUnidadMedida());
        r.setStockDisponible(l.getStockDisponible());
        r.setFechaEntregaEstimada(l.getFechaEntregaEstimada());
        r.setFechaCosecha(l.getFechaCosecha());
        r.setCondicionesEntrega(l.getCondicionesEntrega());
        r.setEstado(l.getEstado() != null ? l.getEstado().name() : null);
        r.setImagenUrl(l.getImagenUrl());

        cultivoRepository.findById(l.getCultivoId()).ifPresent(c -> {
            r.setNombreProducto(c.getNombreProducto());
            r.setVariedad(c.getVariedad());
            r.setCategoria(c.getCategoria() != null ? c.getCategoria().name() : null);
            r.setUbicacion(c.getUbicacion());
            r.setAgricultorId(c.getAgricultorId());
        });

        return r;
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
        lote.setImagenUrl(req.getImagenUrl());
        lote.setPublicado(true);
        lote.setFechaPublicacion(LocalDateTime.now());
        lote.setEstado(EstadoLoteEnum.ACTIVO);

        Lote guardado = loteRepository.save(lote);

        registrarMovimiento(guardado.getId(), TipoMovimientoEnum.ENTRADA,
                req.getCantidadKg(), "Stock inicial al publicar lote");

        // RF-27: Auditoría cambio de stock
        auditoriaService.registrarAuditoria("CAMBIO_STOCK", 
            "Registro de stock inicial de " + guardado.getCantidadKg() + " " + guardado.getUnidadMedida() + " para el lote ID: " + guardado.getId());

        // RF-24: Notificación nuevo lote a compradores
        cultivoRepository.findById(guardado.getCultivoId()).ifPresent(c -> {
            List<Usuario> compradores = usuarioRepository.findByRol(RolEnum.COMPRADOR);
            String msg = "Nuevo lote publicado: " + c.getNombreProducto() + " (" + guardado.getCalidad() + ") - S/. " + guardado.getPrecioUnitario() + " por " + guardado.getUnidadMedida();
            for (Usuario comp : compradores) {
                notificacionService.enviarNotificacion(comp.getId(), msg, "PUBLICACION_LOTE", guardado.getId());
            }
        });

        return guardado;
    }

    // ── RF09: Control de stock — operaciones atómicas ────────────

    /**
     * Descuenta stock al confirmar un pedido.
     * Lanza excepción si el resultado sería negativo (RNF05).
     */
    @Transactional
    public Lote confirmarPedido(Long loteId, Double cantidad) {
        Lote lote = obtenerLoteParaActualizarOFallar(loteId);
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

        // RF-27: Auditoría cambio de stock
        auditoriaService.registrarAuditoria("CAMBIO_STOCK", 
            "Descuento de stock en lote ID: " + loteId + " por confirmación de pedido. Cantidad: " + cantidad + ". Nuevo stock: " + nuevoStock);

        return actualizado;
    }

    /**
     * Devuelve stock al cancelar un pedido.
     */
    @Transactional
    public Lote cancelarPedido(Long loteId, Double cantidad) {
        Lote lote = obtenerLoteParaActualizarOFallar(loteId);
        lote.setStockDisponible(lote.getStockDisponible() + cantidad);
        // Si estaba AGOTADO y se devuelve stock, volver a ACTIVO
        if (lote.getEstado() == EstadoLoteEnum.AGOTADO && lote.getStockDisponible() > 0) {
            lote.setEstado(EstadoLoteEnum.ACTIVO);
        }
        Lote actualizado = loteRepository.save(lote);
        registrarMovimiento(loteId, TipoMovimientoEnum.ENTRADA, cantidad,
                "Devolución por cancelación de pedido");

        // RF-27: Auditoría cambio de stock
        auditoriaService.registrarAuditoria("CAMBIO_STOCK", 
            "Devolución de stock en lote ID: " + loteId + " por cancelación de pedido. Cantidad: " + cantidad + ". Nuevo stock: " + actualizado.getStockDisponible());

        return actualizado;
    }

    /**
     * Ajuste manual de stock (ENTRADA, SALIDA o AJUSTE).
     * Garantiza que el stock nunca quede negativo (RNF05).
     */
    @Transactional
    public Lote ajustarStock(Long loteId, AjusteStockRequest req) {
        Lote lote = obtenerLoteParaActualizarOFallar(loteId);

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

        // RF-27: Auditoría cambio de stock
        auditoriaService.registrarAuditoria("CAMBIO_STOCK", 
            "Ajuste manual de stock (" + req.getTipo() + ") en lote ID: " + loteId + ". Cantidad: " + req.getCantidad() + ". Motivo: " + req.getMotivo() + ". Nuevo stock: " + nuevoStock);

        // RF-24: Notificación al agricultor
        cultivoRepository.findById(lote.getCultivoId()).ifPresent(c -> {
            String msg = "El stock de tu lote para " + c.getNombreProducto() + " ha sido actualizado a " + nuevoStock + " " + lote.getUnidadMedida();
            notificacionService.enviarNotificacion(c.getAgricultorId(), msg, "STOCK_UPDATE", lote.getId());
        });

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

    /** Como obtenerLoteOFallar, pero con SELECT ... FOR UPDATE (ver LoteRepository). */
    private Lote obtenerLoteParaActualizarOFallar(Long loteId) {
        Lote lote = loteRepository.findByIdForUpdate(loteId).orElse(null);
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

    @Transactional
    public Lote actualizarPrecio(Long loteId, BigDecimal nuevoPrecio, String motivo) {
        if (nuevoPrecio == null || nuevoPrecio.doubleValue() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        Lote lote = obtenerLoteOFallar(loteId);
        BigDecimal precioAnterior = lote.getPrecioUnitario();
        
        lote.setPrecioUnitario(nuevoPrecio);
        Lote guardado = loteRepository.save(lote);
        
        String emailResponsable = "SYSTEM/ANONYMOUS";
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            emailResponsable = SecurityContextHolder.getContext().getAuthentication().getName();
        }
        
        // Registrar historial de precios (RF-20)
        HistorialPrecio hp = new HistorialPrecio();
        hp.setLoteId(loteId);
        hp.setPrecioAnterior(precioAnterior);
        hp.setPrecioNuevo(nuevoPrecio);
        hp.setFechaCambio(LocalDateTime.now());
        hp.setUsuarioResponsableEmail(emailResponsable);
        hp.setMotivo(motivo);
        historialPrecioRepository.save(hp);
        
        // Registrar auditoría de cambio de precio (RF-27)
        auditoriaService.registrarAuditoria("CAMBIO_PRECIO", 
            "Cambio de precio en lote ID: " + loteId + " de S/. " + precioAnterior + " a S/. " + nuevoPrecio + ". Motivo: " + motivo);
            
        // Notificar al agricultor (RF-24)
        cultivoRepository.findById(lote.getCultivoId()).ifPresent(c -> {
            String msg = "El precio de tu lote para " + c.getNombreProducto() + " ha sido actualizado a S/. " + nuevoPrecio;
            notificacionService.enviarNotificacion(c.getAgricultorId(), msg, "STOCK_UPDATE", lote.getId());
        });
        
        return guardado;
    }

    public List<HistorialPrecio> obtenerHistorialPrecios(Long loteId) {
        return historialPrecioRepository.findTop5ByLoteIdOrderByFechaCambioDesc(loteId);
    }
}
