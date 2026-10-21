package com.portal.residencial.notification_service.notificacion;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Component
public class PlantillaNotificacion {

    public String asunto(TipoNotificacion tipo, Map<String, String> datos) {
        return switch (tipo) {
            case BIENVENIDA_RESIDENTE -> "¡Bienvenido al Portal Residencial!";
            case RECUPERACION_PASSWORD -> "Recuperación de contraseña";
            case CASO_CREADO -> "Hemos recibido tu caso #" + datos.get("numeroCaso");
            case CASO_ACTUALIZADO -> "Actualización en tu caso #" + datos.get("numeroCaso");
            case CASO_ASIGNADO -> "Nuevo caso asignado: #" + datos.get("numeroCaso");
            /* AUTORIZÓ O RECHAZÓ */
            case CASO_AUTORIZADO_RECHAZADO -> "Decisión del Supervisor sobre el caso #" + datos.get("numeroCaso");
            case CASO_FINALIZADO -> "Tu caso #" + datos.get("numeroCaso") + " fue finalizado";
            case TRASLADO_AUTORIZACION -> "Caso #" + datos.get("numeroCaso") + " pendiente de autorización";
            case AVANCE_REGISTRADO -> "Nuevo avance en tu caso #" + datos.get("numeroCaso");
            case INFORMACION_AGREGADA -> "El residente agregó información al caso #" + datos.get("numeroCaso");
            case BIENVENIDA_PERSONAL -> "Tu cuenta en el Portal Residencial";
            case CASO_REASIGNADO -> "El caso #" + datos.get("numeroCaso") + " fue reasignado";
            case CAMBIO_ANALISTA -> "Nuevo analista en tu caso #" + datos.get("numeroCaso");
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
            case AVANCE_REGISTRADO -> "El analista registró un avance en tu caso \"" + datos.get("titulo") + "\".";
            case INFORMACION_AGREGADA ->
                "El residente agregó información al caso \"" + datos.get("titulo") + "\". Revisa el seguimiento.";
            case BIENVENIDA_PERSONAL -> "Se creó tu cuenta como " + datos.get("rol") + ".";
            case CASO_REASIGNADO ->
                "El caso \"" + datos.get("titulo") + "\" fue reasignado a otro analista y ya no está a tu cargo.";
            case CAMBIO_ANALISTA ->
                "Tu caso \"" + datos.get("titulo") + "\" ahora lo atiende " + datos.get("analista") + ".";
        };
    }

    /* CUERPO DEL CORREO */
    public String cuerpoCorreo(TipoNotificacion tipo, Map<String, String> datos) {
        if (tipo == TipoNotificacion.BIENVENIDA_RESIDENTE || tipo == TipoNotificacion.RECUPERACION_PASSWORD) {
            return cuerpo(tipo, datos);
        }

        if (tipo == TipoNotificacion.BIENVENIDA_PERSONAL) {
            return htmlBienvenidaPersonal.formatted(
                    escapar(datos.get("nombre")),
                    escapar(datos.get("rol")),
                    escapar(datos.get("correo")),
                    escapar(datos.get("claveTemporal")),
                    frontendUrl + "/login");
        }

        return htmlCaso.formatted(
                escapar(asunto(tipo, datos)),
                escapar(cuerpo(tipo, datos)),
                escapar(datos.get("numeroCaso")),
                escapar(datos.get("titulo")),
                escapar(datos.get("categoria")),
                frontendUrl + "/login");
    }

    private static String escapar(String texto) {
        return texto == null ? "" : HtmlUtils.htmlEscape(texto);
    }

    @Value("${frontend.url}")
    private String frontendUrl;

    private final String htmlCaso = """
            <div style="font-family: Arial, sans-serif; max-width: 520px; margin: auto; color: #334155; line-height: 1.6;">
              <h2 style="color: #0f172a; margin-bottom: 8px;">%s</h2>

              <p style="margin-top: 0;">%s</p>

              <div style="background-color: #f1f5f9; border-radius: 8px; padding: 14px 18px; margin: 20px 0;">
                <p style="margin: 0; font-size: 13px; color: #64748b;">Caso</p>
                <p style="margin: 2px 0 0; font-weight: bold; color: #0f172a;">#%s</p>
                <p style="margin: 2px 0 0;">%s · %s</p>
              </div>

              <p>Ingresa al portal para ver el detalle y dar seguimiento al caso.</p>

              <p style="text-align: center; margin: 24px 0;">
                <a href="%s"
                   style="background-color: #2563eb; color: #ffffff; padding: 12px 24px; text-decoration: none;
                          border-radius: 6px; font-weight: bold; display: inline-block;">
                  Ingresar al portal
                </a>
              </p>

              <p style="font-size: 12px; color: #94a3b8;">
                Este es un mensaje automático del Portal Residencial. No respondas a este correo.
              </p>
            </div>
            """;

    /* BIENVENIDA DEL PERSONAL CON SU CONTRASEÑA TEMPORAL */
    private final String htmlBienvenidaPersonal = """
            <div style="font-family: Arial, sans-serif; max-width: 520px; margin: auto; color: #334155; line-height: 1.6;">
              <h2 style="color: #0f172a; margin-bottom: 8px;">¡Bienvenido, %s!</h2>

              <p style="margin-top: 0;">El administrador creó tu cuenta en el Portal Residencial con el rol de <strong>%s</strong>.</p>

              <div style="background-color: #f1f5f9; border-radius: 8px; padding: 14px 18px; margin: 20px 0;">
                <p style="margin: 0; font-size: 13px; color: #64748b;">Usuario</p>
                <p style="margin: 2px 0 10px; font-weight: bold; color: #0f172a;">%s</p>
                <p style="margin: 0; font-size: 13px; color: #64748b;">Contraseña temporal</p>
                <p style="margin: 2px 0 0; font-weight: bold; color: #0f172a; font-family: monospace; font-size: 16px;">%s</p>
              </div>

              <p>Al ingresar por primera vez el portal te pedirá cambiar esta contraseña por una propia.</p>

              <p style="text-align: center; margin: 24px 0;">
                <a href="%s"
                   style="background-color: #2563eb; color: #ffffff; padding: 12px 24px; text-decoration: none;
                          border-radius: 6px; font-weight: bold; display: inline-block;">
                  Ingresar al portal
                </a>
              </p>

              <p style="font-size: 12px; color: #94a3b8;">
                Este es un mensaje automático del Portal Residencial. No respondas a este correo.
              </p>
            </div>
            """;

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
