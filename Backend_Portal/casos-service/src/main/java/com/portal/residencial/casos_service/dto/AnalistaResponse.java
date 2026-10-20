package com.portal.residencial.casos_service.dto;

/* ANALISTA CON SUS CASOS EN CURSO */
public record AnalistaResponse(
    Long id,
    String nombre,
    String apellido,
    String correo,
    long casosActivos
) {}
