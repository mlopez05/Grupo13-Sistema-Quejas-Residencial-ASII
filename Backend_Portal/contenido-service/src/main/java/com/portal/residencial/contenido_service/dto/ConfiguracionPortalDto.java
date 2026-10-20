package com.portal.residencial.contenido_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/* DATOS DEL RESIDENCIAL */
public record ConfiguracionPortalDto(
        @NotBlank @Size(max = 100) String nombreResidencial,
        @Size(max = 150) String lema,

        @Size(max = 150) String razonSocial,
        @Size(max = 30) String nit,
        @Size(max = 250) String direccion,
        @Size(max = 30) String telefono,
        @Email @Size(max = 120) String correoContacto,
        @Size(max = 150) String horarioAtencion,

        @NotBlank @Size(max = 150) String tituloBienvenida,
        @Size(max = 500) String mensajeBienvenida,
        @Size(max = 1500) String descripcionComunidad,
        @Size(max = 1000) String mision,
        @Size(max = 1000) String vision
) {}
