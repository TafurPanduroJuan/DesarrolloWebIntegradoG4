package com.AgroLink.ProyectoAngular.service;

import com.AgroLink.ProyectoAngular.dto.reporte.ReporteVentasResponse;
import com.AgroLink.ProyectoAngular.dto.reporte.VentaPorAgricultorDTO;
import com.AgroLink.ProyectoAngular.dto.reporte.VentaPorProductoDTO;
import com.AgroLink.ProyectoAngular.model.Cultivo;
import com.AgroLink.ProyectoAngular.model.DetallePedido;
import com.AgroLink.ProyectoAngular.model.HistorialPrecio;
import com.AgroLink.ProyectoAngular.model.Lote;
import com.AgroLink.ProyectoAngular.model.Pedido;
import com.AgroLink.ProyectoAngular.model.Usuario;
import com.AgroLink.ProyectoAngular.model.enums.EstadoDetalleEnum;
import com.AgroLink.ProyectoAngular.model.enums.RolEnum;
import com.AgroLink.ProyectoAngular.repository.CultivoRepository;
import com.AgroLink.ProyectoAngular.repository.DetallePedidoRepository;
import com.AgroLink.ProyectoAngular.repository.HistorialPrecioRepository;
import com.AgroLink.ProyectoAngular.repository.LoteRepository;
import com.AgroLink.ProyectoAngular.repository.PedidoRepository;
import com.AgroLink.ProyectoAngular.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RF-10 — Historial y reportes comerciales.
 * Extiende el dominio de RF-20 (historial de precios) y RF-27 (auditoría),
 * cruzando pedidos/detalles de pedido para construir reportes de ventas
 * filtrables por rango de fechas y con alcance según el rol del usuario:
 *  - ADMINISTRADOR: reporte global de la plataforma (o de un agricultor puntual).
 *  - AGRICULTOR: solo sus propias ventas.
 *  - COMPRADOR: solo sus propias compras.
 */
@Service
public class ReporteService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final LoteRepository loteRepository;
    private final CultivoRepository cultivoRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialPrecioRepository historialPrecioRepository;

    public ReporteService(PedidoRepository pedidoRepository,
                           DetallePedidoRepository detallePedidoRepository,
                           LoteRepository loteRepository,
                           CultivoRepository cultivoRepository,
                           UsuarioRepository usuarioRepository,
                           HistorialPrecioRepository historialPrecioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.loteRepository = loteRepository;
        this.cultivoRepository = cultivoRepository;
        this.usuarioRepository = usuarioRepository;
        this.historialPrecioRepository = historialPrecioRepository;
    }

    public ReporteVentasResponse generarReporteVentas(Usuario usuarioActual, LocalDate desde, LocalDate hasta,
                                                       Long agricultorIdFiltro) {
        LocalDateTime desdeDT = desde != null ? desde.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime hastaDT = hasta != null ? hasta.atTime(LocalTime.MAX) : LocalDateTime.now();

        RolEnum rol = usuarioActual.getRol();
        boolean esGlobal = false;
        List<DetallePedido> detalles;
        String alcance;

        if (rol == RolEnum.ADMINISTRADOR) {
            if (agricultorIdFiltro != null) {
                detalles = detallePedidoRepository.findByAgricultorId(agricultorIdFiltro);
                alcance = "AGRICULTOR";
            } else {
                detalles = detallePedidoRepository.findAll();
                esGlobal = true;
                alcance = "GLOBAL";
            }
        } else if (rol == RolEnum.AGRICULTOR) {
            detalles = detallePedidoRepository.findByAgricultorId(usuarioActual.getId());
            alcance = "AGRICULTOR";
        } else {
            List<Pedido> pedidosComprador = pedidoRepository.findByCompradorId(usuarioActual.getId());
            List<Long> pedidoIds = pedidosComprador.stream().map(Pedido::getId).collect(Collectors.toList());
            detalles = pedidoIds.isEmpty() ? List.of() : detallePedidoRepository.findByPedidoIdIn(pedidoIds);
            alcance = "COMPRADOR";
        }

        // Mapa de pedidos involucrados, para poder filtrar por fecha y contar estados.
        Set<Long> pedidoIdsInvolucrados = detalles.stream().map(DetallePedido::getPedidoId).collect(Collectors.toSet());
        Map<Long, Pedido> pedidoMap = pedidoIdsInvolucrados.isEmpty()
                ? Map.of()
                : pedidoRepository.findAllById(pedidoIdsInvolucrados).stream()
                    .collect(Collectors.toMap(Pedido::getId, p -> p));

        List<DetallePedido> detallesFiltrados = detalles.stream()
                .filter(d -> {
                    Pedido p = pedidoMap.get(d.getPedidoId());
                    return p != null && p.getFechaPedido() != null
                            && !p.getFechaPedido().isBefore(desdeDT) && !p.getFechaPedido().isAfter(hastaDT);
                })
                .collect(Collectors.toList());

        // Solo las líneas CONFIRMADO cuentan como venta real para los montos.
        List<DetallePedido> detallesConfirmados = detallesFiltrados.stream()
                .filter(d -> d.getEstadoDetalle() == EstadoDetalleEnum.CONFIRMADO)
                .collect(Collectors.toList());

        BigDecimal totalIngresos = detallesConfirmados.stream()
                .map(DetallePedido::getSubtotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Set<Long> pedidosUnicos = detallesFiltrados.stream().map(DetallePedido::getPedidoId).collect(Collectors.toSet());
        Map<String, Long> pedidosPorEstado = pedidosUnicos.stream()
                .map(pedidoMap::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.groupingBy(p -> p.getEstado().name(), Collectors.counting()));

        // Lookup de lote -> cultivo para nombre/categoria de producto.
        Set<Long> loteIds = detallesConfirmados.stream().map(DetallePedido::getLoteId).collect(Collectors.toSet());
        Map<Long, Lote> loteMap = loteIds.isEmpty() ? Map.of()
                : loteRepository.findAllById(loteIds).stream().collect(Collectors.toMap(Lote::getId, l -> l));
        Set<Long> cultivoIds = loteMap.values().stream().map(Lote::getCultivoId).collect(Collectors.toSet());
        Map<Long, Cultivo> cultivoMap = cultivoIds.isEmpty() ? Map.of()
                : cultivoRepository.findAllById(cultivoIds).stream().collect(Collectors.toMap(Cultivo::getId, c -> c));

        Map<String, VentaPorProductoDTO> acumProducto = new LinkedHashMap<>();
        for (DetallePedido d : detallesConfirmados) {
            Lote lote = loteMap.get(d.getLoteId());
            Cultivo cultivo = lote != null ? cultivoMap.get(lote.getCultivoId()) : null;
            String nombreProducto = cultivo != null ? cultivo.getNombreProducto() : "Producto no disponible";
            String categoria = (cultivo != null && cultivo.getCategoria() != null) ? cultivo.getCategoria().name() : "OTROS";

            VentaPorProductoDTO acc = acumProducto.computeIfAbsent(nombreProducto,
                    k -> new VentaPorProductoDTO(nombreProducto, categoria, 0.0, BigDecimal.ZERO, 0L));
            double cantidad = d.getCantidadSolicitada() != null ? d.getCantidadSolicitada() : 0.0;
            BigDecimal subtotal = d.getSubtotal() != null ? d.getSubtotal() : BigDecimal.ZERO;
            acc.setCantidadVendidaKg(acc.getCantidadVendidaKg() + cantidad);
            acc.setMontoTotal(acc.getMontoTotal().add(subtotal));
            acc.setNumeroPedidos(acc.getNumeroPedidos() + 1);
        }
        List<VentaPorProductoDTO> ventasPorProducto = acumProducto.values().stream()
                .sorted(Comparator.comparing(VentaPorProductoDTO::getMontoTotal).reversed())
                .collect(Collectors.toList());

        // Desglose por agricultor: solo tiene sentido en la vista global del administrador.
        List<VentaPorAgricultorDTO> ventasPorAgricultor = new ArrayList<>();
        if (esGlobal) {
            Map<Long, BigDecimal> montoPorAgricultor = new LinkedHashMap<>();
            Map<Long, Set<Long>> pedidosPorAgricultor = new LinkedHashMap<>();
            for (DetallePedido d : detallesConfirmados) {
                montoPorAgricultor.merge(d.getAgricultorId(),
                        d.getSubtotal() != null ? d.getSubtotal() : BigDecimal.ZERO, BigDecimal::add);
                pedidosPorAgricultor.computeIfAbsent(d.getAgricultorId(), k -> new java.util.HashSet<>())
                        .add(d.getPedidoId());
            }
            Map<Long, Usuario> agricultoresMap = montoPorAgricultor.isEmpty() ? Map.of()
                    : usuarioRepository.findAllById(montoPorAgricultor.keySet()).stream()
                        .collect(Collectors.toMap(Usuario::getId, u -> u));

            for (Map.Entry<Long, BigDecimal> e : montoPorAgricultor.entrySet()) {
                Usuario u = agricultoresMap.get(e.getKey());
                String nombre = u != null ? (u.getNombre() + " " + u.getApellido()) : "Agricultor no disponible";
                long numeroPedidos = pedidosPorAgricultor.getOrDefault(e.getKey(), Set.of()).size();
                ventasPorAgricultor.add(new VentaPorAgricultorDTO(e.getKey(), nombre, numeroPedidos, e.getValue()));
            }
            ventasPorAgricultor.sort(Comparator.comparing(VentaPorAgricultorDTO::getMontoTotal).reversed());
        }

        // Cambios de precio (RF-20) ocurridos en el periodo, acotados al alcance del usuario.
        List<HistorialPrecio> cambiosPrecio;
        if (rol == RolEnum.COMPRADOR) {
            cambiosPrecio = List.of();
        } else if (rol == RolEnum.ADMINISTRADOR && agricultorIdFiltro == null) {
            cambiosPrecio = historialPrecioRepository.findByFechaCambioBetweenOrderByFechaCambioDesc(desdeDT, hastaDT);
        } else {
            Long agricultorId = rol == RolEnum.AGRICULTOR ? usuarioActual.getId() : agricultorIdFiltro;
            List<Long> cultivoIdsAgricultor = cultivoRepository.findByAgricultorId(agricultorId).stream()
                    .map(Cultivo::getId).collect(Collectors.toList());
            List<Long> loteIdsAgricultor = cultivoIdsAgricultor.isEmpty() ? List.of()
                    : loteRepository.findByCultivoIdIn(cultivoIdsAgricultor).stream()
                        .map(Lote::getId).collect(Collectors.toList());
            cambiosPrecio = loteIdsAgricultor.isEmpty() ? List.of()
                    : historialPrecioRepository.findByLoteIdInAndFechaCambioBetweenOrderByFechaCambioDesc(
                        loteIdsAgricultor, desdeDT, hastaDT);
        }

        ReporteVentasResponse response = new ReporteVentasResponse();
        response.setDesde(desde);
        response.setHasta(hasta);
        response.setAlcance(alcance);
        response.setTotalPedidos(pedidosUnicos.size());
        response.setTotalIngresos(totalIngresos);
        response.setPedidosPorEstado(pedidosPorEstado);
        response.setVentasPorProducto(ventasPorProducto);
        response.setVentasPorAgricultor(ventasPorAgricultor);
        response.setCambiosPrecio(cambiosPrecio);
        return response;
    }
}
