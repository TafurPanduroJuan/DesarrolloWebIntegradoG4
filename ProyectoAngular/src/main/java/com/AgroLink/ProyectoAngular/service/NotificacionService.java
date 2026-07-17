package com.AgroLink.ProyectoAngular.service;

import com.AgroLink.ProyectoAngular.model.Notificacion;
import com.AgroLink.ProyectoAngular.repository.NotificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    @Transactional
    public void enviarNotificacion(Long usuarioId, String mensaje, String tipo, Long idReferencia) {
        Notificacion n = new Notificacion();
        n.setUsuarioId(usuarioId);
        n.setMensaje(mensaje);
        n.setTipo(tipo);
        n.setIdReferencia(idReferencia);
        n.setFecha(LocalDateTime.now());
        n.setLeido(false);
        notificacionRepository.save(n);
    }

    public List<Notificacion> obtenerPorUsuario(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    @Transactional
    public void marcarComoLeida(Long notificacionId) {
        notificacionRepository.findById(notificacionId).ifPresent(n -> {
            n.setLeido(true);
            notificacionRepository.save(n);
        });
    }

    @Transactional
    public void marcarTodasComoLeidas(Long usuarioId) {
        List<Notificacion> list = notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
        for (Notificacion n : list) {
            if (!Boolean.TRUE.equals(n.getLeido())) {
                n.setLeido(true);
                notificacionRepository.save(n);
            }
        }
    }

    public long contarNoLeidas(Long usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeidoFalse(usuarioId);
    }
}
