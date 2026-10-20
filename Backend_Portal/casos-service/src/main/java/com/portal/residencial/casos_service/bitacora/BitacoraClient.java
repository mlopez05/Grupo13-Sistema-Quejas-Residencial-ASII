package com.portal.residencial.casos_service.bitacora;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BitacoraClient {

    private final RestTemplate restTemplate;

    @Value("${bitacora.service.url}")
    private String bitacoraUrl;
    
    @Async
    public void registrar(BitacoraCasoRequest request) {
        try {
            restTemplate.postForEntity(bitacoraUrl + "/bitacora/caso/registro", request, Void.class);
        } catch (Exception e) {
            log.error("No se pudo registrar el evento {} del caso {} en bitácora: {}",
                    request.tipoEvento(), request.numeroCaso(), e.getMessage());
        }
    }

    /* REGISTRA LA CARGA O DESCARGA DE ADJUNTOS */
    @Async
    public void registrarDocumento(BitacoraDocumentoRequest request) {
        try {
            restTemplate.postForEntity(bitacoraUrl + "/bitacora/documento/registro", request, Void.class);
        } catch (Exception e) {
            log.error("No se pudo registrar la {} del documento {} del caso {} en bitácora: {}",
                    request.tipoEvento(), request.nombreArchivo(), request.numeroCaso(), e.getMessage());
        }
    }
}
