package com.portal.residencial.casos_service.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.residencial.casos_service.config.UsuarioAutenticado;
import com.portal.residencial.casos_service.service.DocumentoService;
import com.portal.residencial.casos_service.service.DocumentoService.ArchivoDescarga;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/* DESCARGA DE ADJUNTOS */
@RestController
@RequestMapping("/api/casos/{idCaso}/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;

    @GetMapping("/{idDocumento}/descarga")
    public ResponseEntity<Resource> descargar(@PathVariable Long idCaso, @PathVariable Long idDocumento,
            @AuthenticationPrincipal UsuarioAutenticado usuario, HttpServletRequest httpRequest) {
        ArchivoDescarga archivo = documentoService.descargar(idCaso, idDocumento, usuario,
                httpRequest.getRemoteAddr());
        return responder(archivo, ContentDisposition.attachment());
    }

    @GetMapping("/{idDocumento}/vista")
    public ResponseEntity<Resource> vistaPrevia(@PathVariable Long idCaso, @PathVariable Long idDocumento,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return responder(documentoService.vistaPrevia(idCaso, idDocumento, usuario), ContentDisposition.inline());
    }

    private ResponseEntity<Resource> responder(ArchivoDescarga archivo, ContentDisposition.Builder disposicion) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.tipoMime()))
                .contentLength(archivo.tamanoBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        disposicion.filename(archivo.nombre(), StandardCharsets.UTF_8).build().toString())
                .body(archivo.recurso());
    }
}
