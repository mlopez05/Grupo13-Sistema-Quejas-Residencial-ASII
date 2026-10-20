package com.portal.residencial.casos_service.dto.reporte;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/* FILTROS DEL REPORTE DE CASOS */
public record FiltroReporteCasos(
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
        String estado,
        Long idCategoria,
        Long idAnalista,
        @Min(1) @Max(365) Integer diasEstancado
) {
    public int diasEstancadoEfectivo() {
        return diasEstancado == null ? 7 : diasEstancado;
    }
}
