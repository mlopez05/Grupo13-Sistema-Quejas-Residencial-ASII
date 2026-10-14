package com.portal.residencial.notification_service.notificacion;

import java.time.LocalDateTime;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WebSocketNotificacionService {

    private final SimpMessagingTemplate messagingTemplate;
    private final PlantillaNotificacion plantilla;

    public void enviarAlertaInterna(NotificacionRequest request) {
        AlertaInterna alerta = new AlertaInterna(
                plantilla.asunto(request.tipo(), request.datos()),
                plantilla.cuerpo(request.tipo(), request.datos()),
                LocalDateTime.now()
        );
        messagingTemplate.convertAndSend(
                "/topic/notificaciones/" + request.correoDestinatario(),
                alerta
        );
    }

    public record AlertaInterna(String titulo, String mensaje, LocalDateTime fecha) {}

}
