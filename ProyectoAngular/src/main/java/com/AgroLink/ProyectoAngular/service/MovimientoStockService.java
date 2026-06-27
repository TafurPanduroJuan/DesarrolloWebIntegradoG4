package com.AgroLink.ProyectoAngular.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.AgroLink.ProyectoAngular.model.MovimientoStock;
import com.AgroLink.ProyectoAngular.repository.MovimientoStockRepository;

@Service
public class MovimientoStockService {

    private final MovimientoStockRepository movimientoStockRepository;

    public MovimientoStockService(MovimientoStockRepository repo) {
        this.movimientoStockRepository = repo;
    }

    public List<MovimientoStock> listarTodos() {
        return movimientoStockRepository.findAll();
    }

    /** RF09 – Historial de movimientos de un lote específico */
    public List<MovimientoStock> listarPorLote(Long loteId) {
        return movimientoStockRepository.findByLoteId(loteId);
    }

    public MovimientoStock guardar(MovimientoStock movimiento) {
        return movimientoStockRepository.save(movimiento);
    }

    public MovimientoStock buscarPorId(Long id) {
        return movimientoStockRepository.findById(id).orElse(null);
    }

    public void eliminar(Long id) {
        movimientoStockRepository.deleteById(id);
    }
}
