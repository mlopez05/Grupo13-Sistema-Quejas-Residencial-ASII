package com.portal.residencial.casos_service.bitacora;

/* DATOS PARA LA BITÁCORA DE ADJUNTOS */
public record BitacoraDocumentoRequest(
        Long idDocumento,
        Long idCaso,
        String numeroCaso,
        String nombreArchivo,
        String tipoArchivo,
        long tamanoBytes,
        String tipoEvento,      /* CARGA O DESCARGA */
        String ipUsuario,
        Long idUsuario,
        String nombreUsuario,
        String apellidoUsuario,
        String rolUsuario
) {}
