package com.portal.residencial.contenido_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/* DATOS DEL RESIDENCIAL */
public record ConfiguracionPortalDto(
        @NotBlank @Size(max = 100) String nombreResidencial,
        @Size(max = 150) String lema,

        @Size(max = 150) String razonSocial,
        /* NÚMEROS, GUION Y UN DÍGITO O K AL FINAL (EJ. 1234567-K) */
        @Pattern(regexp = "^$|^[0-9]{1,12}-[0-9Kk]$",
                message = "el NIT debe tener el formato 1234567-8 o 1234567-K")
        String nit,
        @Size(max = 250) String direccion,
        /* SOLO 8 NÚMEROS */
        @Pattern(regexp = "^$|^[0-9]{8}$", message = "el teléfono debe tener 8 números")
        String telefono,
        @Email @Size(max = 120) String correoContacto,
        @Size(max = 150) String horarioAtencion,

        @NotBlank @Size(max = 150) String tituloBienvenida,
        @Size(max = 500) String mensajeBienvenida,
        @Size(max = 1500) String descripcionComunidad,
        @Size(max = 1000) String mision,
        @Size(max = 1000) String vision
) {}
