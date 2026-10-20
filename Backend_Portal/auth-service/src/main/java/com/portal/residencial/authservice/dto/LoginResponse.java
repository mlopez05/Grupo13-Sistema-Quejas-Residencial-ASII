package com.portal.residencial.authservice.dto;

public record LoginResponse(
    String token,
    String usuario,
    String rol,
    boolean debeCambiarClave   /* INDICA SI DEBE CAMBIAR SU CONTRASEÑA TEMPORAL */
) {}
