package com.AgroLink.ProyectoAngular.service;

import com.AgroLink.ProyectoAngular.dto.RegisterRequest;
import com.AgroLink.ProyectoAngular.dto.ValidacionRequest;
import com.AgroLink.ProyectoAngular.model.Usuario;
import com.AgroLink.ProyectoAngular.model.enums.EstadoValidacionEnum;
import com.AgroLink.ProyectoAngular.model.enums.RolEnum;
import com.AgroLink.ProyectoAngular.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * RF01  - Gestión de usuarios y roles
 * RF13  - Registro de agricultores (estado PENDIENTE hasta aprobación)
 * RF14  - Registro de compradores (activación automática)
 * RF15  - Validación de cuenta de agricultor por administrador
 * RNF03 - BCrypt para hash de contraseñas
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ---- RF13 & RF14: Registro ----
    @Transactional
    public Usuario registrar(RegisterRequest req) {
        if (usuarioRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado: " + req.getEmail());
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(req.getNombre());
        usuario.setApellido(req.getApellido());
        usuario.setEmail(req.getEmail());
        // RNF03 - BCrypt
        usuario.setPassword(passwordEncoder.encode(req.getPassword()));
        usuario.setTelefono(req.getTelefono());
        usuario.setRol(req.getRol());

        if (req.getRol() == RolEnum.AGRICULTOR) {
            // RF13 - queda en PENDIENTE hasta aprobación
            usuario.setDni(req.getDni());
            usuario.setRegion(req.getRegion());
            usuario.setProductoresPrincipales(req.getProductoresPrincipales());
            usuario.setDescripcionFinca(req.getDescripcionFinca());
            usuario.setEstadoValidacion(EstadoValidacionEnum.PENDIENTE);
            usuario.setActivo(false); // se activa cuando ADMINISTRADOR aprueba
        } else if (req.getRol() == RolEnum.COMPRADOR) {
            // RF14 - cuenta activa automáticamente
            usuario.setRuc(req.getRuc());
            usuario.setRazonSocial(req.getRazonSocial());
            usuario.setDireccionComercial(req.getDireccionComercial());
            usuario.setTipoComprador(req.getTipoComprador());
            usuario.setActivo(true);
        } else {
            // ADMINISTRADOR
            usuario.setActivo(true);
        }

        return usuarioRepository.save(usuario);
    }

    // ---- RF15: Validar agricultor ----
    @Transactional
    public Usuario validarAgricultor(Long id, ValidacionRequest req) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        if (usuario.getRol() != RolEnum.AGRICULTOR) {
            throw new IllegalArgumentException("Solo se pueden validar agricultores");
        }

        usuario.setEstadoValidacion(req.getEstadoValidacion());
        usuario.setMotivoObservacion(req.getMotivoObservacion());

        // Si aprueba → activa cuenta; si rechaza/observa → desactiva
        usuario.setActivo(req.getEstadoValidacion() == EstadoValidacionEnum.APROBADO);

        return usuarioRepository.save(usuario);
    }

    // ---- RF01: Gestión CRUD ----
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarPendientes() {
        return usuarioRepository.findByRolAndEstadoValidacion(
                RolEnum.AGRICULTOR, EstadoValidacionEnum.PENDIENTE);
    }

    public List<Usuario> listarPorRol(RolEnum rol) {
        return usuarioRepository.findByRol(rol);
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Transactional
    public Usuario actualizarEstado(Long id, Boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        usuario.setActivo(activo);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }
}
