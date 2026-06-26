package com.AgroLink.ProyectoAngular.controller;

import com.AgroLink.ProyectoAngular.dto.SeguimientoRequest;
import com.AgroLink.ProyectoAngular.model.Cultivo;
import com.AgroLink.ProyectoAngular.model.Usuario;
import com.AgroLink.ProyectoAngular.service.CultivoService;
import com.AgroLink.ProyectoAngular.service.UsuarioService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/cultivos")
public class CultivoController {

    @Autowired
    private CultivoService cultivoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private Validator validator;

    @GetMapping
    public ResponseEntity<List<Cultivo>> listarTodos() {
        return ResponseEntity.ok(cultivoService.listarTodos());
    }

    @GetMapping("/agricultor/{agricultorId}")
    public ResponseEntity<List<Cultivo>> listarPorAgricultor(@PathVariable Long agricultorId) {
        return ResponseEntity.ok(cultivoService.listarPorAgricultor(agricultorId));
    }

    @GetMapping("/mis-cultivos")
    @PreAuthorize("hasRole('AGRICULTOR')")
    public ResponseEntity<List<Cultivo>> misCultivos() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario agricultor = usuarioService.buscarPorEmail(email);
        return ResponseEntity.ok(cultivoService.listarPorAgricultor(agricultor.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cultivo> buscarPorId(@PathVariable Long id) {
        Cultivo cultivo = cultivoService.buscarPorId(id);
        if (cultivo == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(cultivo);
    }

    @PostMapping
    @PreAuthorize("hasRole('AGRICULTOR')")
    public ResponseEntity<?> crear(@RequestBody Cultivo cultivo) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario agricultor = usuarioService.buscarPorEmail(email);
        cultivo.setAgricultorId(agricultor.getId());

        Set<ConstraintViolation<Cultivo>> violaciones = validator.validate(cultivo);
        if (!violaciones.isEmpty()) {
            Map<String, String> errores = new HashMap<>();
            violaciones.forEach(v -> errores.put(v.getPropertyPath().toString(), v.getMessage()));
            return ResponseEntity.badRequest().body(errores);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(cultivoService.crear(cultivo));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AGRICULTOR')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Cultivo cultivo) {
        try {
            return ResponseEntity.ok(cultivoService.actualizar(id, cultivo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/seguimiento")
    @PreAuthorize("hasRole('AGRICULTOR')")
    public ResponseEntity<?> actualizarSeguimiento(@PathVariable Long id,
                                                    @Valid @RequestBody SeguimientoRequest req) {
        try {
            return ResponseEntity.ok(cultivoService.actualizarSeguimiento(id, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('AGRICULTOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (cultivoService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        cultivoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}