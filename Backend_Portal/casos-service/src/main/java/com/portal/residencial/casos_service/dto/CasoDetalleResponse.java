package com.portal.residencial.casos_service.dto;

import java.util.List;

/* DETALLE COMPLETO DEL CASO */
public record CasoDetalleResponse(
    CasoResponse caso,
    List<SeguimientoResponse> seguimientos
) {}
