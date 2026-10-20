package com.portal.residencial.casos_service.bitacora;

/* DATOS PARA LA BITÁCORA DE CASOS */
public record BitacoraCasoRequest(
        Long idCaso,
        String numeroCaso,
        String tipoEvento,
        String estadoAnterior,
        String estadoNuevo,
        String descripcion,
        String ipUsuario,
        Long idUsuario,
        String nombreUsuario,
        String apellidoUsuario,
        String rolUsuario
) {}
