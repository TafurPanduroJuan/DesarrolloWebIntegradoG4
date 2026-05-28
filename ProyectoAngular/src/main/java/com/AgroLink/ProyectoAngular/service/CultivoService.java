package com.AgroLink.ProyectoAngular.service;

import com.AgroLink.ProyectoAngular.dto.SeguimientoRequest;
import com.AgroLink.ProyectoAngular.model.Cultivo;
import com.AgroLink.ProyectoAngular.model.EventoProduccion;
import com.AgroLink.ProyectoAngular.model.enums.EstadoCultivoEnum;
import com.AgroLink.ProyectoAngular.repository.CultivoRepository;
import com.AgroLink.ProyectoAngular.repository.EventoProduccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RF02  - Registro de cultivos
 * RF03  - Seguimiento de producción (estado, etapa, observación)
 * RF16  - Gestión de lotes agrícolas vinculados al cultivo
 * RF17  - Gestión de eventos de producción
 */
@Service
public class CultivoService {

    @Autowired
    private CultivoRepository cultivoRepository;

    @Autowired
    private EventoProduccionRepository eventoProduccionRepository;

    // ---- RF02 / RF16: CRUD Cultivos ----
    @Transactional
    public Cultivo crear(Cultivo cultivo) {
        if (cultivo.getEstado() == null) {
            cultivo.setEstado(EstadoCultivoEnum.SEMBRADO);
        }
        return cultivoRepository.save(cultivo);
    }

    @Transactional
    public Cultivo actualizar(Long id, Cultivo datos) {
        Cultivo existing = cultivoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cultivo no encontrado: " + id));
        datos.setId(id);
        datos.setFechaCreacion(existing.getFechaCreacion());
        return cultivoRepository.save(datos);
    }

    // ---- RF03: Seguimiento de producción ----
    @Transactional
    public Cultivo actualizarSeguimiento(Long id, SeguimientoRequest req) {
        Cultivo cultivo = cultivoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cultivo no encontrado: " + id));
        cultivo.setEstado(req.getEstado());
        cultivo.setEtapaProductiva(req.getEtapaProductiva());
        cultivo.setObservacionSeguimiento(req.getObservacion());
        cultivo.setFechaUltimoSeguimiento(LocalDateTime.now());
        return cultivoRepository.save(cultivo);
    }

    public List<Cultivo> listarTodos() {
        return cultivoRepository.findAll();
    }

    public List<Cultivo> listarPorAgricultor(Long agricultorId) {
        return cultivoRepository.findByAgricultorId(agricultorId);
    }

    public List<Cultivo> listarPorAgricultorYEstado(Long agricultorId, EstadoCultivoEnum estado) {
        return cultivoRepository.findByAgricultorIdAndEstado(agricultorId, estado);
    }

    public Cultivo buscarPorId(Long id) {
        return cultivoRepository.findById(id).orElse(null);
    }

    public void eliminar(Long id) {
        cultivoRepository.deleteById(id);
    }

    // ---- RF17: Eventos de producción ----
    @Transactional
    public EventoProduccion registrarEvento(EventoProduccion evento) {
        // validar que existe el cultivo
        cultivoRepository.findById(evento.getCultivoId())
                .orElseThrow(() -> new IllegalArgumentException("Cultivo no encontrado: " + evento.getCultivoId()));
        if (evento.getFecha() == null) evento.setFecha(LocalDateTime.now());
        return eventoProduccionRepository.save(evento);
    }

    public List<EventoProduccion> listarEventosPorCultivo(Long cultivoId) {
        return eventoProduccionRepository.findByCultivoId(cultivoId);
    }

    public EventoProduccion buscarEventoPorId(Long id) {
        return eventoProduccionRepository.findById(id).orElse(null);
    }

    @Transactional
    public EventoProduccion actualizarEvento(Long id, EventoProduccion datos) {
        eventoProduccionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado: " + id));
        datos.setId(id);
        return eventoProduccionRepository.save(datos);
    }

    public void eliminarEvento(Long id) {
        eventoProduccionRepository.deleteById(id);
    }
}
