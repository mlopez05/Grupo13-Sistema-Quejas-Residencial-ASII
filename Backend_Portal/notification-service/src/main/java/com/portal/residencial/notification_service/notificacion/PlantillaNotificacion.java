package com.portal.residencial.notification_service.notificacion;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class PlantillaNotificacion {

    public String asunto(TipoNotificacion tipo, Map<String, String> datos) {
        return switch (tipo) {
            case BIENVENIDA_RESIDENTE -> "¡Bienvenido al Portal Residencial!";
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
            case BIENVENIDA_RESIDENTE -> htmlBienvenida.formatted(datos.get("enlaceLogin"), datos.get("enlaceLogin"));
            case RECUPERACION_PASSWORD -> htmlRecuperarClave.formatted(datos.get("enlace"));
            case CASO_CREADO -> "Tu caso \"" + datos.get("titulo") + "\" fue registrado el " + datos.get("fecha") + ".";
            case CASO_ACTUALIZADO -> "Tu caso cambió de estado a \"" + datos.get("nuevoEstado") + "\".";
            case CASO_ASIGNADO ->
                "Se te asignó el caso \"" + datos.get("titulo") + "\", categoría " + datos.get("categoria") + ".";
            case CASO_AUTORIZADO_RECHAZADO ->
                "El Supervisor " + datos.get("decision") + " el caso \"" + datos.get("titulo") + "\".";
            case CASO_FINALIZADO -> "Tu caso \"" + datos.get("titulo") + "\" se cerró el " + datos.get("fecha") + ".";
            case TRASLADO_AUTORIZACION -> "El caso \"" + datos.get("titulo") + "\" requiere tu autorización.";
        };
    }

    private final String htmlRecuperarClave = """
            <html>
                <body style="font-family: Arial, sans-serif; color: #334155; line-height: 1.6;">
                    <h2 style="color: #0f172a;">Recuperación de contraseña</h2>

                    <p>Hemos recibido una solicitud para restablecer tu contraseña.</p>

                    <p>Haz clic en el siguiente botón para continuar:</p>

                    <p style="margin: 28px 0;">
                        <a href="%s"
                           style="display: inline-block;
                                  background-color: #2563eb;
                                  color: #ffffff;
                                  padding: 14px 24px;
                                  text-decoration: none;
                                  border-radius: 8px;
                                  font-weight: bold;">
                            Restablecer contraseña
                        </a>
                    </p>

                    <p>Este enlace vence en 30 minutos.</p>

                </body>
            </html>
            """;

    private String htmlBienvenida = """
            <div style="font-family: Arial, sans-serif; max-width:480px; margin:auto;">
              <h2>¡Bienvenido al Portal de Quejas Residencial!</h2>
              <p>Tu cuenta fue creada exitosamente. Ya puedes ingresar al portal.</p>
              <p style="text-align:center; margin: 24px 0;">
                <a href="%s" style="background-color:#2563eb; color:#ffffff; padding:12px 24px; text-decoration:none; border-radius:6px; font-weight:bold; display:inline-block;">
                  Ir al portal
                </a>
              </p>
            </div>
            """;
}
