package com.portal.residencial.authservice.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank String nuevaPassword
) {}