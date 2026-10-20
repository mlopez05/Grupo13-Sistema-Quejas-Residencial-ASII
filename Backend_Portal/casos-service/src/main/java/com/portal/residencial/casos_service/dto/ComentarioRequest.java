package com.portal.residencial.casos_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/* COMENTARIO OBLIGATORIO */
public record ComentarioRequest(
    @NotBlank @Size(max = 1000, message = "no puede superar 1000 caracteres") String comentario
) {}
