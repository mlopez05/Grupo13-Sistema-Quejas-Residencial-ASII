package com.portal.residencial.authservice.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Pattern;

/* DATOS QUE EL USUARIO PUEDE CAMBIAR DE SU PERFIL (TODOS OPCIONALES) */
public record ActualizarPerfilRequest(
    @Pattern(regexp = ".*\\S.*", message = "no puede estar vacío") String nombreUsuario,
    @Pattern(regexp = ".*\\S.*", message = "no puede estar vacío") String apellidoUsuario,
    LocalDate fechaNacimiento,
    @Pattern(regexp = ".*\\S.*", message = "no puede estar vacío") String direccion,
    @Pattern(regexp = ".*\\S.*", message = "no puede estar vacío") String telefono
) {

}
