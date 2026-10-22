package com.portal.residencial.notification_service.correo;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

/* ENVÍO POR SMTP (GMAIL). FUNCIONA EN LOCAL; LA MAYORÍA DE NUBES BLOQUEAN ESTE PUERTO */
@Component
@ConditionalOnProperty(name = "correo.proveedor", havingValue = "smtp", matchIfMissing = true)
@RequiredArgsConstructor
public class ProveedorSmtp implements ProveedorCorreo {

    private final JavaMailSender mailSender;

    @Value("${correo.remitente}")
    private String remitente;

    @Value("${mail.remitente.nombre}")
    private String nombreRemitente;

    @Override
    public void enviar(String destinatario, String asunto, String html) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, "UTF-8");

            helper.setFrom(remitente, nombreRemitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(html, true);

            mailSender.send(mensaje);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("No se pudo enviar el correo por SMTP: " + e.getMessage(), e);
        }
    }
}
