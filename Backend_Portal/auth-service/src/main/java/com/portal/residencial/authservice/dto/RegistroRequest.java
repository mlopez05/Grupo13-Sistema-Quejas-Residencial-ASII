package com.portal.residencial.authservice.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroRequest {
    private String nombre;
    private String apellido;
    private String correo;
    private String clave;
    private LocalDate fecha_nacimiento;
    private String dpi;
    private String telefono;
    private Long id_rol;
    private String direccion;
}
