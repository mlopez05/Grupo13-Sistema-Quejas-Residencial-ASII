package com.portal.residencial.bitacora_service.caso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CasoRequest(
        @NotNull Long idCaso,
        @NotBlank String numeroCaso,
        @NotBlank String tipoEvento,
        String estadoAnterior,
        String estadoNuevo,
        String descripcion,
        String ipUsuario,
        Long idUsuario, /* VACÍO CUANDO LO HACE EL SISTEMA */
        String nombreUsuario,
        String apellidoUsuario,
        @NotBlank String rolUsuario
) {}
