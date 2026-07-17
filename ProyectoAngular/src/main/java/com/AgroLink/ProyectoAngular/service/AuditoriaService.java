package com.AgroLink.ProyectoAngular.service;

import com.AgroLink.ProyectoAngular.model.Auditoria;
import com.AgroLink.ProyectoAngular.repository.AuditoriaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarAuditoria(String accion, String descripcion) {
        String usuarioEmail = null;
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            usuarioEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            if ("anonymousUser".equals(usuarioEmail)) {
                usuarioEmail = "SYSTEM/ANONYMOUS";
            }
        }

        String ipAddress = "0.0.0.0";
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }
        }

        Auditoria a = new Auditoria();
        a.setAccion(accion);
        a.setDescripcion(descripcion);
        a.setUsuarioEmail(usuarioEmail);
        a.setIpAddress(ipAddress);
        a.setFecha(LocalDateTime.now());
        
        auditoriaRepository.save(a);
    }

    public List<Auditoria> listarTodas() {
        return auditoriaRepository.findAllByOrderByFechaDesc();
    }
}
