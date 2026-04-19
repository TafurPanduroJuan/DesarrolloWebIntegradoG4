package com.AgroLink.ProyectoAngular.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.AgroLink.ProyectoAngular.model.DatosContacto;
import com.AgroLink.ProyectoAngular.service.DatosContactoService;

@RestController
@RequestMapping("/api/datos-contacto")
@CrossOrigin(origins = "*")
public class DatosContactoController {

    private final DatosContactoService datosContactoService;

    public DatosContactoController(DatosContactoService datosContactoService) {
        this.datosContactoService = datosContactoService;
    }

    @GetMapping
    public ResponseEntity<List<DatosContacto>> listarTodos() {
        return ResponseEntity.ok(datosContactoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatosContacto> buscarPorId(@PathVariable Long id) {
        DatosContacto contacto = datosContactoService.buscarPorId(id);
        if (contacto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(contacto);
    }

    @PostMapping
    public ResponseEntity<DatosContacto> crear(@RequestBody DatosContacto contacto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(datosContactoService.guardar(contacto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DatosContacto> actualizar(@PathVariable Long id, @RequestBody DatosContacto contacto) {
        if (datosContactoService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        contacto.setId(id);
        return ResponseEntity.ok(datosContactoService.guardar(contacto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (datosContactoService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        datosContactoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
