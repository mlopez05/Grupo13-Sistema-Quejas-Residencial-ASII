package com.portal.residencial.casos_service.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.portal.residencial.casos_service.dto.reporte.FiltroReporteCasos;
import com.portal.residencial.casos_service.dto.reporte.ReporteCasosResponse;
import com.portal.residencial.casos_service.service.ReporteCasoService;
import com.portal.residencial.casos_service.service.ReporteCasoService.ArchivoReporte;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/* REPORTE DE CASOS */
@RestController
@RequestMapping("/api/casos/reportes")
@PreAuthorize("hasAnyAuthority('Supervisor', 'Administrador')")
@RequiredArgsConstructor
public class ReporteCasoController {

    private final ReporteCasoService reporteService;

    @GetMapping
    public ResponseEntity<ReporteCasosResponse> generar(@Valid @ModelAttribute FiltroReporteCasos filtro) {
        return ResponseEntity.ok(reporteService.generar(filtro));
    }

    /* EXPORTA A EXCEL O PDF */
    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(@Valid @ModelAttribute FiltroReporteCasos filtro,
            @RequestParam String formato) {
        ArchivoReporte archivo = reporteService.exportar(filtro, formato);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.tipoMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(archivo.nombre(), StandardCharsets.UTF_8).build().toString())
                .body(archivo.contenido());
    }
}
