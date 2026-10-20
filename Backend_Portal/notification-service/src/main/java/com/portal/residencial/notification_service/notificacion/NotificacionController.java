package com.portal.residencial.notification_service.notificacion;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    /* RECIBE UNA NOTIFICACIÓN */
    @PostMapping
    public ResponseEntity<Void> enviarNotificacion(@Valid @RequestBody NotificacionRequest request) {
        notificacionService.procesar(request);
        return ResponseEntity.accepted().build();
    }

    /* ALERTAS DEL USUARIO */

    @GetMapping
    public ResponseEntity<List<NotificacionResponse>> recientes(@AuthenticationPrincipal String correo) {
        return ResponseEntity.ok(notificacionService.listarRecientes(correo));
    }

    @GetMapping("/no-leidas")
    public ResponseEntity<List<NotificacionResponse>> noLeidas(@AuthenticationPrincipal String correo) {
        return ResponseEntity.ok(notificacionService.listarNoLeidas(correo));
    }

    @PutMapping("/{id}/leida")
    public ResponseEntity<Void> marcarLeida(@PathVariable Long id, @AuthenticationPrincipal String correo) {
        notificacionService.marcarLeida(id, correo);
        return ResponseEntity.noContent().build();
    }

    /* MARCA COMO LEÍDAS LAS ALERTAS DEL CASO */
    @PutMapping("/caso/{idCaso}/leidas")
    public ResponseEntity<Void> marcarLeidasPorCaso(@PathVariable Long idCaso, @AuthenticationPrincipal String correo) {
        notificacionService.marcarLeidasPorCaso(idCaso, correo);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/leidas")
    public ResponseEntity<Void> marcarTodasLeidas(@AuthenticationPrincipal String correo) {
        notificacionService.marcarTodasLeidas(correo);
        return ResponseEntity.noContent().build();
    }
}
