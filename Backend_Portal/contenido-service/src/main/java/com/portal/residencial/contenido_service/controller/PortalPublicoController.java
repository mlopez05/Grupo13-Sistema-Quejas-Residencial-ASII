package com.portal.residencial.contenido_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.portal.residencial.contenido_service.dto.AvisoDtos.AvisoResponse;
import com.portal.residencial.contenido_service.dto.ConfiguracionPortalDto;
import com.portal.residencial.contenido_service.dto.ServicioDtos.ServicioResponse;
import com.portal.residencial.contenido_service.service.ContenidoService;

import lombok.RequiredArgsConstructor;

/* CONTENIDO DEL PORTAL PÚBLICO */
@RestController
@RequestMapping("/api/portal/publico")
@RequiredArgsConstructor
public class PortalPublicoController {

    private final ContenidoService contenidoService;

    @GetMapping("/configuracion")
    public ResponseEntity<ConfiguracionPortalDto> configuracion() {
        return ResponseEntity.ok(contenidoService.obtenerConfiguracion());
    }

    /* AVISOS VIGENTES */
    @GetMapping("/avisos")
    public ResponseEntity<List<AvisoResponse>> avisos(@RequestParam(required = false) Integer limite) {
        return ResponseEntity.ok(contenidoService.listarAvisosVigentes(limite));
    }

    @GetMapping("/servicios")
    public ResponseEntity<List<ServicioResponse>> servicios() {
        return ResponseEntity.ok(contenidoService.listarServiciosActivos());
    }
}
