package com.portal.residencial.casos_service.dto;

import jakarta.validation.constraints.Size;

/* COMENTARIO OPCIONAL AL AUTORIZAR */
public record AutorizarRequest(
    @Size(max = 1000, message = "no puede superar 1000 caracteres") String comentario
) {}
