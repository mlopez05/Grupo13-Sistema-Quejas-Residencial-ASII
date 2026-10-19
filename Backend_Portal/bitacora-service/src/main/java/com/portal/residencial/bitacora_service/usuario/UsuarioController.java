package com.portal.residencial.bitacora_service.usuario;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping ("/bitacora/usuario")
@RequiredArgsConstructor 
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping ("/registro")
    public ResponseEntity<Void> registro(@Valid @RequestBody UsuarioRequest request){
        usuarioService.guadarBitacora(request);
        return ResponseEntity.ok().build();
    }
}
