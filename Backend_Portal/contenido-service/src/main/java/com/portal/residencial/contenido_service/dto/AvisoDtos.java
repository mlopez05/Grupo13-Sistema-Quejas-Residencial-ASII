package com.portal.residencial.contenido_service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AvisoDtos {

    private AvisoDtos() {
    }

    public record AvisoResponse(
            Long id,
            String titulo,
            String contenido,
            String categoria,          /* CATEGORÍA DEL AVISO */
            String categoriaEtiqueta,
            boolean publicado,
            LocalDate fechaPublicacion,
            LocalDate fechaVencimiento,
            boolean vigente,           /* INDICA SI SE VE EN EL PORTAL */
            LocalDateTime fechaActualizacion,
            String actualizadoPor
    ) {}

    public record AvisoRequest(
            @NotBlank @Size(max = 150) String titulo,
            @NotBlank @Size(max = 2000) String contenido,
            @NotBlank String categoria,
            @NotNull Boolean publicado,
            @NotNull LocalDate fechaPublicacion,
            LocalDate fechaVencimiento
    ) {}
}
