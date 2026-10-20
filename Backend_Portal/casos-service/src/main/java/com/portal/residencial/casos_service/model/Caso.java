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

/* CASO REPORTADO POR UN RESIDENTE */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "caso")
public class Caso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String numeroCaso;

    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "id_tipo_incidente")
    private TipoIncidente tipoIncidente;

    @Column(length = 2000)
    private String descripcion;

    private String estado; /* CÓDIGO DEL ESTADO */

    private Long idResidente;
    private String correoResidente;
    private String nombreResidente;
    private String direccionResidente;

    private Long idAnalista;
    private String correoAnalista;
    private String nombreAnalista;

    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
    private LocalDateTime fechaCierre;
}
