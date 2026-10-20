package com.portal.residencial.casos_service.auth;

/* DATOS DEL PERSONAL */
public record PersonalResponse(
        Long id,
        String nombre,
        String apellido,
        String correo
) {
    public String nombreCompleto() {
        return ((nombre == null ? "" : nombre) + " " + (apellido == null ? "" : apellido)).trim();
    }
}
