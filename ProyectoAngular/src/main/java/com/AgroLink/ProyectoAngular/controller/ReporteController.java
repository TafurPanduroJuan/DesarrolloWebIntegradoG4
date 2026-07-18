package com.AgroLink.ProyectoAngular.controller;

import com.AgroLink.ProyectoAngular.dto.reporte.ReporteVentasResponse;
import com.AgroLink.ProyectoAngular.model.Usuario;
import com.AgroLink.ProyectoAngular.model.enums.RolEnum;
import com.AgroLink.ProyectoAngular.service.ReporteExportService;
import com.AgroLink.ProyectoAngular.service.ReporteService;
import com.AgroLink.ProyectoAngular.service.UsuarioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * RF-10 — Historial y reportes comerciales.
 * RF-26 — Exportación de reportes en PDF/Excel (usa los mismos filtros que /ventas).
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;
    private final ReporteExportService reporteExportService;
    private final UsuarioService usuarioService;

    public ReporteController(ReporteService reporteService,
                              ReporteExportService reporteExportService,
                              UsuarioService usuarioService) {
        this.reporteService = reporteService;
        this.reporteExportService = reporteExportService;
        this.usuarioService = usuarioService;
    }

    private Usuario getUsuarioLogueado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioService.buscarPorEmail(email);
    }

    @GetMapping("/ventas")
    public ResponseEntity<?> reporteVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long agricultorId) {
        Usuario usuario = getUsuarioLogueado();
        if (usuario == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (agricultorId != null && usuario.getRol() != RolEnum.ADMINISTRADOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo un administrador puede filtrar por agricultor"));
        }
        ReporteVentasResponse reporte = reporteService.generarReporteVentas(usuario, desde, hasta, agricultorId);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/ventas/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long agricultorId) {
        Usuario usuario = getUsuarioLogueado();
        if (usuario == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (agricultorId != null && usuario.getRol() != RolEnum.ADMINISTRADOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        ReporteVentasResponse reporte = reporteService.generarReporteVentas(usuario, desde, hasta, agricultorId);
        byte[] pdf = reporteExportService.generarPdf(reporte);
        String filename = "reporte-comercial-" + LocalDate.now() + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/ventas/exportar/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long agricultorId) {
        Usuario usuario = getUsuarioLogueado();
        if (usuario == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (agricultorId != null && usuario.getRol() != RolEnum.ADMINISTRADOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        ReporteVentasResponse reporte = reporteService.generarReporteVentas(usuario, desde, hasta, agricultorId);
        byte[] excel = reporteExportService.generarExcel(reporte);
        String filename = "reporte-comercial-" + LocalDate.now() + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
