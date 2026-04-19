package com.AgroLink.ProyectoAngular.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.AgroLink.ProyectoAngular.model.EventoProduccion;
import com.AgroLink.ProyectoAngular.repository.EventoProduccionRepository;

@Service
public class EventoProduccionService {
    private final EventoProduccionRepository eventoProduccionRepository;
    public EventoProduccionService(EventoProduccionRepository eventoProduccionRepository) { this.eventoProduccionRepository = eventoProduccionRepository; }

    public List<EventoProduccion> listarTodos() { return eventoProduccionRepository.findAll(); }
    public EventoProduccion guardar(EventoProduccion evento) { return eventoProduccionRepository.save(evento); }
    public EventoProduccion buscarPorId(Long id) { return eventoProduccionRepository.findById(id).orElse(null); }
    public void eliminar(Long id) { eventoProduccionRepository.deleteById(id); }

}