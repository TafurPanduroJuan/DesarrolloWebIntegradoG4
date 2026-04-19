package com.AgroLink.ProyectoAngular.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.AgroLink.ProyectoAngular.model.HistorialEstadoPedido;
import com.AgroLink.ProyectoAngular.repository.HistorialEstadoPedidoRepository;

@Service
public class HistorialEstadoPedidoService {
    private final HistorialEstadoPedidoRepository historialEstadoPedidoRepository;
    public HistorialEstadoPedidoService(HistorialEstadoPedidoRepository historialEstadoPedidoRepository) { this.historialEstadoPedidoRepository = historialEstadoPedidoRepository; }

    public List<HistorialEstadoPedido> listarTodos() { return historialEstadoPedidoRepository.findAll(); }
    public HistorialEstadoPedido guardar(HistorialEstadoPedido historial) { return historialEstadoPedidoRepository.save(historial); }
    public HistorialEstadoPedido buscarPorId(Long id) { return historialEstadoPedidoRepository.findById(id).orElse(null); }
    public void eliminar(Long id) { historialEstadoPedidoRepository.deleteById(id); }
}