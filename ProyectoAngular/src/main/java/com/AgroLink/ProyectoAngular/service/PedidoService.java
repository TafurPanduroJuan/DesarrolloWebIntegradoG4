package com.AgroLink.ProyectoAngular.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.AgroLink.ProyectoAngular.dto.PedidoRequest;
import com.AgroLink.ProyectoAngular.dto.PedidoResponse;
import com.AgroLink.ProyectoAngular.model.DetallePedido;
import com.AgroLink.ProyectoAngular.model.HistorialEstadoPedido;
import com.AgroLink.ProyectoAngular.model.Lote;
import com.AgroLink.ProyectoAngular.model.Pedido;
import com.AgroLink.ProyectoAngular.model.enums.EstadoDetalleEnum;
import com.AgroLink.ProyectoAngular.model.enums.EstadoPedidoEnum;
import com.AgroLink.ProyectoAngular.repository.CultivoRepository;
import com.AgroLink.ProyectoAngular.repository.DetallePedidoRepository;
import com.AgroLink.ProyectoAngular.repository.HistorialEstadoPedidoRepository;
import com.AgroLink.ProyectoAngular.repository.PedidoRepository;
import com.AgroLink.ProyectoAngular.service.AuditoriaService;
import com.AgroLink.ProyectoAngular.service.NotificacionService;

/**
 * RF07 - Registro de pedidos con validacion de stock.
 * RF08 - Seguimiento con historial de estados.
 * RF12 - Observaciones / notas especiales del pedido.
 * RF18 - Confirmacion o rechazo por el agricultor.
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detalleRepository;
    private final HistorialEstadoPedidoRepository historialRepository;
    private final LoteService loteService;
    private final CultivoRepository cultivoRepository;
    private final AuditoriaService auditoriaService;
    private final NotificacionService notificacionService;

    public PedidoService(PedidoRepository pedidoRepository,
                         DetallePedidoRepository detalleRepository,
                         HistorialEstadoPedidoRepository historialRepository,
                         LoteService loteService,
                         CultivoRepository cultivoRepository,
                         AuditoriaService auditoriaService,
                         NotificacionService notificacionService) {
        this.pedidoRepository    = pedidoRepository;
        this.detalleRepository   = detalleRepository;
        this.historialRepository = historialRepository;
        this.loteService         = loteService;
        this.cultivoRepository   = cultivoRepository;
        this.auditoriaService    = auditoriaService;
        this.notificacionService = notificacionService;
    }

    // -- RF07: Crear pedido con validacion de stock ----------------

    @Transactional
    public PedidoResponse crearPedido(PedidoRequest req) {
        if (req.getCantidadSolicitada() == null || req.getCantidadSolicitada() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (req.getLoteId() == null) {
            throw new IllegalArgumentException("Debe especificar un lote");
        }

        Lote lote = loteService.buscarPorId(req.getLoteId());
        if (lote == null) {
            throw new IllegalArgumentException("Lote no encontrado: " + req.getLoteId());
        }
        if (lote.getStockDisponible() < req.getCantidadSolicitada()) {
            throw new IllegalStateException(
                "Stock insuficiente. Disponible: " + lote.getStockDisponible() +
                ", solicitado: " + req.getCantidadSolicitada());
        }

        Long agricultorId = cultivoRepository.findById(lote.getCultivoId())
            .map(c -> c.getAgricultorId())
            .orElseThrow(() -> new IllegalStateException("Cultivo no encontrado para el lote"));

        BigDecimal precioUnitario = lote.getPrecioUnitario();
        BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(req.getCantidadSolicitada()));

        Pedido pedido = new Pedido();
        pedido.setCompradorId(req.getCompradorId());
        pedido.setEstado(EstadoPedidoEnum.PENDIENTE);
        pedido.setNotasEspeciales(req.getNotasEspeciales());
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setFechaEntregaEstimada(req.getFechaEntregaDeseada());
        pedido.setTotalEstimado(subtotal);
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        DetallePedido detalle = new DetallePedido();
        detalle.setPedidoId(pedidoGuardado.getId());
        detalle.setLoteId(lote.getId());
        detalle.setAgricultorId(agricultorId);
        detalle.setCantidadSolicitada(req.getCantidadSolicitada());
        detalle.setPrecioUnitario(precioUnitario);
        detalle.setSubtotal(subtotal);
        detalle.setEstadoDetalle(EstadoDetalleEnum.PENDIENTE);
        detalleRepository.save(detalle);

        registrarHistorial(pedidoGuardado.getId(), null, EstadoPedidoEnum.PENDIENTE, "Pedido creado");

        // RF-24: Notificación nuevo pedido al agricultor
        notificacionService.enviarNotificacion(
            agricultorId,
            "Nuevo pedido #" + pedidoGuardado.getId() + " recibido para tu lote por una cantidad de " + req.getCantidadSolicitada() + " " + (lote.getUnidadMedida() != null ? lote.getUnidadMedida() : "kg"),
            "NUEVO_PEDIDO",
            pedidoGuardado.getId()
        );

        return toResponse(pedidoGuardado);
    }

    // -- RF18: Confirmacion por el agricultor ----------------------

    @Transactional
    public PedidoResponse confirmarPedido(Long pedidoId, Long agricultorId) {
        Pedido pedido = obtenerPedidoOFallar(pedidoId);
        validarEstado(pedido, EstadoPedidoEnum.PENDIENTE, "confirmar");

        List<DetallePedido> detalles = detalleRepository.findByPedidoId(pedidoId).stream()
            .filter(d -> d.getAgricultorId().equals(agricultorId))
            .collect(Collectors.toList());
        if (detalles.isEmpty()) {
            throw new IllegalArgumentException("No tienes items en este pedido");
        }

        for (DetallePedido d : detalles) {
            loteService.confirmarPedido(d.getLoteId(), d.getCantidadSolicitada());
            d.setEstadoDetalle(EstadoDetalleEnum.CONFIRMADO);
            detalleRepository.save(d);
        }

        pedido.setEstado(EstadoPedidoEnum.CONFIRMADO);
        pedidoRepository.save(pedido);
        registrarHistorial(pedidoId, EstadoPedidoEnum.PENDIENTE, EstadoPedidoEnum.CONFIRMADO,
                "Pedido confirmado por el agricultor");

        // RF-24: Notificación de confirmación al comprador
        notificacionService.enviarNotificacion(
            pedido.getCompradorId(),
            "Tu pedido #" + pedido.getId() + " ha sido confirmado por el agricultor.",
            "CONFIRMACION",
            pedido.getId()
        );

        // RF-27: Auditoría de confirmación de pedido
        auditoriaService.registrarAuditoria("CONFIRMACION_PEDIDO",
            "Pedido ID: " + pedidoId + " confirmado por el agricultor ID: " + agricultorId);

        return toResponse(pedido);
    }

    // -- RF18: Rechazo por el agricultor ---------------------------

    @Transactional
    public PedidoResponse rechazarPedido(Long pedidoId, Long agricultorId, String motivo) {
        Pedido pedido = obtenerPedidoOFallar(pedidoId);
        validarEstado(pedido, EstadoPedidoEnum.PENDIENTE, "rechazar");

        List<DetallePedido> detalles = detalleRepository.findByPedidoId(pedidoId).stream()
            .filter(d -> d.getAgricultorId().equals(agricultorId))
            .collect(Collectors.toList());
        if (detalles.isEmpty()) {
            throw new IllegalArgumentException("No tienes items en este pedido");
        }

        for (DetallePedido d : detalles) {
            d.setEstadoDetalle(EstadoDetalleEnum.CANCELADO);
            detalleRepository.save(d);
        }

        pedido.setEstado(EstadoPedidoEnum.RECHAZADO);
        pedidoRepository.save(pedido);
        String obs = (motivo != null && !motivo.isBlank()) ? motivo : "Pedido rechazado por el agricultor";
        registrarHistorial(pedidoId, EstadoPedidoEnum.PENDIENTE, EstadoPedidoEnum.RECHAZADO, obs);

        // RF-24: Notificación de rechazo al comprador
        notificacionService.enviarNotificacion(
            pedido.getCompradorId(),
            "Tu pedido #" + pedido.getId() + " ha sido rechazado por el agricultor. Motivo: " + obs,
            "RECHAZO",
            pedido.getId()
        );

        return toResponse(pedido);
    }

    // -- RF08: Cambio de estado con historial ----------------------

    private static final Map<EstadoPedidoEnum, Set<EstadoPedidoEnum>> TRANSICIONES = Map.of(
        EstadoPedidoEnum.CONFIRMADO, Set.of(EstadoPedidoEnum.PREPARADO, EstadoPedidoEnum.CANCELADO),
        EstadoPedidoEnum.PREPARADO,  Set.of(EstadoPedidoEnum.DESPACHADO, EstadoPedidoEnum.CANCELADO),
        EstadoPedidoEnum.DESPACHADO, Set.of(EstadoPedidoEnum.ENTREGADO, EstadoPedidoEnum.CANCELADO)
    );

    @Transactional
    public PedidoResponse cambiarEstado(Long pedidoId, EstadoPedidoEnum nuevoEstado, String observacion) {
        Pedido pedido = obtenerPedidoOFallar(pedidoId);
        EstadoPedidoEnum estadoActual = pedido.getEstado();

        Set<EstadoPedidoEnum> permitidos = TRANSICIONES.get(estadoActual);
        if (permitidos == null || !permitidos.contains(nuevoEstado)) {
            throw new IllegalStateException(
                "No se puede cambiar de " + estadoActual + " a " + nuevoEstado);
        }

        if (nuevoEstado == EstadoPedidoEnum.CANCELADO) {
            List<DetallePedido> detalles = detalleRepository.findByPedidoId(pedidoId);
            for (DetallePedido d : detalles) {
                if (d.getEstadoDetalle() == EstadoDetalleEnum.CONFIRMADO) {
                    loteService.cancelarPedido(d.getLoteId(), d.getCantidadSolicitada());
                    d.setEstadoDetalle(EstadoDetalleEnum.CANCELADO);
                    detalleRepository.save(d);
                }
            }
        }

        pedido.setEstado(nuevoEstado);
        pedidoRepository.save(pedido);
        String obs = (observacion != null && !observacion.isBlank()) ? observacion
                : estadoActual + " -> " + nuevoEstado;
        registrarHistorial(pedidoId, estadoActual, nuevoEstado, obs);

        // RF-24: Notificación de cambio de estado al comprador
        notificacionService.enviarNotificacion(
            pedido.getCompradorId(),
            "El estado de tu pedido #" + pedido.getId() + " ha cambiado de " + estadoActual + " a " + nuevoEstado,
            "CAMBIO_ESTADO",
            pedido.getId()
        );

        return toResponse(pedido);
    }

    // -- Consultas ------------------------------------------------

    public PedidoResponse obtenerConHistorial(Long pedidoId) {
        Pedido pedido = obtenerPedidoOFallar(pedidoId);
        return toResponse(pedido);
    }

    public List<PedidoResponse> listarPorComprador(Long compradorId) {
        return pedidoRepository.findByCompradorId(compradorId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<PedidoResponse> listarPorAgricultor(Long agricultorId) {
        return pedidoRepository.findByAgricultorId(agricultorId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<HistorialEstadoPedido> obtenerHistorial(Long pedidoId) {
        return historialRepository.findByPedidoIdOrderByFechaCambioAsc(pedidoId);
    }

    // Mantener compatibilidad con CRUD basico existente
    public List<Pedido> listarTodos() { return pedidoRepository.findAll(); }
    public Pedido buscarPorId(Long id) { return pedidoRepository.findById(id).orElse(null); }
    public Pedido guardar(Pedido pedido) { return pedidoRepository.save(pedido); }
    public void eliminar(Long id) { pedidoRepository.deleteById(id); }

    // -- Helpers privados -----------------------------------------

    private Pedido obtenerPedidoOFallar(Long id) {
        return pedidoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + id));
    }

    private void validarEstado(Pedido pedido, EstadoPedidoEnum esperado, String accion) {
        if (pedido.getEstado() != esperado) {
            throw new IllegalStateException(
                "No se puede " + accion + " un pedido en estado " + pedido.getEstado());
        }
    }

    private void registrarHistorial(Long pedidoId, EstadoPedidoEnum anterior,
                                    EstadoPedidoEnum nuevo, String observacion) {
        HistorialEstadoPedido h = new HistorialEstadoPedido();
        h.setPedidoId(pedidoId);
        h.setEstadoAnterior(anterior);
        h.setEstadoNuevo(nuevo);
        h.setObservacion(observacion);
        h.setFechaCambio(LocalDateTime.now());
        historialRepository.save(h);
    }

    private PedidoResponse toResponse(Pedido p) {
        PedidoResponse r = new PedidoResponse();
        r.setId(p.getId());
        r.setCompradorId(p.getCompradorId());
        r.setEstado(p.getEstado() != null ? p.getEstado().name() : null);
        r.setNotasEspeciales(p.getNotasEspeciales());
        r.setFechaPedido(p.getFechaPedido());
        r.setFechaEntregaEstimada(p.getFechaEntregaEstimada());
        r.setTotalEstimado(p.getTotalEstimado());

        List<DetallePedido> detalles = detalleRepository.findByPedidoId(p.getId());
        r.setDetalles(detalles.stream().map(d -> {
            PedidoResponse.DetalleResponse dr = new PedidoResponse.DetalleResponse();
            dr.setId(d.getId());
            dr.setLoteId(d.getLoteId());
            dr.setAgricultorId(d.getAgricultorId());
            dr.setCantidadSolicitada(d.getCantidadSolicitada());
            dr.setPrecioUnitario(d.getPrecioUnitario());
            dr.setSubtotal(d.getSubtotal());
            dr.setEstadoDetalle(d.getEstadoDetalle() != null ? d.getEstadoDetalle().name() : null);
            Lote lote = loteService.buscarPorId(d.getLoteId());
            if (lote != null) {
                dr.setUnidadMedida(lote.getUnidadMedida());
                cultivoRepository.findById(lote.getCultivoId()).ifPresent(c -> {
                    dr.setNombreProducto(c.getNombreProducto());
                });
            }
            return dr;
        }).collect(Collectors.toList()));

        List<HistorialEstadoPedido> historial = historialRepository.findByPedidoIdOrderByFechaCambioAsc(p.getId());
        r.setHistorial(historial.stream().map(h -> {
            PedidoResponse.HistorialResponse hr = new PedidoResponse.HistorialResponse();
            hr.setId(h.getId());
            hr.setEstadoAnterior(h.getEstadoAnterior() != null ? h.getEstadoAnterior().name() : null);
            hr.setEstadoNuevo(h.getEstadoNuevo() != null ? h.getEstadoNuevo().name() : null);
            hr.setObservacion(h.getObservacion());
            hr.setFechaCambio(h.getFechaCambio());
            return hr;
        }).collect(Collectors.toList()));

        return r;
    }
}
