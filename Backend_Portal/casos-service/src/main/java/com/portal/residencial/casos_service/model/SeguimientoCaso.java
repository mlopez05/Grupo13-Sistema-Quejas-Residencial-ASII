package com.portal.residencial.casos_service.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* HISTORIAL DEL CASO */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "seguimiento_caso")
public class SeguimientoCaso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_caso")
    private Caso caso;

    private String tipo; /* TIPO DE MOVIMIENTO */

    private String estadoAnterior; /* ESTADO ANTERIOR */

    private String estadoNuevo; /* ESTADO NUEVO */

    @Column(length = 1000)
    private String comentario;

    private Long idUsuario;
    private String nombreUsuario;
    private String rolUsuario;

    private LocalDateTime fecha;
}
