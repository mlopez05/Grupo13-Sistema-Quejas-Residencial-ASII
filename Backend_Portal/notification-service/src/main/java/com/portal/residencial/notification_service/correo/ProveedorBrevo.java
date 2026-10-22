package com.portal.residencial.notification_service.correo;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/* ENVÍO POR LA API HTTPS DE BREVO. PARA LA NUBE, DONDE EL SMTP SUELE ESTAR BLOQUEADO */
@Component
@ConditionalOnProperty(name = "correo.proveedor", havingValue = "brevo")
@Slf4j
public class ProveedorBrevo implements ProveedorCorreo {

    private final RestClient restClient = RestClient.create();

    @Value("${brevo.url}")
    private String url;

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${correo.remitente}")
    private String remitente;

    @Value("${mail.remitente.nombre}")
    private String nombreRemitente;

    /* DATOS QUE PIDE LA API DE BREVO */
    record Contacto(String name, String email) {}
    record Mensaje(Contacto sender, List<Contacto> to, String subject, String htmlContent) {}

    @PostConstruct
    void revisarConfiguracion() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("CORREO_PROVEEDOR=brevo pero BREVO_API_KEY está vacía: los correos no se enviarán.");
        }
        if (remitente == null || remitente.isBlank()) {
            log.warn("CORREO_REMITENTE está vacío: Brevo rechazará los correos.");
        }
    }

    @Override
    public void enviar(String destinatario, String asunto, String html) {
        Mensaje mensaje = new Mensaje(
                new Contacto(nombreRemitente, remitente),
                List.of(new Contacto(null, destinatario)),
                asunto,
                html);

        try {
            restClient.post()
                    .uri(url)
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(mensaje)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new IllegalStateException("Brevo respondió " + e.getStatusCode().value() + ": "
                    + e.getResponseBodyAsString(), e);
        }
    }
}
