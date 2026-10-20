package com.portal.residencial.contenido_service.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* DATOS DEL RESIDENCIAL */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "configuracion_portal")
public class ConfiguracionPortal {

    public static final long ID_UNICO = 1L;

    @Id
    private Long id;

    /* IDENTIDAD DEL PORTAL */
    private String nombreResidencial;
    private String lema;

    /* DATOS DE LA ADMINISTRACIÓN */
    private String razonSocial;
    private String nit;
    @Column(length = 250)
    private String direccion;
    private String telefono;
    private String correoContacto;
    private String horarioAtencion;

    /* TEXTOS DEL PORTAL */
    private String tituloBienvenida;
    @Column(length = 500)
    private String mensajeBienvenida;
    @Column(length = 1500)
    private String descripcionComunidad;
    @Column(length = 1000)
    private String mision;
    @Column(length = 1000)
    private String vision;

    private LocalDateTime fechaActualizacion;
    private String actualizadoPor;
}
