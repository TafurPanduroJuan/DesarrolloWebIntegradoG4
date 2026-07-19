package com.AgroLink.ProyectoAngular.service;

import com.AgroLink.ProyectoAngular.model.SolicitudContacto;
import com.AgroLink.ProyectoAngular.model.Usuario;
import com.AgroLink.ProyectoAngular.model.enums.RolEnum;
import com.AgroLink.ProyectoAngular.repository.SolicitudContactoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SolicitudContactoService {

    private final SolicitudContactoRepository repository;
    private final UsuarioService usuarioService;
    private final NotificacionService notificacionService;

    public SolicitudContactoService(SolicitudContactoRepository repository,
                                     UsuarioService usuarioService,
                                     NotificacionService notificacionService) {
        this.repository = repository;
        this.usuarioService = usuarioService;
        this.notificacionService = notificacionService;
    }

    @Transactional
    public SolicitudContacto registrar(SolicitudContacto solicitud) {
        if (solicitud.getNombre() == null || solicitud.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (solicitud.getCorreo() == null || solicitud.getCorreo().isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }
        if (solicitud.getMensaje() == null || solicitud.getMensaje().isBlank()) {
            throw new IllegalArgumentException("El mensaje es obligatorio");
        }

        solicitud.setId(null);
        solicitud.setFecha(LocalDateTime.now());
        solicitud.setAtendida(false);
        SolicitudContacto guardada = repository.save(solicitud);

        // Avisar a todos los administradores por notificación interna (no hay
        // servicio de email configurado en el proyecto todavía).
        List<Usuario> admins = usuarioService.listarPorRol(RolEnum.ADMINISTRADOR);
        String resumen = "Nueva solicitud de contacto de " + guardada.getNombre()
            + " (" + guardada.getCorreo() + ")";
        for (Usuario admin : admins) {
            notificacionService.enviarNotificacion(admin.getId(), resumen, "CONTACTO", guardada.getId());
        }

        return guardada;
    }

    public List<SolicitudContacto> listarTodas() {
        return repository.findAllByOrderByFechaDesc();
    }

    public SolicitudContacto buscarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public SolicitudContacto marcarAtendida(Long id) {
        SolicitudContacto s = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
        s.setAtendida(true);
        return repository.save(s);
    }
}
