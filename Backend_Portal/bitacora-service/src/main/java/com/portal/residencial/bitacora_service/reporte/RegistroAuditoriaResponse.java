package com.portal.residencial.bitacora_service.reporte;

import java.time.LocalDate;
import java.time.LocalTime;

/* FILA DEL REPORTE DE AUDITORÍA */
public record RegistroAuditoriaResponse(
        Long id,
        LocalDate fecha,
        LocalTime hora,
        String usuario,
        String rol,
        String evento,
        String detalle,
        String ip
) {}
