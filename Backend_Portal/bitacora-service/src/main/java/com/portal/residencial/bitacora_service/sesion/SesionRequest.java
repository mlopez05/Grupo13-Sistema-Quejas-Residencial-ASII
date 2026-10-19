package com.portal.residencial.bitacora_service.sesion;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;

public record SesionRequest(

        @NotNull Long idUsuario,
        @NotBlank String ipUsuario,
        @NotBlank String correoUsuario,
        @NotBlank String nombreUsuario,
        @NotBlank String apellidoUsuario,
        @NotBlank String rolUsuario,
        @NotBlank String tipoEvento
) {}
 