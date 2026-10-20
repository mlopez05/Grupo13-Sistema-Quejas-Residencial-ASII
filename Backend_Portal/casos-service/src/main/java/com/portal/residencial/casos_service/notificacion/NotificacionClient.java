package com.portal.residencial.casos_service.notificacion;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacionClient {

    private final RestTemplate restTemplate;

    @Value("${notificaciones.service.url}")
    private String notificacionesUrl;

    /* ENVÍA LA ALERTA Y EL CORREO */
    @Async
    public void enviar(String correo, TipoNotificacion tipo, Map<String, String> datos) {
        if (correo == null || correo.isBlank()) {
            return;
        }

        try {
            restTemplate.postForEntity(notificacionesUrl + "/notificaciones",
                    new NotificacionRequest(correo, tipo, datos, true), Void.class);
        } catch (Exception e) {
            log.error("No se pudo enviar la notificación {} a {}: {}", tipo, correo, e.getMessage());
        }
    }
}
