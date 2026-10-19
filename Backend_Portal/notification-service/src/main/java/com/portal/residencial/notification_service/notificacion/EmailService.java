package com.portal.residencial.notification_service.notificacion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final PlantillaNotificacion plantilla;

    @Value("${spring.mail.username}")
    private String remitente;

    public void enviarCorreo(NotificacionRequest request) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(request.correoDestinatario());
            helper.setSubject(plantilla.asunto(request.tipo(), request.datos()));
            helper.setText(plantilla.cuerpo(request.tipo(), request.datos()), true);

            mailSender.send(mensaje);
        } catch (MessagingException e) {
            log.error("Error enviando correo a {}: {}", request.correoDestinatario(), e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo", e);
        }
    }   
}
