package com.portal.residencial.bitacora_service.caso;

import java.time.LocalDate;
import java.time.LocalTime;

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

/* BITÁCORA DE CASOS */
@Entity
@Data
@Table(name = "a_caso")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Caso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_bitacora_caso;
    private Long id_caso;
    private String numero_caso;
    private String tipo_evento;
    private String estado_anterior;
    private String estado_nuevo;
    @Column(length = 2000)
    private String descripcion;
    private LocalDate fecha;
    private LocalTime hora;
    private String ip_usuario;
    private Long id_usuario;
    private String nombre_usuario;
    private String apellido_usuario;
    private String rol_usuario;
}
