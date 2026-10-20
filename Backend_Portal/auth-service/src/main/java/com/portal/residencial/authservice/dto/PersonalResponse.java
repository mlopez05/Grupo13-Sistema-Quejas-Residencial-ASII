package com.portal.residencial.authservice.dto;

/* DATOS BÁSICOS DEL PERSONAL PARA OTROS SERVICIOS */
public record PersonalResponse(
    Long id,
    String nombre,
    String apellido,
    String correo
) {}
