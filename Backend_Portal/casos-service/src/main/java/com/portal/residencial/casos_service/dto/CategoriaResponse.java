package com.portal.residencial.casos_service.dto;

import java.util.List;

/* CATEGORÍA CON SUS TIPOS DE INCIDENTE */
public record CategoriaResponse(
    Long id,
    String nombre,
    List<TipoIncidenteResponse> tipos
) {}
