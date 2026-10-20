package com.portal.residencial.casos_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SeguimientoResponse(
    Long id,
    String tipo,
    String estadoAnterior,
    String estadoNuevo,
    String comentario,
    String nombreUsuario,
    String rolUsuario,
    LocalDateTime fecha,
    List<DocumentoResponse> documentos   /* ADJUNTOS DE ESTE MOVIMIENTO */
) {}
