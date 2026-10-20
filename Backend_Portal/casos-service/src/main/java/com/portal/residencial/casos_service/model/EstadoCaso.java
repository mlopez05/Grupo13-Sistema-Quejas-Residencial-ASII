package com.portal.residencial.casos_service.model;

import java.util.List;

/* ESTADOS DEL CASO */
public enum EstadoCaso {
    NUEVO("Nuevo"),
    ASIGNADO("Asignado"),
    EN_ANALISIS("En análisis"),
    PENDIENTE_AUTORIZACION("Pendiente de autorización"),
    EN_PROCESO("En proceso"),
    RECHAZADO("Rechazado"),
    FINALIZADO("Finalizado");

    /* ESTADOS QUE CUENTAN COMO CASOS EN CURSO */
    public static final List<String> ACTIVOS =
            List.of(ASIGNADO.name(), EN_ANALISIS.name(), PENDIENTE_AUTORIZACION.name(), EN_PROCESO.name());

    private final String descripcion;

    EstadoCaso(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esFinal() {
        return this == RECHAZADO || this == FINALIZADO;
    }
}
