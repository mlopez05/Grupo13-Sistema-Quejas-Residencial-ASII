package com.portal.residencial.casos_service.config;

/* USUARIO QUE INICIÓ SESIÓN */
public record UsuarioAutenticado(
        Long id,
        String correo,
        String nombre,
        String apellido,
        String rol,
        String token
) {
    public String nombreCompleto() {
        return ((nombre == null ? "" : nombre) + " " + (apellido == null ? "" : apellido)).trim();
    }

    public boolean esRol(String nombreRol) {
        return nombreRol.equals(rol);
    }
}
