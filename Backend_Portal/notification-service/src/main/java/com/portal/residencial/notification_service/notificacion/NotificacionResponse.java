package com.portal.residencial.notification_service.notificacion;

import java.time.LocalDateTime;

public record NotificacionResponse(
        Long id,
        String tipo,
        String titulo,
        String mensaje,
        Long idCaso,
        String numeroCaso,
        boolean leida,
        LocalDateTime fecha
) {
    public static NotificacionResponse desde(Notificacion notificacion) {
        return new NotificacionResponse(
                notificacion.getId(),
                notificacion.getTipo(),
                notificacion.getTitulo(),
                notificacion.getMensaje(),
                notificacion.getIdCaso(),
                notificacion.getNumeroCaso(),
                notificacion.isLeida(),
                notificacion.getFecha());
    }
}
