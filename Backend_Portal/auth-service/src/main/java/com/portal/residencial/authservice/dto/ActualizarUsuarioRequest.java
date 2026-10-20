package com.portal.residencial.authservice.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Pattern;

public record ActualizarUsuarioRequest(
    @Pattern(regexp = ".*\\S.*", message = "no puede estar vacío") String nombreUsuario,
    @Pattern(regexp = ".*\\S.*", message = "no puede estar vacío") String apellidoUsuario,
    LocalDate fechaNacimiento,
    @Pattern(regexp = ".*\\S.*", message = "no puede estar vacío") String dpi,
    @Pattern(regexp = ".*\\S.*", message = "no puede estar vacío") String direccion,
    @Pattern(regexp = ".*\\S.*", message = "no puede estar vacío") String telefono,
    @Pattern(regexp = "Activo|Inactivo", message = "debe ser 'Activo' o 'Inactivo'") String estado
) {

}
