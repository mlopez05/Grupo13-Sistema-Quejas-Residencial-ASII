package com.portal.residencial.casos_service.almacenamiento;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.Resource;

/* DONDE SE GUARDAN LOS ADJUNTOS */
public interface AlmacenamientoArchivos {

    /* GUARDA EL ARCHIVO Y DEVUELVE SU RUTA */
    String guardar(InputStream contenido, String carpeta, String extension) throws IOException;

    Resource cargar(String ruta) throws IOException;

    void eliminar(String ruta);
}
