package com.portal.residencial.bitacora_service.reporte;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.portal.residencial.bitacora_service.reporte.AuditoriaService.ArchivoReporte;

import lombok.RequiredArgsConstructor;

/* REPORTE DE AUDITORÍA */
@RestController
@RequestMapping("/bitacora/reportes/{tipo}")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<PaginaResponse<RegistroAuditoriaResponse>> consultar(@PathVariable String tipo,
            @ModelAttribute FiltroAuditoria filtro) {
        return ResponseEntity.ok(auditoriaService.consultar(tipo, filtro));
    }

    @GetMapping("/eventos")
    public ResponseEntity<List<String>> eventos(@PathVariable String tipo) {
        return ResponseEntity.ok(auditoriaService.eventos(tipo));
    }

    /* EXPORTA A EXCEL O PDF */
    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(@PathVariable String tipo, @ModelAttribute FiltroAuditoria filtro,
            @RequestParam String formato) {
        ArchivoReporte archivo = auditoriaService.exportar(tipo, filtro, formato);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.tipoMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(archivo.nombre(), StandardCharsets.UTF_8).build().toString())
                .body(archivo.contenido());
    }
}
