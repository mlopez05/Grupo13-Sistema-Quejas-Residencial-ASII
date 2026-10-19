package com.portal.residencial.authservice.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

public record ActualizarUsuarioRequest(
    @NotBlank String nombreUsuario,
    @NotBlank String apellidoUsuario,
    LocalDate fechaNacimiento,
    @NotBlank String dpi,
    @NotBlank String direccion,
    @NotBlank String telefono,
    @NotBlank String estado
) {

}
