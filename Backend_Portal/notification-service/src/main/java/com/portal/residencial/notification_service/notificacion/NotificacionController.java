package com.portal.residencial.notification_service.notificacion;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final EmailService emailService;
    private final WebSocketNotificacionService webSocketService;

    @PostMapping
    public ResponseEntity<Void> enviarNotificacion(@Valid @RequestBody NotificacionRequest request) {
        procesarAsync(request);
        return ResponseEntity.accepted().build();
    }

    @Async
    protected void procesarAsync(NotificacionRequest request) {
        webSocketService.enviarAlertaInterna(request);
        emailService.enviarCorreo(request);
    }
}
