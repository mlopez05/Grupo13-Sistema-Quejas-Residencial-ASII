package com.portal.residencial.casos_service.dto.catalogo;

import java.util.List;

/* CATALOGO COMPLETO PARA EL ADMINISTRADOR, CON CUANTOS CASOS USAN CADA ELEMENTO */
public record CategoriaAdminResponse(
        Long id,
        String nombre,
        boolean activo,
        long casos,
        List<TipoAdmin> tipos
) {
    public record TipoAdmin(Long id, String nombre, boolean activo, long casos) {}
}
