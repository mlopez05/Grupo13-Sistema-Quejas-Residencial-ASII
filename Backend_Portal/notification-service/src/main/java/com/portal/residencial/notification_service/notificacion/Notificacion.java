package com.portal.residencial.notification_service.notificacion;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ALERTA INTERNA DEL PORTAL */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notificacion", indexes = @Index(name = "idx_notificacion_destinatario", columnList = "correo_destinatario, leida"))
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "correo_destinatario")
    private String correoDestinatario;

    private String tipo; /* TIPO DE NOTIFICACIÓN */

    private String titulo;

    @Column(length = 1000)
    private String mensaje;

    private Long idCaso;
    private String numeroCaso;

    @Column(name = "leida")
    private boolean leida;

    private LocalDateTime fecha;
    private LocalDateTime fechaLectura;
}
