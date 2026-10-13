package com.portal.residencial.authservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.residencial.authservice.dto.CorreoRequest;
import com.portal.residencial.authservice.dto.RegistroRequest;
import com.portal.residencial.authservice.dto.RegistroResponse;
import com.portal.residencial.authservice.service.UsuarioService;

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

    @PostMapping("/confirma-correo")
    public ResponseEntity<Boolean> existeCorreo(@Valid @RequestBody CorreoRequest correo){
        boolean existe = usuarioService.existeCorreo(correo);

        return  ResponseEntity.ok(existe);    
    }
}
