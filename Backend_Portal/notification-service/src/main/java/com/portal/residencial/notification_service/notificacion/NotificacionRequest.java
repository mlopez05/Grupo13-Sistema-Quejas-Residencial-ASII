package com.portal.residencial.notification_service.notificacion;

import java.util.Map;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/* DATOS DE LA NOTIFICACIÓN */
public record NotificacionRequest(

    @NotBlank @Email String correoDestinatario,
    @NotNull TipoNotificacion tipo,
    Map<String, String> datos,
    Boolean enviarCorreo
) {}
