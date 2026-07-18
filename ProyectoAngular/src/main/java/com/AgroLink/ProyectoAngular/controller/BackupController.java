package com.AgroLink.ProyectoAngular.controller;

import com.AgroLink.ProyectoAngular.service.BackupService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * RNF-09 — Backup automático de PostgreSQL.
 * Solo ADMINISTRADOR puede disparar, listar o descargar backups.
 */
@RestController
@RequestMapping("/api/backups")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @GetMapping
    public ResponseEntity<List<BackupService.BackupInfo>> listar() {
        return ResponseEntity.ok(backupService.listarBackups());
    }

    @PostMapping("/ejecutar")
    public ResponseEntity<?> ejecutarManual() {
        BackupService.ResultadoBackup resultado = backupService.ejecutarBackup("MANUAL");
        if (!resultado.exitoso()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", resultado.mensaje()));
        }
        return ResponseEntity.ok(Map.of("archivo", resultado.archivo(), "mensaje", resultado.mensaje()));
    }

    @GetMapping("/{nombreArchivo}/descargar")
    public ResponseEntity<Resource> descargar(@PathVariable String nombreArchivo) {
        Path archivo = backupService.resolverArchivoParaDescarga(nombreArchivo);
        if (archivo == null) {
            return ResponseEntity.notFound().build();
        }
        Resource recurso = new FileSystemResource(archivo);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(recurso);
    }
}
