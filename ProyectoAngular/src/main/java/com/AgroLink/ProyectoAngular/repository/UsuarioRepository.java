package com.AgroLink.ProyectoAngular.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.AgroLink.ProyectoAngular.model.Usuario;
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmail(String email);
}
