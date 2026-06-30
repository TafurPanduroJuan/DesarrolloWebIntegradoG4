package com.AgroLink.ProyectoAngular.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.AgroLink.ProyectoAngular.dto.ContactoPedidoResponse;
import com.AgroLink.ProyectoAngular.model.DetallePedido;
import com.AgroLink.ProyectoAngular.model.Pedido;
import com.AgroLink.ProyectoAngular.model.Usuario;
import com.AgroLink.ProyectoAngular.model.DatosContacto;
import com.AgroLink.ProyectoAngular.model.enums.EstadoPedidoEnum;
import com.AgroLink.ProyectoAngular.repository.DatosContactoRepository;
import com.AgroLink.ProyectoAngular.repository.DetallePedidoRepository;
import com.AgroLink.ProyectoAngular.repository.PedidoRepository;
import com.AgroLink.ProyectoAngular.repository.UsuarioRepository;

@Service
public class DatosContactoService {
    private final DatosContactoRepository datosContactoRepository;
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final UsuarioRepository usuarioRepository;

    public DatosContactoService(DatosContactoRepository datosContactoRepository,
                                 PedidoRepository pedidoRepository,
                                 DetallePedidoRepository detallePedidoRepository,
                                 UsuarioRepository usuarioRepository) {
        this.datosContactoRepository = datosContactoRepository;
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<DatosContacto> listarTodos() { return datosContactoRepository.findAll(); }
    public DatosContacto guardar(DatosContacto contacto) { return datosContactoRepository.save(contacto); }
    public DatosContacto buscarPorId(Long id) { return datosContactoRepository.findById(id).orElse(null); }
    public void eliminar(Long id) { datosContactoRepository.deleteById(id); }

    /**
     * RF11 — Gestión de datos de contacto: visibles solo cuando existe
     * pedido confirmado o coordinación autorizada entre comprador y agricultor.
     *
     * Reglas:
     *  - El pedido debe existir.
     *  - El solicitante debe ser el comprador del pedido o el agricultor de
     *    alguno de sus detalles (una de las dos partes involucradas).
     *  - El pedido debe estar en un estado de coordinación autorizada
     *    (CONFIRMADO, PREPARADO, DESPACHADO o ENTREGADO). Si está PENDIENTE,
     *    CANCELADO o RECHAZADO, los datos de contacto permanecen ocultos.
     *
     * Si todo se cumple, se devuelven los datos de contacto de la OTRA parte
     * (al comprador se le muestra el contacto del agricultor y viceversa).
     */
    public ContactoPedidoResponse obtenerContactoAutorizado(Long pedidoId, Long solicitanteId) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElse(null);
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido no existe.");
        }

        List<DetallePedido> detalles = detallePedidoRepository.findByPedidoId(pedidoId);
        if (detalles.isEmpty()) {
            throw new IllegalStateException("El pedido no tiene información de agricultor asociada.");
        }
        Long agricultorId = detalles.get(0).getAgricultorId();
        Long compradorId = pedido.getCompradorId();

        boolean esComprador = solicitanteId.equals(compradorId);
        boolean esAgricultor = solicitanteId.equals(agricultorId);
        if (!esComprador && !esAgricultor) {
            throw new SecurityException("No tienes autorización para ver los datos de contacto de este pedido.");
        }

        boolean coordinacionAutorizada =
                pedido.getEstado() == EstadoPedidoEnum.CONFIRMADO ||
                pedido.getEstado() == EstadoPedidoEnum.PREPARADO ||
                pedido.getEstado() == EstadoPedidoEnum.DESPACHADO ||
                pedido.getEstado() == EstadoPedidoEnum.ENTREGADO;

        if (!coordinacionAutorizada) {
            throw new SecurityException(
                "Los datos de contacto se habilitan solo cuando el pedido es confirmado por el agricultor."
            );
        }

        // Se solicita el contacto de la OTRA parte
        Long idObjetivo = esComprador ? agricultorId : compradorId;
        String rolObjetivo = esComprador ? "AGRICULTOR" : "COMPRADOR";

        Usuario usuarioObjetivo = usuarioRepository.findById(idObjetivo).orElse(null);
        List<DatosContacto> contactosExtra = datosContactoRepository.findByUsuarioId(idObjetivo);
        DatosContacto extra = contactosExtra.isEmpty() ? null : contactosExtra.get(0);

        String nombre = usuarioObjetivo != null ? usuarioObjetivo.getNombre() : "Usuario";
        String email = (extra != null && extra.getEmailContacto() != null)
                ? extra.getEmailContacto()
                : (usuarioObjetivo != null ? usuarioObjetivo.getEmail() : null);
        String telefono = (extra != null && extra.getTelefonoAdicional() != null)
                ? extra.getTelefonoAdicional()
                : (usuarioObjetivo != null ? usuarioObjetivo.getTelefono() : null);
        String direccion = extra != null ? extra.getDireccion() : null;
        String referencia = extra != null ? extra.getReferencia() : null;

        return new ContactoPedidoResponse(idObjetivo, nombre, rolObjetivo, direccion, referencia, email, telefono);
    }
}