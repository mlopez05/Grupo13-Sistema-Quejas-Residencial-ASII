package com.portal.residencial.casos_service.almacenamiento;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/* GUARDA LOS ADJUNTOS EN UNA CARPETA DEL SERVIDOR */
@Component
@Slf4j
public class AlmacenamientoLocal implements AlmacenamientoArchivos {

    private final Path raiz;

    public AlmacenamientoLocal(@Value("${archivos.directorio}") String directorio) throws IOException {
        this.raiz = Paths.get(directorio).toAbsolutePath().normalize();
        Files.createDirectories(this.raiz);
        log.info("Adjuntos almacenados en {}", this.raiz);
    }

    @Override
    public String guardar(InputStream contenido, String carpeta, String extension) throws IOException {
        String ruta = carpeta + "/" + UUID.randomUUID() + "." + extension;
        Path destino = resolver(ruta);

        Files.createDirectories(destino.getParent());
        Files.copy(contenido, destino, StandardCopyOption.REPLACE_EXISTING);

        return ruta;
    }

    @Override
    public Resource cargar(String ruta) throws IOException {
        Path archivo = resolver(ruta);

        if (!Files.isReadable(archivo)) {
            throw new IOException("El archivo no existe en el almacenamiento: " + ruta);
        }

        return new FileSystemResource(archivo);
    }

    @Override
    public void eliminar(String ruta) {
        try {
            Files.deleteIfExists(resolver(ruta));
        } catch (IOException e) {
            log.warn("No se pudo eliminar el archivo {}: {}", ruta, e.getMessage());
        }
    }

    /* EVITA SALIR DE LA CARPETA DE ADJUNTOS */
    private Path resolver(String ruta) throws IOException {
        Path archivo = raiz.resolve(ruta).normalize();

        if (!archivo.startsWith(raiz)) {
            throw new IOException("Ruta de archivo no permitida.");
        }

        return archivo;
    }
}
