package com.portal.residencial.casos_service.auth;

import java.time.LocalDate;

/* DATOS DEL USUARIO */
public record UsuarioResponse(
        Long id,
        String nombre,
        String apellido,
        String correo,
        LocalDate fechaNacimiento,
        String dpi,
        String telefono,
        String direccion,
        String rol,
        String estado
) {}
