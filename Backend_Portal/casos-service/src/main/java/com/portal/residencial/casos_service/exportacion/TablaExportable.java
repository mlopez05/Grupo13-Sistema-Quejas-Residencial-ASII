package com.portal.residencial.casos_service.exportacion;

import java.util.List;

/* TABLA DEL REPORTE */
public record TablaExportable(
        String nombre,
        List<String> columnas,
        List<List<Object>> filas
) {}
