package com.portal.residencial.contenido_service.model;

import java.time.LocalDate;
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

/* AVISO DEL PORTAL PÚBLICO */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "aviso")
public class Aviso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150)
    private String titulo;

    @Column(length = 2000)
    private String contenido;

    private String categoria; /* CATEGORÍA DEL AVISO */

    private boolean publicado;
    private LocalDate fechaPublicacion;
    private LocalDate fechaVencimiento;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String actualizadoPor;
}
