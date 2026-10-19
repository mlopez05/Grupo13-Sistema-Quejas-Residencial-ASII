package com.portal.residencial.authservice.bitacora;

public record SesionRequest(
        Long idUsuario,
        String ipUsuario,
        String correoUsuario,
        String nombreUsuario,
        String apellidoUsuario,
        String rolUsuario,
        String tipoEvento
) {

}
