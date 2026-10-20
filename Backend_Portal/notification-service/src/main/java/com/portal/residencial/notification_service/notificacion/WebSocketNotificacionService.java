package com.portal.residencial.notification_service.notificacion;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WebSocketNotificacionService {

    public static final String DESTINO = "/queue/notificaciones";

    private final SimpMessagingTemplate messagingTemplate;

    /* ENVÍA LA ALERTA AL USUARIO */
    public void enviarAlertaInterna(String correo, NotificacionResponse notificacion) {
        messagingTemplate.convertAndSendToUser(correo, DESTINO, notificacion);
    }
}
