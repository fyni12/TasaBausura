package com.mycompany.proyecto_si2.infra.pdf;

import com.mycompany.proyecto_si2.domain.model.PeriodoImpositivo;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
/**
 * Clase de utilidad encargada de generar documentos PDF relacionados con los recibos y con el resumen
 * del padrón. Su función principal es construir el contenido visual de los documentos, formatear los
 * importes económicos y organizar la información en tablas y bloques preparados para su impresión o
 * almacenamiento.
 *
 * Funciones de la clase:
 *
 * - generateResumenPdf(String destino, String periodo, BigDecimal totalBase, BigDecimal totalIva, int numeroRecibos):
 *   Genera un PDF de resumen del padrón para un período concreto expresado como texto. El documento
 *   incluye el período, la base imponible total, el total de IVA, el número de recibos y el importe
 *   total del padrón.
 *
 * - generatePdf(String destino, ReciboData data):
 *   Genera el PDF completo de un recibo individual a partir de una estructura de datos con toda la
 *   información necesaria. Compone el documento añadiendo la cabecera, los datos del destinatario,
 *   las lecturas, el título, la tabla de conceptos y el resumen final de importes.
 *
 * - addCabecera(Document document, ReciboData data):
 *   Método auxiliar privado que construye la parte superior del recibo con los datos de la entidad
 *   emisora, el código del recibo, la fecha de generación, la dirección del emisor y varios datos
 *   informativos del contribuyente.
 *
 * - addBloqueDestinatarioYLogo(Document document, ReciboData data):
 *   Método auxiliar privado que añade el bloque con el logotipo de la entidad y los datos del
 *   destinatario del recibo.
 *
 * - addLecturas(Document document, ReciboData data):
 *   Método auxiliar privado que inserta un bloque con la lectura actual, la lectura anterior y
 *   los kilogramos generados.
 *
 * - addTitulo(Document document, ReciboData data):
 *   Método auxiliar privado que añade el título principal del recibo centrado y con formato destacado.
 *
 * - addTablaConceptos(Document document, ReciboData data):
 *   Método auxiliar privado que construye la tabla de conceptos del recibo, mostrando para cada línea
 *   el concepto, subconcepto, kilos incluidos, base imponible, porcentaje de IVA e importe de IVA.
 *   También añade una fila final con los totales acumulados.
 *
 * - addResumenTotales(Document document, ReciboData data):
 *   Método auxiliar privado que genera el bloque final de resumen con el total de base imponible,
 *   el total de IVA y el total completo del recibo.
 *
 * - simpleBoxCell(String text, TextAlignment alignment):
 *   Método auxiliar privado que crea una celda sencilla con borde y texto alineado, utilizada en
 *   distintos bloques del PDF.
 *
 * - headerCell(String text):
 *   Método auxiliar privado que crea una celda de cabecera para la tabla de conceptos, con formato
 *   específico de borde y alineación.
 *
 * - bodyCell(String text, TextAlignment alignment):
 *   Método auxiliar privado que crea una celda de contenido normal para la tabla de conceptos.
 *
 * - resumenLabelCell(String text, boolean topBorder):
 *   Método auxiliar privado que crea una celda de etiqueta dentro del bloque de resumen de importes.
 *
 * - resumenValueCell(String text, boolean topBorder):
 *   Método auxiliar privado que crea una celda de valor numérico dentro del bloque de resumen final.
 *
 * - p(String text, boolean bold, float size, TextAlignment alignment):
 *   Método auxiliar privado que construye un párrafo con texto seguro, tamaño configurable, alineación
 *   y opción de resaltado en negrita.
 *
 * - calculateTotalBase(List<LineaConcepto> lineas):
 *   Método auxiliar privado que suma las bases imponibles de todas las líneas de concepto y devuelve
 *   el total redondeado a dos decimales.
 *
 * - calculateTotalIva(List<LineaConcepto> lineas):
 *   Método auxiliar privado que suma los importes de IVA de todas las líneas de concepto y devuelve
 *   el total redondeado a dos decimales.
 *
 * - calculateIva(LineaConcepto linea):
 *   Método auxiliar privado que calcula el IVA de una línea de concepto. Si el importe de IVA ya está
 *   informado, lo reutiliza; en caso contrario, lo calcula a partir de la base imponible y del porcentaje.
 *
 * - nvl(BigDecimal value):
 *   Método auxiliar privado que devuelve cero cuando el valor recibido es nulo, evitando problemas en
 *   las operaciones con importes.
 *
 * - format(BigDecimal value):
 *   Método auxiliar privado que convierte un valor numérico a texto con formato decimal adaptado a la
 *   configuración española.
 *
 * - safe(String value):
 *   Método auxiliar privado que devuelve una cadena vacía cuando el texto recibido es nulo.
 *
 * - generateResumenPdf(String destino, PeriodoImpositivo periodo, BigDecimal totalBase, BigDecimal totalIva, int numeroRecibos):
 *   Sobrecarga del método de generación de resumen que recibe directamente un objeto PeriodoImpositivo
 *   y utiliza su representación textual para construir el PDF resumen.
 *
 * Clases internas:
 *
 * - ReciboData:
 *   Clase contenedora de datos utilizada para reunir toda la información necesaria para generar un PDF
 *   de recibo, incluyendo datos del emisor, destinatario, lecturas, importes y líneas de concepto.
 *
 * - LineaConcepto:
 *   Clase contenedora que representa una línea individual dentro del detalle del recibo, almacenando
 *   el concepto, subconcepto, kilogramos, base imponible, porcentaje de IVA e importe de IVA.
 */
public class PDFGenerator {

    private static final String imgPath = Paths.get("resources").resolve("logo.png").toString();
    private static final DecimalFormat DF;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "ES"));
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        DF = new DecimalFormat("#,##0.00", symbols);
    }

    public static void generateResumenPdf(String destino, String periodo,
            BigDecimal totalBase, BigDecimal totalIva,
            int numeroRecibos) throws IOException {
        PdfWriter writer = new PdfWriter(destino);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(30, 30, 30, 30);

        document.add(new Paragraph("Resumen del padrón")
                .simulateBold()
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        document.add(new Paragraph("Periodo: " + safe(periodo)).setFontSize(12));
        document.add(new Paragraph("Total base imponible: " + format(totalBase)).setFontSize(12));
        document.add(new Paragraph("Total IVA: " + format(totalIva)).setFontSize(12));
        document.add(new Paragraph("Total recibos: " + numeroRecibos).setFontSize(12));
        document.add(new Paragraph("Total padrón: " + format(totalBase.add(totalIva)))
                .simulateBold()
                .setFontSize(12)
                .setMarginTop(10));

        document.close();
    }

    public static void generatePdf(String destino, ReciboData data) throws IOException {
        PdfWriter writer = new PdfWriter(destino);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(20, 20, 20, 20);

        addCabecera(document, data);
        addBloqueDestinatarioYLogo(document, data);
        addLecturas(document, data);
        addTitulo(document, data);
        addTablaConceptos(document, data);
        addResumenTotales(document, data);

        document.close();
    }

    private static void addCabecera(Document document, ReciboData data) {
        Table cabecera = new Table(UnitValue.createPercentArray(new float[]{55, 45}))
                .useAllAvailableWidth();

        Cell emisor = new Cell()
                .setBorder(new SolidBorder(1))
                .setPadding(10)
                .setTextAlignment(TextAlignment.CENTER);

        emisor.add(p(data.entidadEmisora, false, 15, TextAlignment.CENTER));
        emisor.add(p(data.codigoRecibo, false, 12, TextAlignment.CENTER));
        emisor.add(p("Fecha Generación Recibo: " + safe(data.fechaGeneracionRecibo), false, 11, TextAlignment.CENTER));
        emisor.add(p(safe(data.direccionEmisorLinea1), false, 11, TextAlignment.CENTER));
        emisor.add(p(safe(data.direccionEmisorLinea2), false, 11, TextAlignment.CENTER));

        Cell datosDerecha = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingLeft(12)
                .setTextAlignment(TextAlignment.RIGHT);

        datosDerecha.add(p("IBAN: " + safe(data.iban), false, 11, TextAlignment.RIGHT));
        datosDerecha.add(p("Tipo de cálculo: " + safe(data.tipoCalculo), false, 11, TextAlignment.RIGHT));
        datosDerecha.add(p("Fecha de alta: " + safe(data.fechaAlta), false, 11, TextAlignment.RIGHT));
        datosDerecha.add(p(safe(data.situacionContribuyente), false, 11, TextAlignment.RIGHT));
        datosDerecha.add(p(safe(data.textoBonificacion), false, 11, TextAlignment.RIGHT));

        cabecera.addCell(emisor);
        cabecera.addCell(datosDerecha);

        document.add(cabecera);
    }

    private static void addBloqueDestinatarioYLogo(Document document, ReciboData data) throws IOException {
        Table bloque = new Table(UnitValue.createPercentArray(new float[]{55, 45}))
                .useAllAvailableWidth()
                .setMarginTop(10);

        Cell logoCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setMinHeight(110)
                .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);

        Image image = new Image(ImageDataFactory.create(imgPath));
        image.scaleToFit(90, 90);
        image.setHorizontalAlignment(HorizontalAlignment.LEFT);
        logoCell.add(image);

        Cell destinatario = new Cell()
                .setBorder(new SolidBorder(1))
                .setPadding(10)
                .setMinHeight(110);

        destinatario.add(new Paragraph("Destinatario:")
                .simulateBold()
                .setFontSize(11)
                .setTextAlignment(TextAlignment.LEFT));

        destinatario.add(new Paragraph(safe(data.nombreDestinatario))
                .setFontSize(11)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(10));

        destinatario.add(new Paragraph("DNI: " + safe(data.dniDestinatario))
                .setFontSize(11)
                .setTextAlignment(TextAlignment.RIGHT));

        destinatario.add(new Paragraph(safe(data.referenciaDestinatario))
                .setFontSize(11)
                .setTextAlignment(TextAlignment.RIGHT));

        destinatario.add(new Paragraph(safe(data.poblacionDestinatario))
                .setFontSize(11)
                .setTextAlignment(TextAlignment.RIGHT));

        bloque.addCell(logoCell);
        bloque.addCell(destinatario);

        document.add(bloque);
    }

    private static void addLecturas(Document document, ReciboData data) {
        Table lecturas = new Table(UnitValue.createPercentArray(new float[]{33, 33, 34}))
                .useAllAvailableWidth()
                .setMarginTop(10);

        lecturas.addCell(simpleBoxCell("Lectura actual: " + data.lecturaActual, TextAlignment.LEFT));
        lecturas.addCell(simpleBoxCell("Lectura anterior: " + data.lecturaAnterior, TextAlignment.LEFT));
        lecturas.addCell(simpleBoxCell("KgGenerados: " + data.kgGenerados + " Kg.", TextAlignment.RIGHT));

        document.add(lecturas);
    }

    private static void addTitulo(Document document, ReciboData data) {
        document.add(new Paragraph(safe(data.tituloRecibo))
                .simulateBold()
                .simulateItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(13)
                .setMarginTop(30)
                .setMarginBottom(25));
    }

    private static void addTablaConceptos(Document document, ReciboData data) {
        Table tabla = new Table(UnitValue.createPercentArray(new float[]{2.3f, 2.1f, 1.7f, 1.9f, 1.8f, 1.7f}))
                .useAllAvailableWidth();

        tabla.addHeaderCell(headerCell("Concepto"));
        tabla.addHeaderCell(headerCell("Subconcepto"));
        tabla.addHeaderCell(headerCell("Kg incluídos"));
        tabla.addHeaderCell(headerCell("Base Imponible"));
        tabla.addHeaderCell(headerCell("Porcentaje IVA"));
        tabla.addHeaderCell(headerCell("Importe IVA"));

        for (LineaConcepto linea : data.lineas) {
            tabla.addCell(bodyCell(safe(linea.concepto), TextAlignment.LEFT));
            tabla.addCell(bodyCell(safe(linea.subconcepto), TextAlignment.LEFT));
            tabla.addCell(bodyCell(format(linea.kgIncluidos), TextAlignment.RIGHT));
            tabla.addCell(bodyCell(format(linea.baseImponible), TextAlignment.RIGHT));
            tabla.addCell(bodyCell(format(linea.porcentajeIva) + "%", TextAlignment.RIGHT));
            tabla.addCell(bodyCell(format(calculateIva(linea)), TextAlignment.RIGHT));
        }

        BigDecimal totalBase = data.totalBase != null ? data.totalBase : calculateTotalBase(data.lineas);
        BigDecimal totalIva = data.totalIva != null ? data.totalIva : calculateTotalIva(data.lineas);

        tabla.addCell(new Cell(1, 3)
                .add(new Paragraph("TOTALES").setTextAlignment(TextAlignment.CENTER).setFontSize(10))
                .setBorderTop(new SolidBorder(1.2f))
                .setBorderBottom(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setPaddingTop(8)
                .setPaddingBottom(8));

        tabla.addCell(new Cell()
                .add(new Paragraph(format(totalBase)).setTextAlignment(TextAlignment.RIGHT).setFontSize(10))
                .setBorderTop(new SolidBorder(1.2f))
                .setBorderBottom(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setPaddingTop(8)
                .setPaddingBottom(8));

        tabla.addCell(new Cell()
                .add(new Paragraph("").setTextAlignment(TextAlignment.CENTER).setFontSize(10))
                .setBorderTop(new SolidBorder(1.2f))
                .setBorderBottom(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setPaddingTop(8)
                .setPaddingBottom(8));

        tabla.addCell(new Cell()
                .add(new Paragraph(format(totalIva)).setTextAlignment(TextAlignment.RIGHT).setFontSize(10))
                .setBorderTop(new SolidBorder(1.2f))
                .setBorderBottom(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setPaddingTop(8)
                .setPaddingBottom(8));
        document.add(tabla);
    }

    private static void addResumenTotales(Document document, ReciboData data) {
        BigDecimal totalBase = data.totalBase != null ? data.totalBase : calculateTotalBase(data.lineas);
        BigDecimal totalIva = data.totalIva != null ? data.totalIva : calculateTotalIva(data.lineas);
        BigDecimal totalRecibo = data.totalRecibo != null ? data.totalRecibo : totalBase.add(totalIva);

        Table resumen = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .useAllAvailableWidth()
                .setMarginTop(25);

        resumen.addCell(resumenLabelCell("TOTAL BASE IMPONIBLE......................................", true));
        resumen.addCell(resumenValueCell(format(totalBase), true));

        resumen.addCell(resumenLabelCell("TOTAL IVA..............................................................", false));
        resumen.addCell(resumenValueCell(format(totalIva), false));

        resumen.addCell(resumenLabelCell("TOTAL RECIBO...........................................................", true));
        resumen.addCell(resumenValueCell(format(totalRecibo), true));

        document.add(resumen);
    }

    private static Cell simpleBoxCell(String text, TextAlignment alignment) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(11).setTextAlignment(alignment))
                .setBorder(new SolidBorder(1))
                .setPadding(4);
    }

    private static Cell headerCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(10).setTextAlignment(TextAlignment.CENTER))
                .setBorderTop(new SolidBorder(1.2f))
                .setBorderBottom(new SolidBorder(1.2f))
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setPaddingTop(6)
                .setPaddingBottom(6);
    }

    private static Cell bodyCell(String text, TextAlignment alignment) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(10).setTextAlignment(alignment))
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(4)
                .setPaddingBottom(4);
    }

    private static Cell resumenLabelCell(String text, boolean topBorder) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setFontSize(11).setTextAlignment(TextAlignment.LEFT))
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderBottom(Border.NO_BORDER)
                .setPaddingTop(8)
                .setPaddingBottom(8);

        cell.setBorderTop(topBorder ? new SolidBorder(ColorConstants.BLACK, 1) : Border.NO_BORDER);
        return cell;
    }

    private static Cell resumenValueCell(String text, boolean topBorder) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setFontSize(11).setTextAlignment(TextAlignment.RIGHT))
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderBottom(Border.NO_BORDER)
                .setPaddingTop(8)
                .setPaddingBottom(8);

        cell.setBorderTop(topBorder ? new SolidBorder(ColorConstants.BLACK, 1) : Border.NO_BORDER);
        return cell;
    }

    private static Paragraph p(String text, boolean bold, float size, TextAlignment alignment) {
        Paragraph paragraph = new Paragraph(safe(text))
                .setFontSize(size)
                .setTextAlignment(alignment)
                .setMargin(0);

        if (bold) {
            paragraph.simulateBold();
        }
        return paragraph;
    }

    private static BigDecimal calculateTotalBase(List<LineaConcepto> lineas) {
        BigDecimal total = BigDecimal.ZERO;
        if (lineas == null) {
            return total;
        }
        for (LineaConcepto linea : lineas) {
            total = total.add(nvl(linea.baseImponible));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateTotalIva(List<LineaConcepto> lineas) {
        BigDecimal total = BigDecimal.ZERO;
        if (lineas == null) {
            return total;
        }
        for (LineaConcepto linea : lineas) {
            total = total.add(calculateIva(linea));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateIva(LineaConcepto linea) {
        if (linea == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (linea.importeIva != null) {
            return linea.importeIva.setScale(2, RoundingMode.HALF_UP);
        }
        return nvl(linea.baseImponible)
                .multiply(nvl(linea.porcentajeIva))
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String format(BigDecimal value) {
        return DF.format(nvl(value));
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    public static class ReciboData {

        public String entidadEmisora;
        public String codigoRecibo;
        public String fechaGeneracionRecibo;
        public String direccionEmisorLinea1;
        public String direccionEmisorLinea2;
        public String iban;
        public String tipoCalculo;
        public String fechaAlta;
        public String situacionContribuyente;
        public String textoBonificacion;

        public String nombreDestinatario;
        public String dniDestinatario;
        public String referenciaDestinatario;
        public String poblacionDestinatario;

        public int lecturaActual;
        public int lecturaAnterior;
        public int kgGenerados;

        public String tituloRecibo;
        public String logoPath;

        public BigDecimal totalBase;
        public BigDecimal totalIva;
        public BigDecimal totalRecibo;

        public List<LineaConcepto> lineas = new ArrayList<>();
    }

    public static class LineaConcepto {

        public String concepto;
        public String subconcepto;
        public BigDecimal kgIncluidos;
        public BigDecimal baseImponible;
        public BigDecimal porcentajeIva;
        public BigDecimal importeIva;

        public LineaConcepto() {
        }

        public LineaConcepto(String concepto, String subconcepto, BigDecimal kgIncluidos,
                BigDecimal baseImponible, BigDecimal porcentajeIva, BigDecimal importeIva) {
            this.concepto = concepto;
            this.subconcepto = subconcepto;
            this.kgIncluidos = kgIncluidos;
            this.baseImponible = baseImponible;
            this.porcentajeIva = porcentajeIva;
            this.importeIva = importeIva;
        }
    }
    
    public static void generateResumenPdf(
        String destino,
        PeriodoImpositivo periodo,
        BigDecimal totalBase,
        BigDecimal totalIva,
        int numeroRecibos
) throws IOException {
    generateResumenPdf(
            destino,
            periodo == null ? "" : periodo.toString(),
            totalBase,
            totalIva,
            numeroRecibos
    );
}
}
