package com.portal.residencial.authservice.bitacora;

public record UsuarioRequest(
        Long idUsuarioModificado,
        String nombreUsuarioModificado,
        String apellidoUsuarioModificado,
        String campoModificado,
        String valorAnterior,
        String valorNuevo,
        String tipoEvento,
        String ipUsuarioModificador,
        Long idUsuarioModificador,
        String nombreUsuarioModificador,
        String apellidoUsuarioModificador
) {

}
