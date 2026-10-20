package com.portal.residencial.bitacora_service.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioRequest(
        @NotNull Long idUsuarioModificado,
        @NotBlank String nombreUsuarioModificado,
        @NotBlank String apellidoUsuarioModificado,
        @NotBlank String campoModificado,
        String valorAnterior,
        String valorNuevo,
        @NotBlank String tipoEvento,
        @NotBlank String ipUsuarioModificador,
        @NotNull Long idUsuarioModificador,
        @NotBlank String nombreUsuarioModificador,
        @NotBlank String apellidoUsuarioModificador
    ) {

}
