package com.portal.residencial.authservice.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
    int status,
    String mensaje,
    LocalDateTime fecha
) {}
