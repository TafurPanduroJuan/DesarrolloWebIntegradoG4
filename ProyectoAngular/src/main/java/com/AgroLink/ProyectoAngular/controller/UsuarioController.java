package com.AgroLink.ProyectoAngular.controller;

import com.AgroLink.ProyectoAngular.dto.ValidacionRequest;
import com.AgroLink.ProyectoAngular.model.Usuario;
import com.AgroLink.ProyectoAngular.model.enums.RolEnum;
import com.AgroLink.ProyectoAngular.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * RF01  - Gestión de usuarios y roles (solo ADMINISTRADOR)
 * RF15  - Validación de cuenta de agricultor (solo ADMINISTRADOR)
 * RNF11 - 403 si rol no autorizado
 */
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // RF01 - listar todos (ADMIN)
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    // RF15 - listar agricultores pendientes de validación
    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<Usuario>> listarPendientes() {
        return ResponseEntity.ok(usuarioService.listarPendientes());
    }

    // Listar por rol
    @GetMapping("/rol/{rol}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<Usuario>> listarPorRol(@PathVariable RolEnum rol) {
        return ResponseEntity.ok(usuarioService.listarPorRol(rol));
    }

    // Buscar por id - el propio usuario o ADMIN
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        if (usuario == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(usuario);
    }

    // RF01 - activar / desactivar usuario (ADMIN)
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id,
                                           @RequestBody Map<String, Boolean> body) {
        Boolean activo = body.get("activo");
        if (activo == null) return ResponseEntity.badRequest().body(Map.of("error", "Se requiere campo 'activo'"));
        try {
            Usuario updated = usuarioService.actualizarEstado(id, activo);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // RF15 - validar agricultor (ADMIN): APROBADO / OBSERVADO / RECHAZADO
    @PatchMapping("/{id}/validacion")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> validarAgricultor(@PathVariable Long id,
                                               @Valid @RequestBody ValidacionRequest req) {
        try {
            Usuario updated = usuarioService.validarAgricultor(id, req);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // RF01 - actualizar datos
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(@PathVariable Long id, @RequestBody Usuario usuario) {
        if (usuarioService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        usuario.setId(id);
        return ResponseEntity.ok(usuarioService.guardar(usuario));
    }

    // RF01 - eliminar (ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (usuarioService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
