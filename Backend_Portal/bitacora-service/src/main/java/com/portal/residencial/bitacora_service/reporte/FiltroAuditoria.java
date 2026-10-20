package com.portal.residencial.bitacora_service.reporte;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

/* FILTROS DEL REPORTE DE AUDITORÍA */
public record FiltroAuditoria(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
        String usuario,
        String evento,
        Integer pagina,
        Integer tamano
) {
    public int paginaEfectiva() {
        return pagina == null || pagina < 0 ? 0 : pagina;
    }

    public int tamanoEfectivo() {
        return tamano == null || tamano < 1 ? 20 : Math.min(tamano, 100);
    }
}
