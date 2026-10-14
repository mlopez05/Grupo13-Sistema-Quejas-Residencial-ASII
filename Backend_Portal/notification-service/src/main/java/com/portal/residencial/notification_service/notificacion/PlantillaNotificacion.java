package com.portal.residencial.notification_service.notificacion;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class PlantillaNotificacion {

    public String asunto(TipoNotificacion tipo, Map<String, String> datos) {
        return switch (tipo) {
            case BIENVENIDA_RESIDENTE -> "¡Bienvenido al Portal de Quejas Residencial!";
            case RECUPERACION_PASSWORD -> "Recuperación de contraseña";
            case CASO_CREADO -> "Hemos recibido tu caso #" + datos.get("numeroCaso");
            case CASO_ACTUALIZADO -> "Actualización en tu caso #" + datos.get("numeroCaso");
            case CASO_ASIGNADO -> "Nuevo caso asignado: #" + datos.get("numeroCaso");
            case CASO_AUTORIZADO_RECHAZADO -> "Caso #" + datos.get("numeroCaso") + " " + datos.get("decision");
            case CASO_FINALIZADO -> "Tu caso #" + datos.get("numeroCaso") + " fue finalizado";
            case TRASLADO_AUTORIZACION -> "Caso #" + datos.get("numeroCaso") + " pendiente de autorización";
        };
    }

    public String cuerpo(TipoNotificacion tipo, Map<String, String> datos) {
        return switch (tipo) {
            case BIENVENIDA_RESIDENTE -> "Tu cuenta fue creada exitosamente. Ya puedes ingresar al portal y registrar tus casos.";
            case RECUPERACION_PASSWORD -> "Haz clic en el siguiente enlace para restablecer tu contraseña: " + datos.get("enlace")
                    + " (este enlace vence en 30 minutos)";
            case CASO_CREADO -> "Tu caso \"" + datos.get("titulo") + "\" fue registrado el " + datos.get("fecha") + ".";
            case CASO_ACTUALIZADO -> "Tu caso cambió de estado a \"" + datos.get("nuevoEstado") + "\".";
            case CASO_ASIGNADO -> "Se te asignó el caso \"" + datos.get("titulo") + "\", categoría " + datos.get("categoria") + ".";
            case CASO_AUTORIZADO_RECHAZADO -> "El Supervisor " + datos.get("decision") + " el caso \"" + datos.get("titulo") + "\".";
            case CASO_FINALIZADO -> "Tu caso \"" + datos.get("titulo") + "\" se cerró el " + datos.get("fecha") + ".";
            case TRASLADO_AUTORIZACION -> "El caso \"" + datos.get("titulo") + "\" requiere tu autorización.";
        };
    }
}
