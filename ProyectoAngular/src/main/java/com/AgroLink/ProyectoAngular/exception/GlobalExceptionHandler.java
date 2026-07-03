package com.AgroLink.ProyectoAngular.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;



@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<?> handleTransactionSystem(TransactionSystemException ex) {
        Throwable root = ex.getRootCause() != null ? ex.getRootCause() : ex;
        log.error("Fallo al hacer commit de la transacción. Causa raíz:", root);

        if (root instanceof ConstraintViolationException cve) {
            Map<String, String> errores = cve.getConstraintViolations().stream()
                    .collect(Collectors.toMap(
                            v -> v.getPropertyPath().toString(),
                            ConstraintViolation::getMessage,
                            (a, b) -> a,
                            LinkedHashMap::new));
            return ResponseEntity.unprocessableEntity().body(Map.of(
                    "error", "Error de validación al guardar en base de datos",
                    "detalles", errores));
        }

        return ResponseEntity.unprocessableEntity().body(Map.of(
                "error", "No se pudo completar la operación en base de datos",
                "detalle", root.getMessage()));
    }

    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("Violación de integridad de datos:", ex);
        Throwable root = ex.getRootCause() != null ? ex.getRootCause() : ex;
        return ResponseEntity.unprocessableEntity().body(Map.of(
                "error", "Los datos enviados violan una restricción de la base de datos",
                "detalle", root.getMessage()));
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errores = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (a, b) -> a,
                        LinkedHashMap::new));
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Error de validación",
                "detalles", errores));
    }
}