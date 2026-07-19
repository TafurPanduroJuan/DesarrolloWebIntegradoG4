package com.AgroLink.ProyectoAngular.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.AgroLink.ProyectoAngular.dto.PedidoRequest;
import com.AgroLink.ProyectoAngular.dto.PedidoResponse;
import com.AgroLink.ProyectoAngular.model.Cultivo;
import com.AgroLink.ProyectoAngular.model.DetallePedido;
import com.AgroLink.ProyectoAngular.model.HistorialEstadoPedido;
import com.AgroLink.ProyectoAngular.model.Lote;
import com.AgroLink.ProyectoAngular.model.Pedido;
import com.AgroLink.ProyectoAngular.model.enums.EstadoDetalleEnum;
import com.AgroLink.ProyectoAngular.model.enums.EstadoPedidoEnum;
import com.AgroLink.ProyectoAngular.repository.CultivoRepository;
import com.AgroLink.ProyectoAngular.repository.DetallePedidoRepository;
import com.AgroLink.ProyectoAngular.repository.HistorialEstadoPedidoRepository;
import com.AgroLink.ProyectoAngular.repository.LoteRepository;
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
    private final LoteRepository loteRepository;
    private final CultivoRepository cultivoRepository;
    private final AuditoriaService auditoriaService;
    private final NotificacionService notificacionService;

    public PedidoService(PedidoRepository pedidoRepository,
                         DetallePedidoRepository detalleRepository,
                         HistorialEstadoPedidoRepository historialRepository,
                         LoteService loteService,
                         LoteRepository loteRepository,
                         CultivoRepository cultivoRepository,
                         AuditoriaService auditoriaService,
                         NotificacionService notificacionService) {
        this.pedidoRepository    = pedidoRepository;
        this.detalleRepository   = detalleRepository;
        this.historialRepository = historialRepository;
        this.loteService         = loteService;
        this.loteRepository      = loteRepository;
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

        Lote loteOrigen = loteService.buscarPorId(req.getLoteId());
        if (loteOrigen == null) {
            throw new IllegalArgumentException("Lote no encontrado: " + req.getLoteId());
        }

        Cultivo cultivoOrigen = cultivoRepository.findById(loteOrigen.getCultivoId())
            .orElseThrow(() -> new IllegalStateException("Cultivo no encontrado para el lote"));

        double cantidadRestante = req.getCantidadSolicitada();

        // ── RF-19: Atención de pedidos parciales ──────────────────────────
        // Estructura de asignación: lote -> cantidad tomada de ese lote.
        // Se arma primero con el lote elegido por el comprador; si no alcanza,
        // se completa con otros lotes ACTIVOS del mismo producto (de cualquier
        // agricultor), priorizando mejor precio y mayor antigüedad de publicación.
        LinkedHashMap<Lote, Double> asignaciones = new LinkedHashMap<>();

        double tomadoDelOrigen = Math.min(loteOrigen.getStockDisponible(), cantidadRestante);
        if (tomadoDelOrigen > 0) {
            asignaciones.put(loteOrigen, tomadoDelOrigen);
            cantidadRestante -= tomadoDelOrigen;
        }

        if (cantidadRestante > 0) {
            List<Lote> alternativos = loteRepository.buscarLotesAlternativosParaProducto(
                cultivoOrigen.getNombreProducto(), loteOrigen.getId());

            for (Lote alterno : alternativos) {
                if (cantidadRestante <= 0) break;
                double disponible = alterno.getStockDisponible() != null ? alterno.getStockDisponible() : 0;
                if (disponible <= 0) continue;
                double aTomar = Math.min(disponible, cantidadRestante);
                asignaciones.put(alterno, aTomar);
                cantidadRestante -= aTomar;
            }
        }

        if (cantidadRestante > 0) {
            throw new IllegalStateException(
                "Stock insuficiente entre todos los productores disponibles. Faltan " +
                cantidadRestante + " " + (loteOrigen.getUnidadMedida() != null ? loteOrigen.getUnidadMedida() : "kg") +
                " para completar el pedido.");
        }

        boolean esPedidoParcial = asignaciones.size() > 1;

        Pedido pedido = new Pedido();
        pedido.setCompradorId(req.getCompradorId());
        pedido.setEstado(EstadoPedidoEnum.PENDIENTE);
        pedido.setNotasEspeciales(req.getNotasEspeciales());
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setFechaEntregaEstimada(req.getFechaEntregaDeseada());
        pedido.setEsParcial(esPedidoParcial);
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        BigDecimal totalEstimado = BigDecimal.ZERO;
        List<DetallePedido> detallesCreados = new ArrayList<>();

        for (Map.Entry<Lote, Double> entry : asignaciones.entrySet()) {
            Lote lote = entry.getKey();
            Double cantidad = entry.getValue();
            Long agricultorId = cultivoRepository.findById(lote.getCultivoId())
                .map(Cultivo::getAgricultorId)
                .orElseThrow(() -> new IllegalStateException("Cultivo no encontrado para el lote " + lote.getId()));

            BigDecimal precioUnitario = lote.getPrecioUnitario();
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
            totalEstimado = totalEstimado.add(subtotal);

            // Se RESERVA (descuenta) el stock al crear el pedido, no recién al
            // confirmarlo. loteService.confirmarPedido() toma un bloqueo
            // pesimista sobre el lote y falla si, por una compra concurrente,
            // el stock ya no alcanza — evitando así la sobreventa. Como todo
            // este método es @Transactional, si una reserva falla se
            // revierten también las reservas anteriores de este mismo pedido.
            loteService.confirmarPedido(lote.getId(), cantidad);

            DetallePedido detalle = new DetallePedido();
            detalle.setPedidoId(pedidoGuardado.getId());
            detalle.setLoteId(lote.getId());
            detalle.setAgricultorId(agricultorId);
            detalle.setCantidadSolicitada(cantidad);
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotal);
            detalle.setEstadoDetalle(EstadoDetalleEnum.PENDIENTE);
            detalleRepository.save(detalle);
            detallesCreados.add(detalle);

            // RF-24: Notificación nuevo pedido (o parte de un pedido dividido) al agricultor
            String unidad = lote.getUnidadMedida() != null ? lote.getUnidadMedida() : "kg";
            String mensajeAgricultor = esPedidoParcial
                ? "Nuevo pedido parcial #" + pedidoGuardado.getId() + " recibido para tu lote (" + cantidad + " " + unidad + "). El comprador solicitó una cantidad mayor a la disponible en un solo lote, por lo que el pedido se dividió entre varios productores."
                : "Nuevo pedido #" + pedidoGuardado.getId() + " recibido para tu lote por una cantidad de " + cantidad + " " + unidad;

            notificacionService.enviarNotificacion(
                agricultorId,
                mensajeAgricultor,
                esPedidoParcial ? "PEDIDO_PARCIAL" : "NUEVO_PEDIDO",
                pedidoGuardado.getId()
            );
        }

        pedido.setTotalEstimado(totalEstimado);
        pedidoRepository.save(pedido);

        String obsHistorial = esPedidoParcial
            ? "Pedido creado y dividido automáticamente entre " + asignaciones.size() + " lotes/agricultores por falta de stock en un solo productor."
            : "Pedido creado";
        registrarHistorial(pedidoGuardado.getId(), null, EstadoPedidoEnum.PENDIENTE, obsHistorial);

        // RF-24 / RF-19: Notificación al comprador si su pedido tuvo que dividirse
        if (esPedidoParcial) {
            notificacionService.enviarNotificacion(
                req.getCompradorId(),
                "Tu pedido #" + pedidoGuardado.getId() + " de " + cultivoOrigen.getNombreProducto() +
                " fue dividido entre " + asignaciones.size() + " productores porque un solo lote no tenía stock suficiente. " +
                "Cada agricultor deberá confirmar su parte por separado.",
                "PEDIDO_PARCIAL",
                pedidoGuardado.getId()
            );
        }

        return toResponse(pedidoGuardado);
    }

    // -- RF18: Confirmacion por el agricultor ----------------------

    @Transactional
    public PedidoResponse confirmarPedido(Long pedidoId, Long agricultorId) {
        Pedido pedido = obtenerPedidoOFallar(pedidoId);
        validarEstado(pedido, EstadoPedidoEnum.PENDIENTE, "confirmar");

        List<DetallePedido> todosLosDetalles = detalleRepository.findByPedidoId(pedidoId);
        List<DetallePedido> misDetalles = todosLosDetalles.stream()
            .filter(d -> d.getAgricultorId().equals(agricultorId) && d.getEstadoDetalle() == EstadoDetalleEnum.PENDIENTE)
            .collect(Collectors.toList());
        if (misDetalles.isEmpty()) {
            throw new IllegalArgumentException("No tienes items pendientes en este pedido");
        }

        for (DetallePedido d : misDetalles) {
            // El stock ya se reservó (descontó) al crear el pedido; confirmar
            // solo cambia el estado del detalle, no vuelve a tocar el stock.
            d.setEstadoDetalle(EstadoDetalleEnum.CONFIRMADO);
            detalleRepository.save(d);
        }

        // RF-19: en un pedido dividido, el estado general solo avanza cuando
        // todos los agricultores involucrados hayan resuelto su parte.
        boolean esParcial = Boolean.TRUE.equals(pedido.getEsParcial());
        EstadoResolucion resolucion = resolverEstadoDetalles(todosLosDetalles);

        if (resolucion.pendientes == 0) {
            EstadoPedidoEnum nuevoEstado = resolucion.confirmados > 0
                ? EstadoPedidoEnum.CONFIRMADO
                : EstadoPedidoEnum.RECHAZADO;
            pedido.setEstado(nuevoEstado);
            pedidoRepository.save(pedido);
            registrarHistorial(pedidoId, EstadoPedidoEnum.PENDIENTE, nuevoEstado,
                    "Pedido confirmado por el agricultor ID: " + agricultorId +
                    (esParcial ? " (todas las partes del pedido dividido fueron resueltas)" : ""));

            // RF-24: Notificación de confirmación final al comprador
            notificacionService.enviarNotificacion(
                pedido.getCompradorId(),
                nuevoEstado == EstadoPedidoEnum.CONFIRMADO
                    ? "Tu pedido #" + pedido.getId() + " ha sido confirmado" + (esParcial ? " en su totalidad por todos los productores." : " por el agricultor.")
                    : "Tu pedido #" + pedido.getId() + " no pudo confirmarse: ningún productor aceptó su parte.",
                "CONFIRMACION",
                pedido.getId()
            );
        } else {
            // Aún faltan otros agricultores por resolver su parte del pedido.
            registrarHistorial(pedidoId, EstadoPedidoEnum.PENDIENTE, EstadoPedidoEnum.PENDIENTE,
                    "El agricultor ID: " + agricultorId + " confirmó su parte del pedido dividido " +
                    "(" + resolucion.confirmados + " de " + todosLosDetalles.size() + " partes confirmadas).");

            notificacionService.enviarNotificacion(
                pedido.getCompradorId(),
                "Un productor confirmó su parte de tu pedido parcial #" + pedido.getId() + " (" +
                resolucion.confirmados + " de " + todosLosDetalles.size() + " partes confirmadas). " +
                "Aún esperamos la respuesta de los demás productores.",
                "PEDIDO_PARCIAL",
                pedido.getId()
            );
        }

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

        List<DetallePedido> todosLosDetalles = detalleRepository.findByPedidoId(pedidoId);
        List<DetallePedido> misDetalles = todosLosDetalles.stream()
            .filter(d -> d.getAgricultorId().equals(agricultorId) && d.getEstadoDetalle() == EstadoDetalleEnum.PENDIENTE)
            .collect(Collectors.toList());
        if (misDetalles.isEmpty()) {
            throw new IllegalArgumentException("No tienes items pendientes en este pedido");
        }

        for (DetallePedido d : misDetalles) {
            // El stock se reservó al crear el pedido; al rechazar hay que
            // devolverlo al lote (antes esto no hacía falta porque el stock
            // recién se descontaba al confirmar).
            loteService.cancelarPedido(d.getLoteId(), d.getCantidadSolicitada());
            d.setEstadoDetalle(EstadoDetalleEnum.CANCELADO);
            detalleRepository.save(d);
        }

        String obs = (motivo != null && !motivo.isBlank()) ? motivo : "Pedido rechazado por el agricultor";
        boolean esParcial = Boolean.TRUE.equals(pedido.getEsParcial());

        // RF-19: en un pedido dividido, rechazar la parte de un agricultor no
        // rechaza automáticamente las partes de los demás productores.
        EstadoResolucion resolucion = resolverEstadoDetalles(todosLosDetalles);

        if (resolucion.pendientes == 0) {
            EstadoPedidoEnum nuevoEstado = resolucion.confirmados > 0
                ? EstadoPedidoEnum.CONFIRMADO
                : EstadoPedidoEnum.RECHAZADO;
            pedido.setEstado(nuevoEstado);
            pedidoRepository.save(pedido);
            registrarHistorial(pedidoId, EstadoPedidoEnum.PENDIENTE, nuevoEstado, obs);

            notificacionService.enviarNotificacion(
                pedido.getCompradorId(),
                nuevoEstado == EstadoPedidoEnum.RECHAZADO
                    ? "Tu pedido #" + pedido.getId() + " ha sido rechazado por el agricultor. Motivo: " + obs
                    : "Tu pedido parcial #" + pedido.getId() + " quedó confirmado con las partes que sí fueron aceptadas. Un productor rechazó su parte. Motivo: " + obs,
                "RECHAZO",
                pedido.getId()
            );
        } else {
            registrarHistorial(pedidoId, EstadoPedidoEnum.PENDIENTE, EstadoPedidoEnum.PENDIENTE,
                    "El agricultor ID: " + agricultorId + " rechazó su parte del pedido dividido. Motivo: " + obs);

            notificacionService.enviarNotificacion(
                pedido.getCompradorId(),
                "Un productor rechazó su parte de tu pedido parcial #" + pedido.getId() + ". Motivo: " + obs +
                ". Aún esperamos la respuesta de los demás productores.",
                "PEDIDO_PARCIAL",
                pedido.getId()
            );
        }

        return toResponse(pedido);
    }

    /** Pequeño resumen de cuántos detalles de un pedido están confirmados / cancelados / pendientes. */
    private static class EstadoResolucion {
        int confirmados;
        int cancelados;
        int pendientes;
    }

    private EstadoResolucion resolverEstadoDetalles(List<DetallePedido> detalles) {
        EstadoResolucion r = new EstadoResolucion();
        for (DetallePedido d : detalles) {
            switch (d.getEstadoDetalle()) {
                case CONFIRMADO -> r.confirmados++;
                case CANCELADO -> r.cancelados++;
                default -> r.pendientes++;
            }
        }
        return r;
    }

    // -- RF08: Cambio de estado con historial ----------------------

    private static final Map<EstadoPedidoEnum, Set<EstadoPedidoEnum>> TRANSICIONES = Map.of(
        EstadoPedidoEnum.PENDIENTE,  Set.of(EstadoPedidoEnum.CANCELADO),
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
                // Tanto los detalles ya CONFIRMADOS como los que seguían
                // PENDIENTES tienen stock reservado (se descuenta al crear el
                // pedido, ver crearPedido()), así que ambos deben devolverlo.
                if (d.getEstadoDetalle() == EstadoDetalleEnum.CONFIRMADO
                        || d.getEstadoDetalle() == EstadoDetalleEnum.PENDIENTE) {
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
        r.setEsParcial(Boolean.TRUE.equals(p.getEsParcial()));

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