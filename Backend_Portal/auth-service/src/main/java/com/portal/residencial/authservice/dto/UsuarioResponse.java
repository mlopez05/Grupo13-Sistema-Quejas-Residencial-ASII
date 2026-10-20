package com.portal.residencial.authservice.dto;

import java.time.LocalDate;

public record UsuarioResponse(
    Long id, String nombre, String apellido, String correo,
    LocalDate fechaNacimiento, String dpi, String telefono,
    String direccion, String rol,   
    String estado                  
) {}
