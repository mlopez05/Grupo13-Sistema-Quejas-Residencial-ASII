package com.portal.residencial.casos_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.residencial.casos_service.dto.catalogo.CategoriaAdminResponse;
import com.portal.residencial.casos_service.dto.catalogo.CategoriaRequest;
import com.portal.residencial.casos_service.dto.catalogo.TipoIncidenteRequest;
import com.portal.residencial.casos_service.service.CatalogoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/* CATALOGO DE CATEGORIAS Y TIPOS DE INCIDENTE (SOLO ADMINISTRADOR) */
@RestController
@RequestMapping("/api/casos/catalogo/admin")
@PreAuthorize("hasAuthority('Administrador')")
@RequiredArgsConstructor
public class CatalogoAdminController {

    private final CatalogoService catalogoService;

    @GetMapping
    public ResponseEntity<List<CategoriaAdminResponse>> listar() {
        return ResponseEntity.ok(catalogoService.listarCatalogoAdmin());
    }

    @PostMapping("/categorias")
    public ResponseEntity<List<CategoriaAdminResponse>> crearCategoria(@Valid @RequestBody CategoriaRequest request) {
        catalogoService.crearCategoria(request);
        return ResponseEntity.ok(catalogoService.listarCatalogoAdmin());
    }

    @PutMapping("/categorias/{id}")
    public ResponseEntity<List<CategoriaAdminResponse>> actualizarCategoria(@PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request) {
        catalogoService.actualizarCategoria(id, request);
        return ResponseEntity.ok(catalogoService.listarCatalogoAdmin());
    }

    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<List<CategoriaAdminResponse>> eliminarCategoria(@PathVariable Long id) {
        catalogoService.eliminarCategoria(id);
        return ResponseEntity.ok(catalogoService.listarCatalogoAdmin());
    }

    @PostMapping("/tipos")
    public ResponseEntity<List<CategoriaAdminResponse>> crearTipo(@Valid @RequestBody TipoIncidenteRequest request) {
        catalogoService.crearTipo(request);
        return ResponseEntity.ok(catalogoService.listarCatalogoAdmin());
    }

    @PutMapping("/tipos/{id}")
    public ResponseEntity<List<CategoriaAdminResponse>> actualizarTipo(@PathVariable Long id,
            @Valid @RequestBody TipoIncidenteRequest request) {
        catalogoService.actualizarTipo(id, request);
        return ResponseEntity.ok(catalogoService.listarCatalogoAdmin());
    }

    @DeleteMapping("/tipos/{id}")
    public ResponseEntity<List<CategoriaAdminResponse>> eliminarTipo(@PathVariable Long id) {
        catalogoService.eliminarTipo(id);
        return ResponseEntity.ok(catalogoService.listarCatalogoAdmin());
    }
}
