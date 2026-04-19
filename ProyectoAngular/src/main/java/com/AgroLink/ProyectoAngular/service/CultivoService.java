package com.AgroLink.ProyectoAngular.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.AgroLink.ProyectoAngular.model.Cultivo;
import com.AgroLink.ProyectoAngular.repository.CultivoRepository;

@Service
public class CultivoService {
    private final CultivoRepository cultivoRepository;

    public CultivoService(CultivoRepository cultivoRepository) {
        this.cultivoRepository = cultivoRepository;
    }

    public List<Cultivo> listarTodos() { return cultivoRepository.findAll(); }
    public List<Cultivo> listarPorAgricultor(Long agricultorId) { return cultivoRepository.findByAgricultorId(agricultorId); }
    public Cultivo guardar(Cultivo cultivo) { return cultivoRepository.save(cultivo); }
    public Cultivo buscarPorId(Long id) { return cultivoRepository.findById(id).orElse(null); }
    public void eliminar(Long id) { cultivoRepository.deleteById(id); }
}
