package com.portal.residencial.casos_service.dto;

import jakarta.validation.constraints.NotNull;

public record AsignarAnalistaRequest(
    @NotNull Long idAnalista
) {}
