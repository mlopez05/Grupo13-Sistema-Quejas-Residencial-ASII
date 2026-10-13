package com.portal.residencial.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CorreoRequest(
    @NotBlank @Email String usuario
) {

}
