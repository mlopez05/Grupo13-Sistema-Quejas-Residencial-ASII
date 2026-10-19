package com.portal.residencial.authservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.residencial.authservice.dto.ActualizarUsuarioRequest;
import com.portal.residencial.authservice.dto.RegistroRequest;
import com.portal.residencial.authservice.dto.RegistroResponse;
import com.portal.residencial.authservice.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/registro")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/residente")
    public ResponseEntity<RegistroResponse> guardar(@Valid @RequestBody RegistroRequest request){
        return ResponseEntity.ok(usuarioService.registroUsuarioResidente(request));
    }

    @PostMapping("/staff")
    public ResponseEntity<RegistroResponse> guardarAdmin(@Valid @RequestBody RegistroRequest request){
        return ResponseEntity.ok(usuarioService.registroUsuarioPortal(request));
    }

    @PutMapping ("/actualizar/{id}")
    public ResponseEntity<Void> actualizarUsuario(@Valid @PathVariable Long id, 
                                                    @RequestBody ActualizarUsuarioRequest request, HttpServletRequest httpRequest){
        usuarioService.actualizarUsuario(id, request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok().build();
    }

}
