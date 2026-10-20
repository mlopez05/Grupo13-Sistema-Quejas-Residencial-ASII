package com.portal.residencial.casos_service.model;

import java.time.LocalDateTime;

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

/* ARCHIVO ADJUNTO DE UN CASO */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "documento")
public class Documento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_caso")
    private Caso caso;

    /* MOVIMIENTO DEL CASO EN QUE SE SUBIÓ */
    @ManyToOne
    @JoinColumn(name = "id_seguimiento")
    private SeguimientoCaso seguimiento;

    private String nombreOriginal;

    private String tipoArchivo; /* TIPO DE ARCHIVO */

    private long tamanoBytes;
    private String rutaAlmacenamiento;

    private Long idUsuario;
    private String nombreUsuario;
    private String rolUsuario;

    private LocalDateTime fechaCarga;
}
