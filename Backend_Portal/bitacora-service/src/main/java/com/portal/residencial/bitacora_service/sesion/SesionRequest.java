package com.portal.residencial.bitacora_service.sesion;

import jakarta.validation.constraints.NotBlank;


public record SesionRequest(

        /* EN UN INTENTO FALLIDO CON UN CORREO NO REGISTRADO SOLO LLEGAN EL CORREO, LA IP Y EL EVENTO */
        Long idUsuario,
        @NotBlank String ipUsuario,
        @NotBlank String correoUsuario,
        String nombreUsuario,
        String apellidoUsuario,
        String rolUsuario,
        @NotBlank String tipoEvento
) {}
 