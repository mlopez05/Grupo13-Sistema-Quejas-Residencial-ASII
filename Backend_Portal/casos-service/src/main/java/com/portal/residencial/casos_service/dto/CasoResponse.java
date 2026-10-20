package com.portal.residencial.casos_service.dto;

import java.time.LocalDateTime;

public record CasoResponse(
    Long id,
    String numeroCaso,
    String categoria,
    String tipoIncidente,
    String descripcion,
    String estado,              /* CÓDIGO DEL ESTADO */
    String estadoDescripcion,   /* NOMBRE DEL ESTADO PARA MOSTRAR */
    String nombreResidente,
    String correoResidente,
    String direccionResidente,
    Long idAnalista,
    String nombreAnalista,
    String correoAnalista,
    LocalDateTime fechaRegistro,
    LocalDateTime fechaActualizacion,
    LocalDateTime fechaCierre
) {}
