package com.portal.residencial.contenido_service.model;

/* CATEGORÍAS DE AVISO */
public enum CategoriaAviso {
    GENERAL("General"),
    IMPORTANTE("Importante"),
    MANTENIMIENTO("Mantenimiento"),
    ACTIVIDAD("Actividad"),
    COMUNIDAD("Comunidad");

    private final String etiqueta;

    CategoriaAviso(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
