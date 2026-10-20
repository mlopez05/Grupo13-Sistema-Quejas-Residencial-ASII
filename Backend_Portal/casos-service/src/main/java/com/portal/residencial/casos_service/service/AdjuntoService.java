package com.portal.residencial.casos_service.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.portal.residencial.casos_service.almacenamiento.AlmacenamientoArchivos;
import com.portal.residencial.casos_service.bitacora.BitacoraClient;
import com.portal.residencial.casos_service.bitacora.BitacoraDocumentoRequest;
import com.portal.residencial.casos_service.config.UsuarioAutenticado;
import com.portal.residencial.casos_service.exception.BusinessException;
import com.portal.residencial.casos_service.model.Caso;
import com.portal.residencial.casos_service.model.Documento;
import com.portal.residencial.casos_service.model.SeguimientoCaso;
import com.portal.residencial.casos_service.model.TipoArchivo;
import com.portal.residencial.casos_service.repository.DocumentoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/* ADJUNTOS DE LOS CASOS */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdjuntoService {

    private static final int MAXIMO_ARCHIVOS_POR_ACCION = 10;
    private static final int LONGITUD_MAXIMA_NOMBRE = 200;
    private static final long MB = 1024L * 1024L;

    private final DocumentoRepository documentoRepository;
    private final AlmacenamientoArchivos almacenamiento;
    private final ValidadorArchivos validador;
    private final BitacoraClient bitacoraClient;

    /* REVISA QUE LOS ARCHIVOS CUMPLAN LAS REGLAS */
    public List<TipoArchivo> validar(List<MultipartFile> archivos, Long idCaso) {
        if (archivos.isEmpty()) {
            return List.of();
        }

        if (archivos.size() > MAXIMO_ARCHIVOS_POR_ACCION) {
            throw new BusinessException("Puede adjuntar hasta " + MAXIMO_ARCHIVOS_POR_ACCION + " archivos a la vez.",
                    HttpStatus.BAD_REQUEST);
        }

        List<TipoArchivo> tipos = archivos.stream().map(validador::validar).toList();

        long usado = idCaso == null ? 0 : documentoRepository.sumarTamanoPorCaso(idCaso);
        long nuevo = archivos.stream().mapToLong(MultipartFile::getSize).sum();

        if (usado + nuevo > TipoArchivo.MAXIMO_TOTAL_POR_CASO) {
            long disponibleMb = Math.max(0, TipoArchivo.MAXIMO_TOTAL_POR_CASO - usado) / MB;
            throw new BusinessException("Se supera el máximo de " + TipoArchivo.MAXIMO_TOTAL_POR_CASO / MB +
                    " MB de adjuntos por caso. Espacio disponible: " + disponibleMb + " MB.", HttpStatus.BAD_REQUEST);
        }

        return tipos;
    }

    /* GUARDA LOS ARCHIVOS DEL CASO */
    public void guardar(Caso caso, SeguimientoCaso seguimiento, List<MultipartFile> archivos,
            List<TipoArchivo> tipos, UsuarioAutenticado usuario, String ip) {

        if (archivos.isEmpty()) {
            return;
        }

        List<String> rutasGuardadas = new ArrayList<>();
        List<Documento> documentos = new ArrayList<>();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    documentos.forEach(documento -> registrarBitacora(documento, "CARGA", usuario, ip));
                } else {
                    rutasGuardadas.forEach(almacenamiento::eliminar);
                }
            }
        });

        try {
            for (int i = 0; i < archivos.size(); i++) {
                MultipartFile archivo = archivos.get(i);
                TipoArchivo tipo = tipos.get(i);

                String ruta;
                try (InputStream contenido = archivo.getInputStream()) {
                    ruta = almacenamiento.guardar(contenido, "caso-" + caso.getId(), tipo.getExtension());
                }
                rutasGuardadas.add(ruta);

                documentos.add(documentoRepository.save(Documento.builder()
                        .caso(caso)
                        .seguimiento(seguimiento)
                        .nombreOriginal(limpiarNombre(archivo.getOriginalFilename(), tipo))
                        .tipoArchivo(tipo.name())
                        .tamanoBytes(archivo.getSize())
                        .rutaAlmacenamiento(ruta)
                        .idUsuario(usuario.id())
                        .nombreUsuario(usuario.nombreCompleto())
                        .rolUsuario(usuario.rol())
                        .fechaCarga(seguimiento.getFecha())
                        .build()));
            }
        } catch (IOException e) {
            /* SI FALLA SE BORRAN LOS ARCHIVOS GUARDADOS */
            log.error("Error al guardar adjuntos del caso {}: {}", caso.getNumeroCaso(), e.getMessage());
            throw new BusinessException("No se pudieron guardar los archivos. Intente nuevamente.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /* DESCARGA UN ADJUNTO */
    public void registrarDescarga(Documento documento, UsuarioAutenticado usuario, String ip) {
        registrarBitacora(documento, "DESCARGA", usuario, ip);
    }

    /* LIMPIA EL NOMBRE DEL ARCHIVO */
    private String limpiarNombre(String nombreOriginal, TipoArchivo tipo) {
        String nombre = nombreOriginal == null ? "" : nombreOriginal;
        nombre = nombre.substring(Math.max(nombre.lastIndexOf('/'), nombre.lastIndexOf('\\')) + 1);
        nombre = nombre.replaceAll("[\\p{Cntrl}\"]", "").trim();

        if (nombre.isBlank()) {
            nombre = "archivo." + tipo.getExtension();
        }

        if (nombre.length() > LONGITUD_MAXIMA_NOMBRE) {
            String extension = "." + tipo.getExtension();
            nombre = nombre.substring(0, LONGITUD_MAXIMA_NOMBRE - extension.length()) + extension;
        }

        return nombre;
    }

    private void registrarBitacora(Documento documento, String tipoEvento, UsuarioAutenticado usuario, String ip) {
        Caso caso = documento.getCaso();

        bitacoraClient.registrarDocumento(new BitacoraDocumentoRequest(
                documento.getId(),
                caso.getId(),
                caso.getNumeroCaso(),
                documento.getNombreOriginal(),
                TipoArchivo.valueOf(documento.getTipoArchivo()).getExtension(),
                documento.getTamanoBytes(),
                tipoEvento,
                ip,
                usuario.id(),
                usuario.nombre(),
                usuario.apellido(),
                usuario.rol()));
    }
}
