package com.portal.residencial.notification_service.correo;

/* FORMA DE ENVIAR UN CORREO: SMTP O BREVO, SEGÚN correo.proveedor */
public interface ProveedorCorreo {

    /* LANZA UNA EXCEPCIÓN SI EL CORREO NO SE PUDO ENVIAR */
    void enviar(String destinatario, String asunto, String html);
}
