package com.mycompany.proyecto_si2.infra.pdf;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.mycompany.proyecto_si2.domain.service.InformeRecibosQueriesService.ReciboObjetivoDTO;
import com.mycompany.proyecto_si2.domain.service.InformeRecibosQueriesService.ResumenAyuntamientoDTO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class InformeRecibosPdfGenerator {

    private static final DecimalFormat DF;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "ES"));
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        DF = new DecimalFormat("#,##0.00", symbols);
    }

    public static void generarPdf(
            String rutaSalida,
            ReciboObjetivoDTO reciboObjetivo,
            String hqlUsado,
            List<ResumenAyuntamientoDTO> resumenes
    ) throws IOException {

        Path path = Paths.get(rutaSalida);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        PdfWriter writer = new PdfWriter(rutaSalida);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(30, 30, 30, 30);

        document.add(
                new Paragraph("INFORME FINAL TRAS EJECUCIÓN DE LOS 3 EXCEL")
                        .simulateBold()
                        .setFontSize(16)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(20)
        );

        String lineaRecibo;
        if (reciboObjetivo != null) {
            lineaRecibo = "Importe total del recibo: " + format(reciboObjetivo.getTotalRecibo())
                    + " | Nombre: " + safe(reciboObjetivo.getNombre())
                    + " | Apellidos: " + safe(reciboObjetivo.getApellidos())
                    + " | NIF: " + safe(reciboObjetivo.getNif())
                    + " | Dirección: " + safe(reciboObjetivo.getDireccion())
                    + " | Ayuntamiento: " + safe(reciboObjetivo.getAyuntamiento());
        } else {
            lineaRecibo = "No se encontró ningún recibo que cumpla la condición del máximo inferior a la media.";
        }

        document.add(
                new Paragraph(lineaRecibo)
                        .setFontSize(11)
                        .setMarginBottom(10)
        );

        document.add(
                new Paragraph("HQL utilizado: " + hqlUsado)
                        .setFontSize(9)
                        .setFontColor(ColorConstants.DARK_GRAY)
                        .setMarginBottom(20)
        );

        document.add(
                new Paragraph("Tabla por Ayuntamiento")
                        .simulateBold()
                        .setFontSize(13)
                        .setMarginBottom(10)
        );

        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 25, 25}))
                .useAllAvailableWidth();

        table.addHeaderCell(headerCell("Ayuntamiento"));
        table.addHeaderCell(headerCell("Nº filas ordenanza"));
        table.addHeaderCell(headerCell("Importe medio"));

        if (resumenes != null && !resumenes.isEmpty()) {
            for (ResumenAyuntamientoDTO resumen : resumenes) {
                table.addCell(bodyCell(safe(resumen.getAyuntamiento()), TextAlignment.LEFT));
                table.addCell(bodyCell(String.valueOf(resumen.getNumeroFilasOrdenanza()), TextAlignment.RIGHT));
                table.addCell(bodyCell(format(resumen.getImporteMedio()), TextAlignment.RIGHT));
            }
        } else {
            Cell cell = new Cell(1, 3)
                    .add(new Paragraph("No hay datos de ordenanzas para mostrar.").setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorder(new SolidBorder(1));
            table.addCell(cell);
        }

        document.add(table);
        document.close();
    }

    private static Cell headerCell(String texto) {
        return new Cell()
                .add(new Paragraph(texto).simulateBold().setFontSize(10))
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBorder(new SolidBorder(1))
                .setPadding(6);
    }

    private static Cell bodyCell(String texto, TextAlignment alignment) {
        return new Cell()
                .add(new Paragraph(texto).setFontSize(10))
                .setTextAlignment(alignment)
                .setBorder(new SolidBorder(1))
                .setPadding(5);
    }

    private static String format(double value) {
        return DF.format(value);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}