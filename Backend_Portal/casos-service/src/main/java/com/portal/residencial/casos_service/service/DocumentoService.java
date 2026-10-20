package com.portal.residencial.casos_service.service;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.portal.residencial.casos_service.almacenamiento.AlmacenamientoArchivos;
import com.portal.residencial.casos_service.config.UsuarioAutenticado;
import com.portal.residencial.casos_service.exception.BusinessException;
import com.portal.residencial.casos_service.model.Caso;
import com.portal.residencial.casos_service.model.CategoriaArchivo;
import com.portal.residencial.casos_service.model.Documento;
import com.portal.residencial.casos_service.model.TipoArchivo;
import com.portal.residencial.casos_service.repository.DocumentoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/* CONSULTA Y DESCARGA DE ADJUNTOS */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentoService {

    private final CasoService casoService;
    private final DocumentoRepository documentoRepository;
    private final AlmacenamientoArchivos almacenamiento;
    private final AdjuntoService adjuntoService;

    /* ARCHIVO PARA DESCARGAR */
    public record ArchivoDescarga(Resource recurso, String nombre, String tipoMime, long tamanoBytes) {}

    /* DESCARGA EL ARCHIVO Y LO REGISTRA EN BITÁCORA */
    public ArchivoDescarga descargar(Long idCaso, Long idDocumento, UsuarioAutenticado usuario, String ip) {
        Documento documento = buscarDocumento(idCaso, idDocumento, usuario);
        ArchivoDescarga archivo = cargar(documento);

        adjuntoService.registrarDescarga(documento, usuario, ip);

        return archivo;
    }

    /* MINIATURA DE UNA IMAGEN */
    public ArchivoDescarga vistaPrevia(Long idCaso, Long idDocumento, UsuarioAutenticado usuario) {
        Documento documento = buscarDocumento(idCaso, idDocumento, usuario);

        if (TipoArchivo.valueOf(documento.getTipoArchivo()).getCategoria() != CategoriaArchivo.IMAGEN) {
            throw new BusinessException("La vista previa solo está disponible para imágenes.",
                    HttpStatus.BAD_REQUEST);
        }

        return cargar(documento);
    }

    /* VALIDA QUE EL USUARIO PUEDA VER EL CASO */
    private Documento buscarDocumento(Long idCaso, Long idDocumento, UsuarioAutenticado usuario) {
        Caso caso = casoService.buscarCasoVisible(idCaso, usuario);

        return documentoRepository.findByIdAndCaso_Id(idDocumento, caso.getId())
                .orElseThrow(() -> new BusinessException("Documento no encontrado.", HttpStatus.NOT_FOUND));
    }

    private ArchivoDescarga cargar(Documento documento) {
        try {
            return new ArchivoDescarga(
                    almacenamiento.cargar(documento.getRutaAlmacenamiento()),
                    documento.getNombreOriginal(),
                    TipoArchivo.valueOf(documento.getTipoArchivo()).getTipoMime(),
                    documento.getTamanoBytes());
        } catch (IOException e) {
            log.error("No se encontró el archivo del documento {}: {}", documento.getId(), e.getMessage());
            throw new BusinessException("El archivo no está disponible.", HttpStatus.NOT_FOUND);
        }
    }
}
