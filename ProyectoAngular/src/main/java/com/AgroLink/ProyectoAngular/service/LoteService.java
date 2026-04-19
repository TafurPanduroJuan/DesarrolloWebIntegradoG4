package com.AgroLink.ProyectoAngular.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.AgroLink.ProyectoAngular.model.Lote;
import com.AgroLink.ProyectoAngular.repository.LoteRepository;

@Service
public class LoteService {
    private final LoteRepository loteRepository;

    public LoteService(LoteRepository loteRepository) {
        this.loteRepository = loteRepository;
    }

    public List<Lote> listarTodos() { return loteRepository.findAll(); }
    public List<Lote> listarPublicados() { return loteRepository.findByPublicadoTrue(); }
    public List<Lote> listarPorCultivo(Long cultivoId) { return loteRepository.findByCultivoId(cultivoId); }
    public Lote guardar(Lote lote) { return loteRepository.save(lote); }
    public Lote buscarPorId(Long id) { return loteRepository.findById(id).orElse(null); }
    public void eliminar(Long id) { loteRepository.deleteById(id); }
}
