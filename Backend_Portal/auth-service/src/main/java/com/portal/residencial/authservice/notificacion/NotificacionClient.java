package com.portal.residencial.authservice.notificacion;

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

    @Value("${frontend.reset-password.url}")
    private String resetPasswordUrl;

    @Value("${frontend.login.url}")
    private String loginUrl;

    @Async
    public void enviarRecuperacionPassword(String correo, String token) {
        try {
            String enlace = resetPasswordUrl + "?token=" + token;

            NotificacionRequest request = new NotificacionRequest(
                    correo,
                    TipoNotificacion.RECUPERACION_PASSWORD,
                    Map.of("enlace", enlace));

            restTemplate.postForEntity(notificacionesUrl + "/notificaciones", request, Void.class);
        } catch (Exception e) {
            // No bloquear el flujo de solicitarRecuperacion si notificaciones-service falla
            log.error("No se pudo enviar el correo de recuperación a {}: {}", correo, e.getMessage());
        }
    }

    @Async
    public void enviarBienvenida(String correo) {
        try {
            NotificacionRequest request = new NotificacionRequest(
                    correo,
                    TipoNotificacion.BIENVENIDA_RESIDENTE,
                    Map.of("enlaceLogin", loginUrl));
            restTemplate.postForEntity(notificacionesUrl + "/notificaciones", request, Void.class);
        } catch (Exception e) {
            log.error("No se pudo enviar el correo de bienvenida a {}: {}", correo, e.getMessage());
        }
    }
}
