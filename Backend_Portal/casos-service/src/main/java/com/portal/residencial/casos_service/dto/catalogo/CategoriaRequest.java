package com.portal.residencial.casos_service.dto.catalogo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank @Size(max = 100, message = "no puede superar 100 caracteres") String nombre,
        @NotNull Boolean activo
) {}
