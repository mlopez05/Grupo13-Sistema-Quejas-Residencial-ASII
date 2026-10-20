package com.portal.residencial.casos_service.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/* FORMATOS Y TAMAÑO MÁXIMO PERMITIDOS */
public enum TipoArchivo {
    PDF("pdf", CategoriaArchivo.DOCUMENTO, "application/pdf", 10),
    DOCX("docx", CategoriaArchivo.DOCUMENTO,
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", 10),
    JPG("jpg", CategoriaArchivo.IMAGEN, "image/jpeg", 8),
    JPEG("jpeg", CategoriaArchivo.IMAGEN, "image/jpeg", 8),
    PNG("png", CategoriaArchivo.IMAGEN, "image/png", 8),
    MP3("mp3", CategoriaArchivo.AUDIO, "audio/mpeg", 15),
    M4A("m4a", CategoriaArchivo.AUDIO, "audio/mp4", 15),
    MP4("mp4", CategoriaArchivo.VIDEO, "video/mp4", 50);

    /* TAMAÑO MÁXIMO DE ADJUNTOS POR CASO */
    public static final long MAXIMO_TOTAL_POR_CASO = 150L * 1024 * 1024;

    private final String extension;
    private final CategoriaArchivo categoria;
    private final String tipoMime;
    private final long tamanoMaximoBytes;

    TipoArchivo(String extension, CategoriaArchivo categoria, String tipoMime, int tamanoMaximoMb) {
        this.extension = extension;
        this.categoria = categoria;
        this.tipoMime = tipoMime;
        this.tamanoMaximoBytes = tamanoMaximoMb * 1024L * 1024L;
    }

    public static Optional<TipoArchivo> porNombreArchivo(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return Optional.empty();
        }

        String ext = nombreArchivo.substring(nombreArchivo.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);

        return Arrays.stream(values()).filter(t -> t.extension.equals(ext)).findFirst();
    }

    public String getExtension() {
        return extension;
    }

    public CategoriaArchivo getCategoria() {
        return categoria;
    }

    public String getTipoMime() {
        return tipoMime;
    }

    public long getTamanoMaximoBytes() {
        return tamanoMaximoBytes;
    }

    public long getTamanoMaximoMb() {
        return tamanoMaximoBytes / (1024 * 1024);
    }
}
