package com.portal.residencial.casos_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarCasoRequest(
    @NotNull Long idCategoria,
    @NotNull Long idTipoIncidente,
    @NotBlank @Size(min = 20, max = 2000, message = "debe tener entre 20 y 2000 caracteres") String descripcion
) {}
