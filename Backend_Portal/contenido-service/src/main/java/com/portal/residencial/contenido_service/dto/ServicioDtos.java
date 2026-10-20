package com.portal.residencial.contenido_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ServicioDtos {

    private ServicioDtos() {
    }

    public record ServicioResponse(
            Long id,
            String nombre,
            String descripcion,
            String horario,
            int orden,
            boolean activo
    ) {}

    public record ServicioRequest(
            @NotBlank @Size(max = 100) String nombre,
            @NotBlank @Size(max = 1000) String descripcion,
            @Size(max = 150) String horario,
            @NotNull @Min(0) @Max(999) Integer orden,
            @NotNull Boolean activo
    ) {}
}
