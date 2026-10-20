package com.portal.residencial.casos_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.portal.residencial.casos_service.exception.BusinessException;
import com.portal.residencial.casos_service.model.TipoArchivo;

class ValidadorArchivosTest {

    private final ValidadorArchivos validador = new ValidadorArchivos();

    // ----- PDF -----

    @Test
    void aceptaPdfNormal() throws IOException {
        assertEquals(TipoArchivo.PDF, validador.validar(archivo("informe.pdf", pdf(null, null))));
    }

    @Test
    void rechazaPdfConContrasenaDeApertura() throws IOException {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validador.validar(archivo("secreto.pdf", pdf("1234", "admin"))));
        assertTrue(ex.getMessage().contains("protegido con contraseña"));
    }

    @Test
    void rechazaPdfConSoloContrasenaDePropietario() throws IOException {
        // Abre sin clave, pero tiene restricciones con contraseña: también se rechaza
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validador.validar(archivo("restringido.pdf", pdf("", "admin"))));
        assertTrue(ex.getMessage().contains("protegido con contraseña"));
    }

    @Test
    void rechazaPdfDanado() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validador.validar(archivo("roto.pdf", "%PDF-1.7 basura".getBytes())));
        assertTrue(ex.getMessage().contains("dañado"));
    }

    // ----- DOCX -----

    @Test
    void aceptaDocxNormal() throws IOException {
        assertEquals(TipoArchivo.DOCX, validador.validar(archivo("acta.docx", zip())));
    }

    @Test
    void rechazaDocxConContrasena() {
        // Un Office cifrado es un contenedor OLE2 con el flujo "EncryptionInfo"
        byte[] contenido = concatenar(
                new byte[] { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1 },
                new byte[512],
                "EncryptionInfo".getBytes(StandardCharsets.UTF_16LE));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> validador.validar(archivo("acta.docx", contenido)));
        assertTrue(ex.getMessage().contains("protegido con contraseña"));
    }

    @Test
    void rechazaDocAntiguoRenombrado() {
        byte[] contenido = concatenar(
                new byte[] { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1 },
                new byte[512]);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> validador.validar(archivo("viejo.docx", contenido)));
        assertTrue(ex.getMessage().contains(".doc"));
    }

    // ----- Firma, formato y tamaño -----

    @Test
    void rechazaEjecutableRenombrado() {
        byte[] exe = concatenar("MZ".getBytes(), new byte[100]);

        assertThrows(BusinessException.class, () -> validador.validar(archivo("foto.jpg", exe)));
        assertThrows(BusinessException.class, () -> validador.validar(archivo("doc.pdf", exe)));
    }

    @Test
    void rechazaFormatoNoPermitido() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validador.validar(archivo("datos.xlsx", zip())));
        assertTrue(ex.getMessage().contains("formato no permitido"));
    }

    @Test
    void aceptaImagenesYMultimedia() {
        assertEquals(TipoArchivo.PNG, validador.validar(archivo("a.png",
                concatenar(new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A }, new byte[20]))));
        assertEquals(TipoArchivo.JPG, validador.validar(archivo("a.JPG",
                concatenar(new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0 }, new byte[20]))));
        assertEquals(TipoArchivo.MP3, validador.validar(archivo("a.mp3",
                concatenar("ID3".getBytes(), new byte[20]))));
        assertEquals(TipoArchivo.MP4, validador.validar(archivo("a.mp4",
                concatenar(new byte[] { 0, 0, 0, 0x18 }, "ftypmp42".getBytes(), new byte[20]))));
    }

    @Test
    void rechazaImagenQueSuperaElLimite() {
        byte[] grande = concatenar(
                new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A },
                new byte[(int) TipoArchivo.PNG.getTamanoMaximoBytes()]);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> validador.validar(archivo("enorme.png", grande)));
        assertTrue(ex.getMessage().contains("8 MB"));
    }

    // ----- utilidades -----

    private static MockMultipartFile archivo(String nombre, byte[] contenido) {
        return new MockMultipartFile("archivos", nombre, "application/octet-stream", contenido);
    }

    private static byte[] pdf(String claveUsuario, String clavePropietario) throws IOException {
        try (PDDocument documento = new PDDocument(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            documento.addPage(new PDPage());

            if (clavePropietario != null) {
                StandardProtectionPolicy politica =
                        new StandardProtectionPolicy(clavePropietario, claveUsuario, new AccessPermission());
                politica.setEncryptionKeyLength(128);
                documento.protect(politica);
            }

            documento.save(salida);
            return salida.toByteArray();
        }
    }

    private static byte[] zip() {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream();
                ZipOutputStream zip = new ZipOutputStream(salida)) {
            zip.putNextEntry(new ZipEntry("[Content_Types].xml"));
            zip.write("<Types/>".getBytes());
            zip.closeEntry();
            zip.finish();
            return salida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] concatenar(byte[]... partes) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        for (byte[] parte : partes) {
            salida.writeBytes(parte);
        }
        return salida.toByteArray();
    }
}
