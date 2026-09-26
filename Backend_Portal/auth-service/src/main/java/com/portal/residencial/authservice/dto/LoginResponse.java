package com.portal.residencial.authservice.dto;

public record LoginResponse(
    String token,
    String usuario,
    String rol
) {}
