package com.AgroLink.ProyectoAngular.repository;

import com.AgroLink.ProyectoAngular.model.Usuario;
import com.AgroLink.ProyectoAngular.model.enums.EstadoValidacionEnum;
import com.AgroLink.ProyectoAngular.model.enums.RolEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmail(String email);
    boolean existsByEmail(String email);
    List<Usuario> findByRol(RolEnum rol);
    List<Usuario> findByRolAndEstadoValidacion(RolEnum rol, EstadoValidacionEnum estadoValidacion);
}
