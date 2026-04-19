package com.AgroLink.ProyectoAngular.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.AgroLink.ProyectoAngular.model.DetallePedido;
import com.AgroLink.ProyectoAngular.repository.DetallePedidoRepository;

@Service
public class DetallePedidoService {
    private final DetallePedidoRepository detallePedidoRepository;
    public DetallePedidoService(DetallePedidoRepository detallePedidoRepository) { this.detallePedidoRepository = detallePedidoRepository; }

    public List<DetallePedido> listarTodos() { return detallePedidoRepository.findAll(); }
    public DetallePedido guardar(DetallePedido detalle) { return detallePedidoRepository.save(detalle); }
    public DetallePedido buscarPorId(Long id) { return detallePedidoRepository.findById(id).orElse(null); }
    public void eliminar(Long id) { detallePedidoRepository.deleteById(id); }
}