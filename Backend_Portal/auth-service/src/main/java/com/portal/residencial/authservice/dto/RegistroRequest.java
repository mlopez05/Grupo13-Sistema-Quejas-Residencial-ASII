package com.portal.residencial.authservice.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroRequest {
    @NotBlank
    private String nombre;
    @NotBlank
    private String apellido;
    @NotBlank @Email
    private String correo;
    @NotBlank
    private String clave;
    private LocalDate fecha_nacimiento;
    @NotBlank
    private String dpi;
    @NotBlank
    private String telefono;
    private Long id_rol;
    private String direccion;
    private String estado;
}
