package com.AgroLink.ProyectoAngular.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.AgroLink.ProyectoAngular.model.DatosContacto;
import com.AgroLink.ProyectoAngular.repository.DatosContactoRepository;

@Service
public class DatosContactoService {
    private final DatosContactoRepository datosContactoRepository;
    public DatosContactoService(DatosContactoRepository datosContactoRepository) { this.datosContactoRepository = datosContactoRepository; }

    public List<DatosContacto> listarTodos() { return datosContactoRepository.findAll(); }
    public DatosContacto guardar(DatosContacto contacto) { return datosContactoRepository.save(contacto); }
    public DatosContacto buscarPorId(Long id) { return datosContactoRepository.findById(id).orElse(null); }
    public void eliminar(Long id) { datosContactoRepository.deleteById(id); }
}