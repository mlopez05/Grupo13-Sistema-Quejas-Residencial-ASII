package com.portal.residencial.bitacora_service.documento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentoRequest(
        @NotNull Long idDocumento,
        @NotNull Long idCaso,
        @NotBlank String numeroCaso,
        @NotBlank String nombreArchivo,
        @NotBlank String tipoArchivo,
        long tamanoBytes,
        @NotBlank String tipoEvento,
        String ipUsuario,
        @NotNull Long idUsuario,
        String nombreUsuario,
        String apellidoUsuario,
        @NotBlank String rolUsuario
) {}
