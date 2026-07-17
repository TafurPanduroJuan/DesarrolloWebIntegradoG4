package com.AgroLink.ProyectoAngular.repository;

import com.AgroLink.ProyectoAngular.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
    long countByUsuarioIdAndLeidoFalse(Long usuarioId);
}
