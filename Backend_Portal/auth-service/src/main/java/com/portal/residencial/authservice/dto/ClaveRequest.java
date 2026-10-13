package com.portal.residencial.authservice.dto;

import jakarta.validation.constraints.NotBlank;

public record ClaveRequest(
    @NotBlank String nuevaClave
) {

}
