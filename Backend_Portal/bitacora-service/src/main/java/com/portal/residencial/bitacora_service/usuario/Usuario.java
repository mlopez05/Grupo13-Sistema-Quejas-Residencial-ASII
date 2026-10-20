package com.portal.residencial.bitacora_service.usuario;

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
@Table(name = "a_usuario")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_bitacora_usuario;
    private Long id_usuario_modificado;
    private String nombre_usuario_modificado;
    private String apellido_usuario_modificado;
    private String campo_modificado;
    private String valor_anterior;
    private String valor_nuevo;
    private LocalDate fecha;
    private LocalTime hora;
    private String tipo_evento;
    private String ip_usuario_modificador;
    private Long id_usuario_modificador;
    private String nombre_usuario_modificador;
    private String apellido_usuario_modificador;
}
