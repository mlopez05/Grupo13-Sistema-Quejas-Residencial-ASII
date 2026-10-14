package com.portal.residencial.authservice.notificacion;

import java.util.Map;

public record NotificacionRequest(

        String correoDestinatario,
        TipoNotificacion tipo,
        Map<String, String> datos    
) {}
