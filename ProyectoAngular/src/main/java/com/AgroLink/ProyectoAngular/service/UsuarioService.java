package com.AgroLink.ProyectoAngular.service;

import com.AgroLink.ProyectoAngular.dto.RegisterRequest;
import com.AgroLink.ProyectoAngular.dto.ValidacionRequest;
import com.AgroLink.ProyectoAngular.model.Usuario;
import com.AgroLink.ProyectoAngular.model.enums.EstadoValidacionEnum;
import com.AgroLink.ProyectoAngular.model.enums.RolEnum;
import com.AgroLink.ProyectoAngular.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.AgroLink.ProyectoAngular.service.AuditoriaService;
import com.AgroLink.ProyectoAngular.service.NotificacionService;
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

    @Autowired
    private AuditoriaService auditoriaService;

    @Autowired
    private NotificacionService notificacionService;

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

        Usuario saved = usuarioRepository.save(usuario);
        auditoriaService.registrarAuditoria("CREACION_USUARIO", 
            "Usuario registrado exitosamente con email: " + saved.getEmail() + " y rol: " + saved.getRol());
        return saved;
    }

    // ---- RF15: Validar agricultor ----
    @Transactional
    public Usuario validarAgricultor(Long id, ValidacionRequest req) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        if (usuario.getRol() != RolEnum.AGRICULTOR) {
            throw new IllegalArgumentException("Solo se pueden validar agricultores");
        }

        boolean anteriorActivo = Boolean.TRUE.equals(usuario.getActivo());
        usuario.setEstadoValidacion(req.getEstadoValidacion());
        usuario.setMotivoObservacion(req.getMotivoObservacion());

        // Si aprueba → activa cuenta; si rechaza/observa → desactiva
        boolean nuevoActivo = req.getEstadoValidacion() == EstadoValidacionEnum.APROBADO;
        usuario.setActivo(nuevoActivo);

        Usuario saved = usuarioRepository.save(usuario);

        // Notificaciones internas (RF-24)
        String msg = "Tu cuenta de agricultor ha sido " + req.getEstadoValidacion();
        if (req.getMotivoObservacion() != null && !req.getMotivoObservacion().isBlank()) {
            msg += ". Motivo: " + req.getMotivoObservacion();
        }
        notificacionService.enviarNotificacion(saved.getId(), msg, "CAMBIO_ESTADO", saved.getId());

        // Auditoría si cambia activo (eliminación lógica) (RF-27)
        if (anteriorActivo != nuevoActivo) {
            auditoriaService.registrarAuditoria("ELIMINACION_LOGICA", 
                "El estado activo del usuario " + saved.getEmail() + " cambió a " + nuevoActivo + " por validación " + req.getEstadoValidacion());
        }

        return saved;
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
        boolean anteriorActivo = Boolean.TRUE.equals(usuario.getActivo());
        usuario.setActivo(activo);
        Usuario saved = usuarioRepository.save(usuario);

        // Auditoría de deactivación / activación (eliminación lógica) (RF-27)
        if (anteriorActivo != activo) {
            auditoriaService.registrarAuditoria("ELIMINACION_LOGICA", 
                "El estado activo del usuario " + saved.getEmail() + " se cambió a " + activo + " (Eliminación lógica/Activación)");
        }
        return saved;
    }

    @Transactional
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }
}
