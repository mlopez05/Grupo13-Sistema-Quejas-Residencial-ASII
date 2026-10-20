package com.portal.residencial.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/* DATOS PARA CAMBIAR LA CONTRASEÑA */
public record CambiarClaveRequest(
        @NotBlank String claveActual,

        @NotBlank
        @Size(min = 8, message = "debe tener al menos 8 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).+$",
                 message = "debe incluir mayúsculas, minúsculas y números")
        String claveNueva
) {}
