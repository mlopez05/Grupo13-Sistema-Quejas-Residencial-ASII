package com.portal.residencial.casos_service.notificacion;

import java.util.Map;

/* DATOS DE LA NOTIFICACIÓN */
public record NotificacionRequest(
        String correoDestinatario,
        TipoNotificacion tipo,
        Map<String, String> datos,
        Boolean enviarCorreo    /* INDICA SI TAMBIÉN SE ENVÍA CORREO */
) {}
