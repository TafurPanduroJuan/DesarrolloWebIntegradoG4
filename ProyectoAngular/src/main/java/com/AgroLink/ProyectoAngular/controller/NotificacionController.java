package com.AgroLink.ProyectoAngular.controller;

import com.AgroLink.ProyectoAngular.model.Notificacion;
import com.AgroLink.ProyectoAngular.model.Usuario;
import com.AgroLink.ProyectoAngular.service.NotificacionService;
import com.AgroLink.ProyectoAngular.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final UsuarioService usuarioService;

    public NotificacionController(NotificacionService notificacionService, UsuarioService usuarioService) {
        this.notificacionService = notificacionService;
        this.usuarioService = usuarioService;
    }

    private Usuario getUsuarioLogueado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioService.buscarPorEmail(email);
    }

    @GetMapping
    public ResponseEntity<List<Notificacion>> obtenerMisNotificaciones() {
        Usuario u = getUsuarioLogueado();
        if (u == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(notificacionService.obtenerPorUsuario(u.getId()));
    }

    @GetMapping("/no-leidas/count")
    public ResponseEntity<Map<String, Long>> contarNoLeidas() {
        Usuario u = getUsuarioLogueado();
        if (u == null) return ResponseEntity.status(401).build();
        long count = notificacionService.contarNoLeidas(u.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PatchMapping("/{id}/leer")
    public ResponseEntity<Void> marcarLeida(@PathVariable Long id) {
        notificacionService.marcarComoLeida(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/leer-todas")
    public ResponseEntity<Void> marcarTodasLeidas() {
        Usuario u = getUsuarioLogueado();
        if (u == null) return ResponseEntity.status(401).build();
        notificacionService.marcarTodasComoLeidas(u.getId());
        return ResponseEntity.ok().build();
    }
}
