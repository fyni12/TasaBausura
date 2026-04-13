package com.mycompany.proyecto_si2;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public final class XmlManager {

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
            if (inc.getApellido2() != null && !inc.getApellido2().trim().isEmpty()) {
                contribuyente.addContent(new Element("SegundoApellido").setText(inc.getApellido2().trim()));
            }
            contribuyente.addContent(new Element("TipoDeError").setText(inc.getTipoError()));
            raiz.addContent(contribuyente);
        }

        write(path, doc);
    }

    public static void escribirErroresCCC(Path path, List<CCCIncidencia> incidencias) throws IOException {
        Element raiz = new Element("cuentas");
        Document doc = new Document(raiz);

        for (CCCIncidencia inc : incidencias) {
            Element cuenta = new Element("cuenta");
            cuenta.setAttribute("id", String.valueOf(inc.getIdFilaExcel()));
            cuenta.addContent(new Element("nombre").setText(nullToEmpty(inc.getNombre())));
            cuenta.addContent(new Element("apellidos").setText(nullToEmpty(inc.getApellidos())));
            cuenta.addContent(new Element("nif_nie").setText(nullToEmpty(inc.getNifNie())));
            cuenta.addContent(new Element("ccc_erroneo").setText(nullToEmpty(inc.getCccErroneo())));

            if (inc.getIbanCorrecto() != null && !inc.getIbanCorrecto().trim().isEmpty()) {
                cuenta.addContent(new Element("iban_correcto").setText(inc.getIbanCorrecto()));
            }

            if (inc.getTipoError() != null && !inc.getTipoError().trim().isEmpty()) {
                cuenta.addContent(new Element("tipo_error").setText(inc.getTipoError()));
            }

            raiz.addContent(cuenta);
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

        Element raiz = new Element("Recibos");
        raiz.setAttribute("fechaPadron", nullToEmpty(fechaPadron));
        raiz.setAttribute("totalPadron", String.format(java.util.Locale.US, "%.2f", totalPadron));
        raiz.setAttribute("numeroTotalRecibos", String.valueOf(numeroTotalRecibos));

        Document doc = new Document(raiz);

        for (Recibo rec : recibos) {
            Element recibo = new Element("Recibo");
            recibo.setAttribute("idRecibo", String.valueOf(rec.getIdRecibo()));

            recibo.addContent(new Element("Exencion").setText(nullToEmpty(rec.getExencion().toUpperCase())));
            recibo.addContent(new Element("idFilaExcel").setText(String.valueOf(rec.getIdFilaExcel())));
            recibo.addContent(new Element("nombre").setText(nullToEmpty(rec.getNombre())));
            recibo.addContent(new Element("primerApellido").setText(nullToEmpty(rec.getPrimerApellido())));
            recibo.addContent(new Element("segundoApellido").setText(nullToEmpty(rec.getSegundoApellido())));
            recibo.addContent(new Element("NIF").setText(nullToEmpty(rec.getNif())));
            recibo.addContent(new Element("IBAN").setText(nullToEmpty(rec.getIban())));
            recibo.addContent(new Element("kgGenerados").setText(String.valueOf(rec.getKgGenerados())));
            recibo.addContent(new Element("baseImponibleRecibo").setText(String.valueOf(rec.getBaseImponibleRecibo())));
            recibo.addContent(new Element("ivaRecibo").setText(String.valueOf(rec.getIvaRecibo())));
            recibo.addContent(new Element("totalRecibo").setText(String.valueOf(rec.getTotalRecibo())));

            raiz.addContent(recibo);
        }

        write(path, doc);
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
}