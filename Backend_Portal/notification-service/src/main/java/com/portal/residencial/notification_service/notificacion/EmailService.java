package com.portal.residencial.notification_service.notificacion;

import org.springframework.stereotype.Service;

import com.portal.residencial.notification_service.correo.ProveedorCorreo;

import lombok.RequiredArgsConstructor;

/* ARMA EL CORREO CON SU PLANTILLA Y LO ENVÍA POR EL PROVEEDOR CONFIGURADO (SMTP O BREVO) */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final ProveedorCorreo proveedor;
    private final PlantillaNotificacion plantilla;

    public void enviarCorreo(NotificacionRequest request) {
        proveedor.enviar(
                request.correoDestinatario(),
                plantilla.asunto(request.tipo(), request.datos()),
                plantilla.cuerpoCorreo(request.tipo(), request.datos()));
    }
}
