package com.AgroLink.ProyectoAngular.controller;

import com.AgroLink.ProyectoAngular.model.Auditoria;
import com.AgroLink.ProyectoAngular.service.AuditoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditorias")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public ResponseEntity<List<Auditoria>> listarTodas() {
        return ResponseEntity.ok(auditoriaService.listarTodas());
    }
}
