package com.portal.residencial.casos_service.exportacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class ExportadoresTest {

    private final List<TablaExportable> tablas = List.of(
            new TablaExportable("Resumen", List.of("Indicador", "Valor"),
                    List.of(List.of("Total de casos", 12), Arrays.asList("Horas promedio", null))),
            new TablaExportable("Detalle de casos", List.of("Caso", "Estado"),
                    List.of(List.of("CASO-2026-000001", "En análisis"))),
            new TablaExportable("Casos estancados", List.of("Caso"), List.of()));

    @Test
    void generaExcelConUnaHojaPorTabla() throws IOException {
        byte[] archivo = new ExportadorExcel().generar("Reporte de casos", List.of("Período: 01/10/2026 al 20/10/2026"), tablas);

        try (Workbook libro = new XSSFWorkbook(new ByteArrayInputStream(archivo))) {
            assertEquals(3, libro.getNumberOfSheets());
            assertEquals("Resumen", libro.getSheetAt(0).getSheetName());
            // título, 1 línea de filtro, línea vacía, columnas -> los datos empiezan en la fila 4
            assertEquals(12.0, libro.getSheetAt(0).getRow(4).getCell(1).getNumericCellValue());
            assertEquals("CASO-2026-000001", libro.getSheetAt(1).getRow(4).getCell(0).getStringCellValue());
        }
    }

    @Test
    void generaPdfLegibleConLasSecciones() throws IOException {
        byte[] archivo = new ExportadorPdf().generar("Reporte de casos", List.of("Período: 01/10/2026 al 20/10/2026"), tablas);

        try (PDDocument pdf = Loader.loadPDF(archivo)) {
            String texto = new PDFTextStripper().getText(pdf);
            assertTrue(texto.contains("Reporte de casos"));
            assertTrue(texto.contains("CASO-2026-000001"));
            assertTrue(texto.contains("Sin registros para los filtros seleccionados."));
        }
    }
}
