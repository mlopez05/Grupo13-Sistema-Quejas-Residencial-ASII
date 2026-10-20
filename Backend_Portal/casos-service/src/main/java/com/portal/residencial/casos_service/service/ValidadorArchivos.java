package com.portal.residencial.casos_service.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.portal.residencial.casos_service.exception.BusinessException;
import com.portal.residencial.casos_service.model.TipoArchivo;

/* REVISA LOS ARCHIVOS ADJUNTOS */
@Component
public class ValidadorArchivos {

    private static final byte[] FIRMA_PDF = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] FIRMA_ZIP = { 0x50, 0x4B, 0x03, 0x04 };
    private static final byte[] FIRMA_OLE2 = {
            (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1 };
    private static final byte[] FIRMA_JPG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
    private static final byte[] FIRMA_PNG = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
    private static final byte[] FIRMA_ID3 = "ID3".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] FIRMA_FTYP = "ftyp".getBytes(StandardCharsets.US_ASCII);

    /* MARCA DE UN ARCHIVO DE OFFICE CON CONTRASEÑA */
    private static final byte[] FLUJO_CIFRADO_OFFICE = "EncryptionInfo".getBytes(StandardCharsets.UTF_16LE);

    public static final String FORMATOS_PERMITIDOS = Arrays.stream(TipoArchivo.values())
            .map(TipoArchivo::getExtension)
            .map(String::toUpperCase)
            .collect(Collectors.joining(", "));

    /* REVISA UN ARCHIVO Y DEVUELVE SU TIPO */
    public TipoArchivo validar(MultipartFile archivo) {
        String nombre = archivo.getOriginalFilename();

        TipoArchivo tipo = TipoArchivo.porNombreArchivo(nombre)
                .orElseThrow(() -> error(nombre, "formato no permitido. Formatos aceptados: " + FORMATOS_PERMITIDOS + "."));

        if (archivo.isEmpty()) {
            throw error(nombre, "el archivo está vacío.");
        }

        if (archivo.getSize() > tipo.getTamanoMaximoBytes()) {
            throw error(nombre, "supera el tamaño máximo de " + tipo.getTamanoMaximoMb() + " MB para este tipo de archivo.");
        }

        byte[] cabecera = leerCabecera(archivo);

        switch (tipo) {
            case PDF -> validarPdf(archivo, cabecera);
            case DOCX -> validarDocx(archivo, cabecera);
            case JPG, JPEG -> exigirFirma(nombre, cabecera, FIRMA_JPG, 0, "una imagen JPG");
            case PNG -> exigirFirma(nombre, cabecera, FIRMA_PNG, 0, "una imagen PNG");
            case MP3 -> validarMp3(nombre, cabecera);
            /* REVISA LA MARCA DE LOS ARCHIVOS M4A Y MP4 */
            case M4A -> exigirFirma(nombre, cabecera, FIRMA_FTYP, 4, "un audio M4A");
            case MP4 -> exigirFirma(nombre, cabecera, FIRMA_FTYP, 4, "un video MP4");
        }

        return tipo;
    }

    private void validarPdf(MultipartFile archivo, byte[] cabecera) {
        String nombre = archivo.getOriginalFilename();
        exigirFirma(nombre, cabecera, FIRMA_PDF, 0, "un PDF");

        /* REVISA SI EL PDF TIENE CONTRASEÑA */
        try (PDDocument pdf = Loader.loadPDF(archivo.getBytes())) {
            if (pdf.isEncrypted()) {
                throw errorContrasena(nombre);
            }
        } catch (InvalidPasswordException e) {
            throw errorContrasena(nombre);
        } catch (IOException e) {
            throw error(nombre, "el PDF está dañado o no se puede leer.");
        }
    }

    private void validarDocx(MultipartFile archivo, byte[] cabecera) {
        String nombre = archivo.getOriginalFilename();

        if (empiezaCon(cabecera, FIRMA_ZIP, 0)) {
            return; /* DOCX NORMAL */
        }

        if (empiezaCon(cabecera, FIRMA_OLE2, 0)) {
            try {
                if (contiene(archivo.getBytes(), FLUJO_CIFRADO_OFFICE)) {
                    throw errorContrasena(nombre);
                }
            } catch (IOException e) {
                throw error(nombre, "no se pudo leer el archivo.");
            }

            throw error(nombre, "parece un documento Word antiguo (.doc). Guárdelo como .docx o PDF.");
        }

        throw error(nombre, "el contenido no corresponde a un documento DOCX.");
    }

    private void validarMp3(String nombre, byte[] cabecera) {
        /* REVISA LA MARCA DE LOS ARCHIVOS MP3 */
        boolean conId3 = empiezaCon(cabecera, FIRMA_ID3, 0);
        boolean conFrame = cabecera.length >= 2
                && (cabecera[0] & 0xFF) == 0xFF
                && (cabecera[1] & 0xE0) == 0xE0;

        if (!conId3 && !conFrame) {
            throw error(nombre, "el contenido no corresponde a un audio MP3.");
        }
    }

    /* UTILIDADES */

    private byte[] leerCabecera(MultipartFile archivo) {
        try (InputStream entrada = archivo.getInputStream()) {
            return entrada.readNBytes(16);
        } catch (IOException e) {
            throw error(archivo.getOriginalFilename(), "no se pudo leer el archivo.");
        }
    }

    private void exigirFirma(String nombre, byte[] cabecera, byte[] firma, int desde, String descripcion) {
        if (!empiezaCon(cabecera, firma, desde)) {
            throw error(nombre, "el contenido no corresponde a " + descripcion + ".");
        }
    }

    private static boolean empiezaCon(byte[] datos, byte[] firma, int desde) {
        if (datos.length < desde + firma.length) {
            return false;
        }

        for (int i = 0; i < firma.length; i++) {
            if (datos[desde + i] != firma[i]) {
                return false;
            }
        }

        return true;
    }

    private static boolean contiene(byte[] datos, byte[] patron) {
        outer:
        for (int i = 0; i <= datos.length - patron.length; i++) {
            for (int j = 0; j < patron.length; j++) {
                if (datos[i + j] != patron[j]) {
                    continue outer;
                }
            }
            return true;
        }

        return false;
    }

    private static BusinessException errorContrasena(String nombre) {
        return error(nombre, "está protegido con contraseña. Quite la contraseña y vuelva a adjuntarlo.");
    }

    private static BusinessException error(String nombre, String motivo) {
        return new BusinessException("\"" + nombre + "\": " + motivo, HttpStatus.BAD_REQUEST);
    }
}
