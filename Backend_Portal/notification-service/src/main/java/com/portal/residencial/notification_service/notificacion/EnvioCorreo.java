package com.portal.residencial.notification_service.notificacion;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* REGISTRO DE CORREOS ENVIADOS */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "envio_correo")
public class EnvioCorreo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String correoDestinatario;
    private String tipo;            /* TIPO DE NOTIFICACIÓN */
    private String asunto;
    private Long idCaso;            /* CASO RELACIONADO */
    private String numeroCaso;

    private String estado;          /* ENVIADO O FALLIDO */

    @Column(length = 500)
    private String error;

    private LocalDateTime fecha;
}
