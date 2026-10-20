package com.portal.residencial.casos_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.portal.residencial.casos_service.config.UsuarioAutenticado;
import com.portal.residencial.casos_service.dto.AnalistaResponse;
import com.portal.residencial.casos_service.dto.AsignarAnalistaRequest;
import com.portal.residencial.casos_service.dto.AutorizarRequest;
import com.portal.residencial.casos_service.dto.CasoDetalleResponse;
import com.portal.residencial.casos_service.dto.CasoResponse;
import com.portal.residencial.casos_service.dto.CategoriaResponse;
import com.portal.residencial.casos_service.dto.ComentarioRequest;
import com.portal.residencial.casos_service.dto.RegistrarCasoRequest;
import com.portal.residencial.casos_service.service.CasoService;
import com.portal.residencial.casos_service.service.CatalogoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/casos")
@RequiredArgsConstructor
public class CasoController {

    private final CasoService casoService;
    private final CatalogoService catalogoService;

    /* CATÁLOGO */

    @GetMapping("/catalogo")
    public ResponseEntity<List<CategoriaResponse>> catalogo() {
        return ResponseEntity.ok(catalogoService.listarCatalogo());
    }

    /* RESIDENTE */

    /* REGISTRA EL CASO CON SUS ADJUNTOS */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('Residente')")
    public ResponseEntity<CasoResponse> registrar(@Valid @ModelAttribute RegistrarCasoRequest request,
            @RequestParam(value = "archivos", required = false) List<MultipartFile> archivos,
            @AuthenticationPrincipal UsuarioAutenticado usuario, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(casoService.registrarCaso(request, adjuntos(archivos), usuario, httpRequest.getRemoteAddr()));
    }

    /* EL RESIDENTE AGREGA INFORMACIÓN A SU CASO */
    @PostMapping(value = "/{id}/informacion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('Residente')")
    public ResponseEntity<CasoDetalleResponse> agregarInformacion(@PathVariable Long id,
            @Valid @ModelAttribute ComentarioRequest request,
            @RequestParam(value = "archivos", required = false) List<MultipartFile> archivos,
            @AuthenticationPrincipal UsuarioAutenticado usuario, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(casoService.agregarInformacion(id, request.comentario(), adjuntos(archivos),
                usuario, httpRequest.getRemoteAddr()));
    }

    @GetMapping("/mis-casos")
    @PreAuthorize("hasAuthority('Residente')")
    public ResponseEntity<List<CasoResponse>> misCasos(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(casoService.listarMisCasos(usuario));
    }

    /* PERSONAL */

    @GetMapping("/bandeja")
    @PreAuthorize("hasAnyAuthority('Analista', 'Supervisor', 'Administrador')")
    public ResponseEntity<List<CasoResponse>> bandeja(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(casoService.listarBandeja(usuario));
    }

    /* LISTA DE ANALISTAS */
    @GetMapping("/analistas")
    @PreAuthorize("hasAnyAuthority('Supervisor', 'Administrador')")
    public ResponseEntity<List<AnalistaResponse>> analistas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(casoService.listarAnalistas(usuario));
    }

    /* DETALLE DEL CASO */

    @GetMapping("/{id}")
    public ResponseEntity<CasoDetalleResponse> detalle(@PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(casoService.obtenerDetalle(id, usuario));
    }

    /* ANALISTA: ANALIZAR Y SOLICITAR AUTORIZACIÓN */

    @PutMapping("/{id}/iniciar-analisis")
    @PreAuthorize("hasAuthority('Analista')")
    public ResponseEntity<CasoDetalleResponse> iniciarAnalisis(@PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(casoService.iniciarAnalisis(id, usuario, httpRequest.getRemoteAddr()));
    }

    /* REGISTRA UN AVANCE CON ADJUNTOS */
    @PostMapping(value = "/{id}/avances", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('Analista')")
    public ResponseEntity<CasoDetalleResponse> registrarAvance(@PathVariable Long id,
            @Valid @ModelAttribute ComentarioRequest request,
            @RequestParam(value = "archivos", required = false) List<MultipartFile> archivos,
            @AuthenticationPrincipal UsuarioAutenticado usuario, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(casoService.registrarAvance(id, request.comentario(), adjuntos(archivos),
                usuario, httpRequest.getRemoteAddr()));
    }

    @PutMapping(value = "/{id}/solicitar-autorizacion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('Analista')")
    public ResponseEntity<CasoDetalleResponse> solicitarAutorizacion(@PathVariable Long id,
            @Valid @ModelAttribute ComentarioRequest request,
            @RequestParam(value = "archivos", required = false) List<MultipartFile> archivos,
            @AuthenticationPrincipal UsuarioAutenticado usuario, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(casoService.solicitarAutorizacion(id, request.comentario(), adjuntos(archivos),
                usuario, httpRequest.getRemoteAddr()));
    }

    /* SUPERVISOR: AUTORIZAR O RECHAZAR */

    @PutMapping("/{id}/autorizar")
    @PreAuthorize("hasAuthority('Supervisor')")
    public ResponseEntity<CasoDetalleResponse> autorizar(@PathVariable Long id,
            @Valid @RequestBody(required = false) AutorizarRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario, HttpServletRequest httpRequest) {
        String comentario = request == null ? null : request.comentario();
        return ResponseEntity.ok(casoService.autorizar(id, comentario, usuario, httpRequest.getRemoteAddr()));
    }

    @PutMapping("/{id}/rechazar")
    @PreAuthorize("hasAuthority('Supervisor')")
    public ResponseEntity<CasoDetalleResponse> rechazar(@PathVariable Long id,
            @Valid @RequestBody ComentarioRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(casoService.rechazar(id, request.comentario(), usuario,
                httpRequest.getRemoteAddr()));
    }

    @PutMapping("/{id}/asignar")
    @PreAuthorize("hasAuthority('Supervisor')")
    public ResponseEntity<CasoDetalleResponse> asignar(@PathVariable Long id,
            @Valid @RequestBody AsignarAnalistaRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(casoService.reasignar(id, request.idAnalista(), usuario,
                httpRequest.getRemoteAddr()));
    }

    /* ANALISTA: CERRAR CASO */

    @PutMapping(value = "/{id}/cerrar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('Analista')")
    public ResponseEntity<CasoDetalleResponse> cerrar(@PathVariable Long id,
            @Valid @ModelAttribute ComentarioRequest request,
            @RequestParam(value = "archivos", required = false) List<MultipartFile> archivos,
            @AuthenticationPrincipal UsuarioAutenticado usuario, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(casoService.cerrar(id, request.comentario(), adjuntos(archivos),
                usuario, httpRequest.getRemoteAddr()));
    }

    /* QUITA LOS ARCHIVOS VACÍOS */
    private static List<MultipartFile> adjuntos(List<MultipartFile> archivos) {
        if (archivos == null) {
            return List.of();
        }

        return archivos.stream()
                .filter(archivo -> archivo.getOriginalFilename() != null && !archivo.getOriginalFilename().isBlank())
                .toList();
    }
}
