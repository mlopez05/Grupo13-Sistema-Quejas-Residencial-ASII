package com.portal.residencial.casos_service.exportacion;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/* GENERA EL ARCHIVO PDF */
@Component
public class ExportadorPdf {

    public static final String TIPO_MIME = "application/pdf";

    private static final Font FUENTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font FUENTE_SECCION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FUENTE_TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(71, 85, 105));
    private static final Font FUENTE_COLUMNA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font FUENTE_CELDA = FontFactory.getFont(FontFactory.HELVETICA, 8);

    public byte[] generar(String titulo, List<String> encabezado, List<TablaExportable> tablas) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.LETTER.rotate(), 30, 30, 30, 30);
        PdfWriter.getInstance(documento, salida);
        documento.open();

        documento.add(new Paragraph(titulo, FUENTE_TITULO));
        for (String linea : encabezado) {
            documento.add(new Paragraph(linea, FUENTE_TEXTO));
        }

        for (TablaExportable tabla : tablas) {
            Paragraph seccion = new Paragraph(tabla.nombre(), FUENTE_SECCION);
            seccion.setSpacingBefore(14);
            seccion.setSpacingAfter(6);
            documento.add(seccion);

            if (tabla.filas().isEmpty()) {
                documento.add(new Paragraph("Sin registros para los filtros seleccionados.", FUENTE_TEXTO));
                continue;
            }

            PdfPTable pdfTabla = new PdfPTable(tabla.columnas().size());
            pdfTabla.setWidthPercentage(100);
            pdfTabla.setHeaderRows(1);

            for (String columna : tabla.columnas()) {
                PdfPCell celda = new PdfPCell(new Phrase(columna, FUENTE_COLUMNA));
                celda.setBackgroundColor(new Color(226, 232, 240));
                celda.setPadding(4);
                pdfTabla.addCell(celda);
            }

            for (List<Object> fila : tabla.filas()) {
                for (Object valor : fila) {
                    PdfPCell celda = new PdfPCell(new Phrase(valor == null ? "" : valor.toString(), FUENTE_CELDA));
                    celda.setPadding(3);
                    if (valor instanceof Number) {
                        celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    }
                    pdfTabla.addCell(celda);
                }
            }

            documento.add(pdfTabla);
        }

        documento.close();
        return salida.toByteArray();
    }
}
