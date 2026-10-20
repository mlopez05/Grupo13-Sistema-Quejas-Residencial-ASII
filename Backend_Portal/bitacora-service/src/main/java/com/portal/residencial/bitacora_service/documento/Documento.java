package com.portal.residencial.bitacora_service.documento;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* BITÁCORA DE CARGA Y DESCARGA DE ADJUNTOS */
@Entity
@Data
@Table(name = "a_documento")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Documento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_bitacora_documento;
    private Long id_documento;
    private Long id_caso;
    private String numero_caso;
    private String nombre_archivo;
    private String tipo_archivo;
    private Long tamano_bytes;
    private String tipo_evento;
    private LocalDate fecha;
    private LocalTime hora;
    private String ip_usuario;
    private Long id_usuario;
    private String nombre_usuario;
    private String apellido_usuario;
    private String rol_usuario;
}
