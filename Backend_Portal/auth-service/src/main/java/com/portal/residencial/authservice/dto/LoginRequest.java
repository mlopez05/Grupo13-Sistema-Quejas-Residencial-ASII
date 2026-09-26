package com.portal.residencial.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Email String usuario,
        @NotBlank String clave
) {}
