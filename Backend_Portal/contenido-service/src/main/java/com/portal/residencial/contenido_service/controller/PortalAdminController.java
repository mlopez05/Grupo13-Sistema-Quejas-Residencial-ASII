package com.portal.residencial.contenido_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.residencial.contenido_service.dto.AvisoDtos.AvisoRequest;
import com.portal.residencial.contenido_service.dto.AvisoDtos.AvisoResponse;
import com.portal.residencial.contenido_service.dto.ConfiguracionPortalDto;
import com.portal.residencial.contenido_service.dto.ServicioDtos.ServicioRequest;
import com.portal.residencial.contenido_service.dto.ServicioDtos.ServicioResponse;
import com.portal.residencial.contenido_service.service.ContenidoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/* ADMINISTRACIÓN DEL CONTENIDO DEL PORTAL */
@RestController
@RequestMapping("/api/portal/admin")
@RequiredArgsConstructor
public class PortalAdminController {

    private final ContenidoService contenidoService;

    /* DATOS DEL RESIDENCIAL */

    @GetMapping("/configuracion")
    public ResponseEntity<ConfiguracionPortalDto> configuracion() {
        return ResponseEntity.ok(contenidoService.obtenerConfiguracion());
    }

    @PutMapping("/configuracion")
    public ResponseEntity<ConfiguracionPortalDto> actualizarConfiguracion(@Valid @RequestBody ConfiguracionPortalDto dto,
            @AuthenticationPrincipal String correo) {
        return ResponseEntity.ok(contenidoService.actualizarConfiguracion(dto, correo));
    }

    /* AVISOS */

    @GetMapping("/avisos")
    public ResponseEntity<List<AvisoResponse>> avisos() {
        return ResponseEntity.ok(contenidoService.listarAvisos());
    }

    @PostMapping("/avisos")
    public ResponseEntity<AvisoResponse> crearAviso(@Valid @RequestBody AvisoRequest request,
            @AuthenticationPrincipal String correo) {
        return ResponseEntity.ok(contenidoService.crearAviso(request, correo));
    }

    @PutMapping("/avisos/{id}")
    public ResponseEntity<AvisoResponse> actualizarAviso(@PathVariable Long id, @Valid @RequestBody AvisoRequest request,
            @AuthenticationPrincipal String correo) {
        return ResponseEntity.ok(contenidoService.actualizarAviso(id, request, correo));
    }

    @DeleteMapping("/avisos/{id}")
    public ResponseEntity<Void> eliminarAviso(@PathVariable Long id) {
        contenidoService.eliminarAviso(id);
        return ResponseEntity.noContent().build();
    }

    /* SERVICIOS */

    @GetMapping("/servicios")
    public ResponseEntity<List<ServicioResponse>> servicios() {
        return ResponseEntity.ok(contenidoService.listarServicios());
    }

    @PostMapping("/servicios")
    public ResponseEntity<ServicioResponse> crearServicio(@Valid @RequestBody ServicioRequest request,
            @AuthenticationPrincipal String correo) {
        return ResponseEntity.ok(contenidoService.crearServicio(request, correo));
    }

    @PutMapping("/servicios/{id}")
    public ResponseEntity<ServicioResponse> actualizarServicio(@PathVariable Long id,
            @Valid @RequestBody ServicioRequest request, @AuthenticationPrincipal String correo) {
        return ResponseEntity.ok(contenidoService.actualizarServicio(id, request, correo));
    }

    @DeleteMapping("/servicios/{id}")
    public ResponseEntity<Void> eliminarServicio(@PathVariable Long id) {
        contenidoService.eliminarServicio(id);
        return ResponseEntity.noContent().build();
    }
}
