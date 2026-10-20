package com.portal.residencial.casos_service.dto;

import java.time.LocalDateTime;

import com.portal.residencial.casos_service.model.Documento;
import com.portal.residencial.casos_service.model.TipoArchivo;

public record DocumentoResponse(
    Long id,
    String nombreOriginal,
    String extension,
    String tipoContenido,
    String categoria,       /* DOCUMENTO, IMAGEN, AUDIO O VIDEO */
    long tamanoBytes,
    String nombreUsuario,
    String rolUsuario,
    LocalDateTime fechaCarga
) {
    public static DocumentoResponse desde(Documento documento) {
        TipoArchivo tipo = TipoArchivo.valueOf(documento.getTipoArchivo());

        return new DocumentoResponse(
                documento.getId(),
                documento.getNombreOriginal(),
                tipo.getExtension(),
                tipo.getTipoMime(),
                tipo.getCategoria().name(),
                documento.getTamanoBytes(),
                documento.getNombreUsuario(),
                documento.getRolUsuario(),
                documento.getFechaCarga());
    }
}
