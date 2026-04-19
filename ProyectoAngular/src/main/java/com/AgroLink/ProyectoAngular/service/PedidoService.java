package com.AgroLink.ProyectoAngular.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.AgroLink.ProyectoAngular.model.Pedido;
import com.AgroLink.ProyectoAngular.repository.PedidoRepository;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    public PedidoService(PedidoRepository pedidoRepository) { this.pedidoRepository = pedidoRepository; }

    public List<Pedido> listarTodos() { return pedidoRepository.findAll(); }
    public Pedido guardar(Pedido pedido) { return pedidoRepository.save(pedido); }
    public Pedido buscarPorId(Long id) { return pedidoRepository.findById(id).orElse(null); }
    public void eliminar(Long id) { pedidoRepository.deleteById(id); }
}
