package com.portal.residencial.bitacora_service.sesion;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/bitacora/sesion")
@RequiredArgsConstructor 
public class SesionController {

    private final SesionService sesionService;

    @PostMapping("/registrar")
    public ResponseEntity<Void> registrar(@Valid @RequestBody SesionRequest request){
        sesionService.registroSesion(request);
        return ResponseEntity.ok().build();
    }
}
