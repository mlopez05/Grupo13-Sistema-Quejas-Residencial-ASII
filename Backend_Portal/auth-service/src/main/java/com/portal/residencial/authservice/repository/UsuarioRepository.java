package com.portal.residencial.authservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portal.residencial.authservice.model.Usuario;

public interface UsuarioRepository  extends JpaRepository<Usuario, Long>{
    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByDpi(String dpi);

    Optional<Usuario> findByCorreoAndClave(String correo, String clave);

    List<Usuario> findByRol_NombreOrderByIdAsc(String nombreRol);

    
}
