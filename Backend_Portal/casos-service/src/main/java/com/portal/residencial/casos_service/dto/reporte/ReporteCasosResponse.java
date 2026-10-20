package com.portal.residencial.casos_service.dto.reporte;

import java.time.LocalDateTime;
import java.util.List;

/* REPORTE DE CASOS */
public record ReporteCasosResponse(
        Resumen resumen,
        List<Conteo> porEstado,
        List<Conteo> porCategoria,
        List<Conteo> porTipoIncidente,
        List<DesempenoAnalista> porAnalista,
        TiemposAtencion tiempos,
        List<TendenciaMensual> tendenciaMensual,
        List<CasoEstancado> estancados
) {

    public record Resumen(
            long total,
            long abiertos,
            long finalizados,
            long rechazados,
            long sinAsignar,
            Double porcentajeRechazo   /* PORCENTAJE DE CASOS RECHAZADOS */
    ) {}

    public record Conteo(String clave, String etiqueta, long cantidad) {}

    public record DesempenoAnalista(
            Long idAnalista,
            String nombreAnalista,
            long asignados,
            long enCurso,
            long finalizados,
            long rechazados,
            Double promedioHorasResolucion
    ) {}

    /* TIEMPOS PROMEDIO EN HORAS */
    public record TiemposAtencion(
            Double registroAAsignacion,
            Double asignacionASolicitud,
            Double solicitudADecision,
            Double registroACierre
    ) {}

    public record TendenciaMensual(String mes, long registrados, long cerrados) {}

    public record CasoEstancado(
            Long id,
            String numeroCaso,
            String tipoIncidente,
            String estado,
            String estadoDescripcion,
            String nombreAnalista,
            LocalDateTime ultimaActualizacion,
            long diasSinMovimiento
    ) {}
}
