package com.portal.residencial.bitacora_service.documento;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bitacora/documento")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;

    @PostMapping("/registro")
    public ResponseEntity<Void> registro(@Valid @RequestBody DocumentoRequest request){
        documentoService.guardarBitacora(request);
        return ResponseEntity.ok().build();
    }
}
