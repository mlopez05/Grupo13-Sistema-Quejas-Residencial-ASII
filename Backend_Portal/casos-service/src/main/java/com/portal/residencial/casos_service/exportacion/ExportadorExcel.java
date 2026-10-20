package com.portal.residencial.casos_service.exportacion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/* GENERA EL ARCHIVO DE EXCEL */
@Component
public class ExportadorExcel {

    public static final String TIPO_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public byte[] generar(String titulo, List<String> encabezado, List<TablaExportable> tablas) {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            CellStyle estiloTitulo = estilo(libro, true, 14, false);
            CellStyle estiloColumna = estilo(libro, true, 11, true);

            for (TablaExportable tabla : tablas) {
                Sheet hoja = libro.createSheet(nombreHoja(tabla.nombre()));
                int fila = 0;

                Cell celdaTitulo = hoja.createRow(fila++).createCell(0);
                celdaTitulo.setCellValue(titulo + " — " + tabla.nombre());
                celdaTitulo.setCellStyle(estiloTitulo);

                for (String linea : encabezado) {
                    hoja.createRow(fila++).createCell(0).setCellValue(linea);
                }
                fila++;

                Row filaColumnas = hoja.createRow(fila++);
                for (int c = 0; c < tabla.columnas().size(); c++) {
                    Cell celda = filaColumnas.createCell(c);
                    celda.setCellValue(tabla.columnas().get(c));
                    celda.setCellStyle(estiloColumna);
                }

                for (List<Object> datos : tabla.filas()) {
                    Row filaDatos = hoja.createRow(fila++);
                    for (int c = 0; c < datos.size(); c++) {
                        escribir(filaDatos.createCell(c), datos.get(c));
                    }
                }

                for (int c = 0; c < tabla.columnas().size(); c++) {
                    hoja.autoSizeColumn(c);
                }
            }

            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el archivo Excel.", e);
        }
    }

    private static void escribir(Cell celda, Object valor) {
        if (valor == null) {
            celda.setBlank();
        } else if (valor instanceof Number numero) {
            celda.setCellValue(numero.doubleValue());
        } else {
            celda.setCellValue(valor.toString());
        }
    }

    private static CellStyle estilo(Workbook libro, boolean negrita, int tamano, boolean relleno) {
        Font fuente = libro.createFont();
        fuente.setBold(negrita);
        fuente.setFontHeightInPoints((short) tamano);

        CellStyle estilo = libro.createCellStyle();
        estilo.setFont(fuente);

        if (relleno) {
            estilo.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        return estilo;
    }

    /* AJUSTA EL NOMBRE DE LA HOJA */
    private static String nombreHoja(String nombre) {
        String limpio = nombre.replaceAll("[\\\\/?*\\[\\]:]", " ");
        return limpio.length() > 31 ? limpio.substring(0, 31) : limpio;
    }
}
