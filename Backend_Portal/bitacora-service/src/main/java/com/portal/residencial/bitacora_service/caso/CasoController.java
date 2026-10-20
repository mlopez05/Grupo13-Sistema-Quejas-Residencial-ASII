package com.portal.residencial.bitacora_service.caso;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bitacora/caso")
@RequiredArgsConstructor
public class CasoController {

    private final CasoService casoService;

    @PostMapping("/registro")
    public ResponseEntity<Void> registro(@Valid @RequestBody CasoRequest request){
        casoService.guardarBitacora(request);
        return ResponseEntity.ok().build();
    }
}
