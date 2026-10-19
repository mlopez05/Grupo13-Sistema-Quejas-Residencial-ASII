package com.portal.residencial.bitacora_service.sesion;

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

@Entity 
@Data 
@Table (name = "a_sesion")
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class Sesion {
    @Id 
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id_sesion;
    private Long id_usuario;
    private LocalDate fecha;
    private LocalTime hora;
    private String ip_usuario;
    private String correo_usuario;
    private String nombre_usuario;
    private String apellido_usuario;
    private String rol_usuario;
    private String tipo_evento;
}
