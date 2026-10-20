package com.portal.residencial.notification_service.notificacion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private static final int LONGITUD_MAXIMA_MENSAJE = 1000;
    private static final int LONGITUD_MAXIMA_ERROR = 500;

    private final NotificacionRepository notificacionRepository;
    private final EnvioCorreoRepository envioCorreoRepository;
    private final WebSocketNotificacionService webSocketService;
    private final EmailService emailService;
    private final PlantillaNotificacion plantilla;

    /* GUARDA LA ALERTA Y ENVÍA EL CORREO */
    @Async
    public void procesar(NotificacionRequest request) {
        Long idCaso = idCaso(request.datos());

        if (idCaso != null) {
            try {
                Notificacion notificacion = notificacionRepository.save(Notificacion.builder()
                        .correoDestinatario(request.correoDestinatario())
                        .tipo(request.tipo().name())
                        .titulo(plantilla.asunto(request.tipo(), request.datos()))
                        .mensaje(recortar(plantilla.cuerpo(request.tipo(), request.datos()), LONGITUD_MAXIMA_MENSAJE))
                        .idCaso(idCaso)
                        .numeroCaso(request.datos().get("numeroCaso"))
                        .leida(false)
                        .fecha(LocalDateTime.now())
                        .build());

                webSocketService.enviarAlertaInterna(request.correoDestinatario(),
                        NotificacionResponse.desde(notificacion));
            } catch (Exception e) {
                log.error("No se pudo registrar la alerta interna {} para {}: {}",
                        request.tipo(), request.correoDestinatario(), e.getMessage());
            }
        }

        if (!Boolean.FALSE.equals(request.enviarCorreo())) {
            enviarCorreo(request, idCaso);
        }
    }

    /* ENVÍA EL CORREO Y GUARDA EL RESULTADO */
    private void enviarCorreo(NotificacionRequest request, Long idCaso) {
        String estado = "ENVIADO";
        String error = null;

        try {
            emailService.enviarCorreo(request);
        } catch (Exception e) {
            estado = "FALLIDO";
            Throwable causa = e.getCause() != null ? e.getCause() : e;
            error = recortar(causa.getMessage(), LONGITUD_MAXIMA_ERROR);
            log.error("No se pudo enviar el correo {} a {}: {}",
                    request.tipo(), request.correoDestinatario(), causa.getMessage());
        }

        try {
            envioCorreoRepository.save(EnvioCorreo.builder()
                    .correoDestinatario(request.correoDestinatario())
                    .tipo(request.tipo().name())
                    .asunto(plantilla.asunto(request.tipo(), request.datos()))
                    .idCaso(idCaso)
                    .numeroCaso(request.datos() == null ? null : request.datos().get("numeroCaso"))
                    .estado(estado)
                    .error(error)
                    .fecha(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("No se pudo registrar el envío del correo {} a {}: {}",
                    request.tipo(), request.correoDestinatario(), e.getMessage());
        }
    }

    public List<NotificacionResponse> listarRecientes(String correo) {
        return notificacionRepository.findTop30ByCorreoDestinatarioOrderByFechaDescIdDesc(correo).stream()
                .map(NotificacionResponse::desde)
                .toList();
    }

    public List<NotificacionResponse> listarNoLeidas(String correo) {
        return notificacionRepository.findByCorreoDestinatarioAndLeidaFalseOrderByFechaDescIdDesc(correo).stream()
                .map(NotificacionResponse::desde)
                .toList();
    }

    public void marcarLeida(Long id, String correo) {
        notificacionRepository.marcarLeida(id, correo, LocalDateTime.now());
    }

    public void marcarLeidasPorCaso(Long idCaso, String correo) {
        notificacionRepository.marcarLeidasPorCaso(idCaso, correo, LocalDateTime.now());
    }

    public void marcarTodasLeidas(String correo) {
        notificacionRepository.marcarTodasLeidas(correo, LocalDateTime.now());
    }

    private static Long idCaso(Map<String, String> datos) {
        if (datos == null || datos.get("idCaso") == null) {
            return null;
        }

        try {
            return Long.valueOf(datos.get("idCaso"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String recortar(String texto, int maximo) {
        return texto != null && texto.length() > maximo
                ? texto.substring(0, maximo)
                : texto;
    }
}
