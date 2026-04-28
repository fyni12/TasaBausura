package com.mycompany.proyecto_si2;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public final class XmlManager {

    private static final DecimalFormat DF;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "ES"));
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        DF = new DecimalFormat("0.00", symbols);
    }

    private XmlManager() {
    }

    public static void escribirErroresNifNie(Path path, List<NifIncidencia> incidencias) throws IOException {
        Element raiz = new Element("Contribuyentes");
        Document doc = new Document(raiz);

        for (NifIncidencia inc : incidencias) {
            Element contribuyente = new Element("Contribuyente");
            contribuyente.setAttribute("id", String.valueOf(inc.getIdFilaExcel()));
            contribuyente.addContent(new Element("NIF_NIE").setText(nullToEmpty(inc.getNifNie())));
            contribuyente.addContent(new Element("Nombre").setText(nullToEmpty(inc.getNombre())));
            contribuyente.addContent(new Element("PrimerApellido").setText(nullToEmpty(inc.getApellido1())));
            contribuyente.addContent(new Element("SegundoApellido").setText(nullToEmpty(trimOrEmpty(inc.getApellido2()))));

            if (inc.getTipoError() != null && !inc.getTipoError().trim().isEmpty()) {
                contribuyente.addContent(new Element("TipoDeError").setText(inc.getTipoError().trim()));
            }

            raiz.addContent(contribuyente);
        }

        write(path, doc);
    }

    public static void escribirErroresCCC(Path path, List<CCCIncidencia> incidencias) throws IOException {
        Element raiz = new Element("Cuentas");
        Document doc = new Document(raiz);

        for (CCCIncidencia inc : incidencias) {
            Element cuenta = new Element("Cuenta");
            cuenta.setAttribute("id", String.valueOf(inc.getIdFilaExcel()));
            cuenta.addContent(new Element("Nombre").setText(nullToEmpty(inc.getNombre())));
            cuenta.addContent(new Element("Apellidos").setText(nullToEmpty(inc.getApellidos())));
            cuenta.addContent(new Element("NIFNIE").setText(nullToEmpty(inc.getNifNie())));
            cuenta.addContent(new Element("CCCErroneo").setText(nullToEmpty(inc.getCccErroneo())));

            if (inc.getIbanCorrecto() != null && !inc.getIbanCorrecto().trim().isEmpty()) {
                cuenta.addContent(new Element("IBANCorrecto").setText(inc.getIbanCorrecto().trim()));
            }

            if (inc.getTipoError() != null && !inc.getTipoError().trim().isEmpty()) {
                cuenta.addContent(new Element("TipoError").setText(inc.getTipoError().trim()));
            }

            raiz.addContent(cuenta);
        }

        write(path, doc);
    }

    public static void escribirRecibos(
            Path path,
            PeriodoImpositivo periodo,
            BigDecimal totalPadron,
            int numeroTotalRecibos,
            List<Recibo> recibos
    ) throws IOException {
        escribirRecibos(
                path,
                periodo == null ? "" : periodo.toString(),
                totalPadron,
                numeroTotalRecibos,
                recibos
        );
    }

    public static void escribirRecibos(
            Path path,
            PeriodoImpositivo periodo,
            double totalPadron,
            int numeroTotalRecibos,
            List<Recibo> recibos
    ) throws IOException {
        escribirRecibos(
                path,
                periodo == null ? "" : periodo.toString(),
                BigDecimal.valueOf(totalPadron),
                numeroTotalRecibos,
                recibos
        );
    }

    public static void escribirRecibos(
            Path path,
            String fechaPadron,
            BigDecimal totalPadron,
            int numeroTotalRecibos,
            List<Recibo> recibos
    ) throws IOException {

        Element raiz = new Element("Recibos");
        raiz.setAttribute("fechaPadron", nullToEmpty(fechaPadron));
        raiz.setAttribute("totalPadron", formatDecimal(totalPadron));
        raiz.setAttribute("numeroTotalRecibos", String.valueOf(numeroTotalRecibos));

        Document doc = new Document(raiz);

        for (Recibo rec : recibos) {
            Element recibo = new Element("Recibo");
            recibo.setAttribute("idRecibo", String.valueOf(rec.getIdRecibo()));

            recibo.addContent(new Element("Exencion").setText(nullToEmpty(rec.getExencion()).toUpperCase(Locale.ROOT)));
            recibo.addContent(new Element("idFilaExcel").setText(String.valueOf(rec.getIdFilaExcel())));
            recibo.addContent(new Element("nombre").setText(nullToEmpty(rec.getNombre())));
            recibo.addContent(new Element("primerApellido").setText(nullToEmpty(rec.getPrimerApellido())));
            recibo.addContent(new Element("segundoApellido").setText(nullToEmpty(rec.getSegundoApellido())));
            recibo.addContent(new Element("NIF").setText(nullToEmpty(rec.getNif())));
            recibo.addContent(new Element("IBAN").setText(nullToEmpty(rec.getIban())));
            recibo.addContent(new Element("kgGenerados").setText(String.valueOf(rec.getKgGenerados())));
            recibo.addContent(new Element("baseImponibleRecibo").setText(formatDecimal(rec.getBaseImponibleRecibo())));
            recibo.addContent(new Element("ivaRecibo").setText(formatDecimal(rec.getIvaRecibo())));
            recibo.addContent(new Element("totalRecibo").setText(formatDecimal(rec.getTotalRecibo())));

            raiz.addContent(recibo);
        }

        write(path, doc);
    }

    public static void escribirRecibos(
            Path path,
            String fechaPadron,
            double totalPadron,
            int numeroTotalRecibos,
            List<Recibo> recibos
    ) throws IOException {
        escribirRecibos(
                path,
                fechaPadron,
                BigDecimal.valueOf(totalPadron),
                numeroTotalRecibos,
                recibos
        );
    }

    private static void write(Path path, Document doc) throws IOException {
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        try (OutputStream out = Files.newOutputStream(path)) {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            outputter.output(doc, out);
        }
    }

    private static String nullToEmpty(String text) {
        return text == null ? "" : text;
    }

    private static String trimOrEmpty(String text) {
        return text == null ? "" : text.trim();
    }

    private static String formatDecimal(double value) {
        return formatDecimal(BigDecimal.valueOf(value));
    }

    private static String formatDecimal(BigDecimal value) {
        BigDecimal safeValue = value == null
                ? BigDecimal.ZERO
                : value.setScale(2, RoundingMode.HALF_UP);
        return DF.format(safeValue);
    }
}