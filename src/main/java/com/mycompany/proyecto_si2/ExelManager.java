/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyecto_si2;

import POJOS.Contribuyente;
import com.mycompany.proyecto_si2.builder.BuilderInterface;
import com.mycompany.proyecto_si2.builder.CCCBuilder;
import com.mycompany.proyecto_si2.builder.Director;
import com.mycompany.proyecto_si2.builder.NifNieBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author david
 */
public class ExelManager {

    private static ExelManager instance;

    private ArrayList<Contribuyente> contribuyentes = new ArrayList<>();
    private ArrayList<Contribuyente> badNif = new ArrayList<>();
    private ArrayList<Contribuyente> badNifDuplicado = new ArrayList<>();

    private ArrayList<CCCIncidencia> badCCC = new ArrayList<>();
    private final DataFormatter formatter = new DataFormatter();
    private final Set<String> emailsUsados = new HashSet<>();
    private final Map<String, Integer> repeticionesEmail = new HashMap<>();

    private ExelManager() {
    }

    public static ExelManager getInstance() {
        if (instance == null) {
            instance = new ExelManager();
        }
        return instance;
    }

    public void processExel(String exelRoute, String xmlRoute) throws IOException, InvalidFormatException {
        XSSFWorkbook libro = new XSSFWorkbook(new File(exelRoute));
        XSSFSheet hojaContribuyentes = libro.getSheetAt(0);

        Director direc = new Director();
        CCCBuilder cccbuilder = new CCCBuilder("Cuentas");
        NifNieBuilder nifnibuilder = new NifNieBuilder("Contribuyentes");

        purgarContribuyentes(hojaContribuyentes);
        //abrimos libro XSSFWorkBook
        //abrimos las hojas XSSFsheet

        //vamos recorriendo las filas XSSFRow
        try (FileOutputStream fos = new FileOutputStream("src/main/resources/SistemasBasuramod.xlsx")) {
            libro.write(fos);
        }

        Document docNif = GenerateNifnieDoc(direc, nifnibuilder);

        try (FileOutputStream fos = new FileOutputStream(xmlRoute + "ErroresNifNiew.xml")) {
            XMLOutputter xmlOutputter = new XMLOutputter();
            xmlOutputter.setFormat(Format.getPrettyFormat());
            xmlOutputter.output(docNif, fos);
        }

        Document docCCC = GenerateCCCDoc(direc, cccbuilder);

        try (FileOutputStream fos = new FileOutputStream(xmlRoute + "ErroresCCC.xml")) {
            XMLOutputter xmlOutputter = new XMLOutputter();
            xmlOutputter.setFormat(Format.getPrettyFormat());
            xmlOutputter.output(docCCC, fos);
        }

        libro.close();
    }

    private boolean isRowEmpty(XSSFRow fila) {
        if (fila == null) {
            return true;
        }

        for (int c = fila.getFirstCellNum(); c < fila.getLastCellNum(); c++) {
            Cell celda = fila.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (celda != null) {
                return false;
            }
        }

        return true;

    }

    private void purgarContribuyentes(XSSFSheet hoja) {
        System.out.println("------INICIANDO LIMPIEZA------------");

        int rowNumber = hoja.getLastRowNum();

        emailsUsados.clear();
        repeticionesEmail.clear();
        Set<String> nifsVistos = new HashSet<>();

        for (int i = 1; i <= rowNumber; i++) {

            XSSFRow fila = hoja.getRow(i);

            if (!isRowEmpty(fila)) {
                boolean save = true;

                Integer id = getIntCell(fila, 1);
                String nombre = getStringCell(fila, 3);
                String apellido1 = getStringCell(fila, 4);
                String apellido2 = getStringCell(fila, 5);
                String nifnie = getStringCell(fila, 6);
                String direccion = getStringCell(fila, 0);

                String paisCCC = getStringCell(fila, 2);
                String ccc = getStringCell(fila, 7);
                String iban = getStringCell(fila, 8);
                String email = getStringCell(fila, 9);
                if (email != null && !email.isBlank()) {
                    emailsUsados.add(email.trim().toLowerCase(Locale.ROOT));
                }

                String exencion = getStringCell(fila, 10);
                Double bonificacion = getDoubleCell(fila, 11);
                Date fechaAlta = getDateCell(fila, 13);
                Date fechaBaja = getDateCell(fila, 14);

                Contribuyente individuo = new Contribuyente(id, nombre, apellido1, apellido2, nifnie, direccion, fechaAlta);
                individuo.setCcc(ccc);
                individuo.setPaisCCC(paisCCC);
                individuo.setIban(iban);
                individuo.setEEmail(email);
                individuo.setExencion(exencion != null && !exencion.isEmpty() ? exencion.charAt(0) : null);
                individuo.setBonificacion(bonificacion);
                individuo.setFechaBaja(fechaBaja);

                //----------------DNI----------------
                try {
                    if (!NifUtils.isValidNIF(nifnie)) {
                        int numbers = NifUtils.getNumericPart(nifnie);
                        String newNifnie = String.format("%d%c", numbers, NifUtils.getLetter(numbers));
                        this.modifyCell(fila, 6, newNifnie);
                        individuo.setNifnie(newNifnie);
                        nifnie = newNifnie; // actualiza la variable local

                        System.out.println(String.format("cambiando dni de id:%d-> %s -> %s", id, nifnie, newNifnie));
                    }

                } catch (IllegalArgumentException e) {
                    System.out.println(String.format("nifNie mal id: %d -> %s", id, nifnie));
                    badNif.add(individuo);
                    save = false;
                }

                //----------------Comprobación de duplicado----------------
                if (save && nifnie != null) {
                    if (nifsVistos.contains(nifnie)) {
                        System.out.println(String.format("NIF duplicado fila: %d id:%d -> %s", i, id, nifnie));
                        badNifDuplicado.add(individuo);
                        save = false;
                    } else {
                        nifsVistos.add(nifnie);
                    }
                }

                //-----------CCC--------------
                try {
                    if (!CCCUtils.isValid(ccc)) {
                        String cccOriginal = ccc;
                        String cccCorregido = CCCUtils.fix(cccOriginal);
                        String ibanCorrecto = CCCUtils.calcularIBAN(cccCorregido, paisCCC);

                        badCCC.add(new CCCIncidencia(individuo, cccOriginal, ibanCorrecto, null));

                        // sincronizar variables locales
                        ccc = cccCorregido;
                        iban = ibanCorrecto;

                        // sincronizar objeto
                        individuo.setCcc(ccc);
                        individuo.setIban(iban);

                        // sincronizar Excel
                        //modifyCell(fila, 7, ccc);
                        //modifyCell(fila, 8, iban);

                        System.out.println(String.format("CCC corregido de id:%d -> %s", id, ccc));
                        System.out.println(String.format("IBAN generado de id:%d -> %s", id, iban));
                    } else {
                        iban = CCCUtils.calcularIBAN(ccc, paisCCC);
                        individuo.setCcc(ccc);
                        individuo.setIban(iban);

                        modifyCell(fila, 8, iban);

                        System.out.println(String.format("CCC correcto de id:%d -> %s", id, ccc));
                        System.out.println(String.format("IBAN generado de id:%d -> %s", id, iban));
                    }

                } catch (IllegalArgumentException e) {
                    System.out.println(String.format("Error validando CCC de id:%d -> %s", id, ccc));
                    System.out.println("Motivo: " + e.getMessage());

                    badCCC.add(new CCCIncidencia(individuo, ccc, null, "IMPOSIBLE GENERAR IBAN"));
                    save = false;
                }

                if (save) {

                    //-----------EMAIL-----------------
                    if (save && (email == null || email.isBlank())) {
                        try {
                            String nuevoEmail = generarEmail(nombre, apellido1, apellido2);
                            individuo.setEEmail(nuevoEmail);
                            modifyCell(fila, 9, nuevoEmail);

                            System.out.println(String.format("Generado email id:%d -> %s", id, nuevoEmail));
                        } catch (IllegalArgumentException e) {
                            System.out.println(String.format("No se pudo generar email para id:%d", id));
                            System.out.println("Motivo: " + e.getMessage());
                            save = false;
                        }
                    }

                    contribuyentes.add(individuo);
                }
            }
        }
    }

    private void modifyCell(XSSFRow fila, int columna, String valor) {

        if (fila == null) {
            return;
        }

        Cell celda = fila.getCell(columna, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        celda.setCellValue(valor);
    }

    private String getStringCell(XSSFRow fila, int col) {
        Cell celda = fila.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (celda == null) {
            return null;
        }

        String valor = formatter.formatCellValue(celda);
        return valor == null || valor.trim().isEmpty() ? null : valor.trim();
    }

    private Integer getIntCell(XSSFRow fila, int col) {
        String valor = getStringCell(fila, col);
        return (valor == null) ? null : Integer.valueOf(valor);
    }

    private Double getDoubleCell(XSSFRow fila, int col) {
        String valor = getStringCell(fila, col);
        if (valor == null) {
            return null;
        }
        valor = valor.replace(",", ".");
        return Double.valueOf(valor);
    }

    private Date getDateCell(XSSFRow fila, int col) {
        Cell celda = fila.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (celda == null) {
            return null;
        }
        if (celda.getCellType() == CellType.NUMERIC) {
            return celda.getDateCellValue();
        }
        return null;
    }

    private String generarEmail(String nombre, String apellido1, String apellido2) {
        String base
                = inicial(nombre)
                + inicial(apellido1)
                + inicial(apellido2);

        base = normalizar(base).toLowerCase(Locale.ROOT);

        if (base.isBlank()) {
            throw new IllegalArgumentException("No se puede generar email sin iniciales");
        }

        int rep = repeticionesEmail.getOrDefault(base, 0);
        String email;

        do {
            email = String.format("%s%02d@tasabasura2026.com", base, rep);
            rep++;
        } while (emailsUsados.contains(email));

        repeticionesEmail.put(base, rep);
        emailsUsados.add(email);

        return email;
    }

    private String inicial(String s) {
        if (s == null || s.isBlank()) {
            return "";
        }
        return String.valueOf(s.trim().charAt(0));
    }

    private String normalizar(String s) {
        String limpia = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^A-Za-z]", "");
        return limpia;
    }

    private Document GenerateNifnieDoc(Director direc, BuilderInterface builder) {
        direc.setBuilder(builder);

        for (Contribuyente cont : badNif) {
            direc.buildNifNieRegister(cont, "NIF ERRONEO");
        }

        for (Contribuyente cont : badNifDuplicado) {
            direc.buildNifNieRegister(cont, "NIF DUPLICADO");
        }

        return builder.getDoc();
    }

    private Document GenerateCCCDoc(Director direc, BuilderInterface builder) {
        direc.setBuilder(builder);

        for (CCCIncidencia inc : badCCC) {
            System.out.println(inc.getContribuyente().getIdContribuyente());
            direc.buildCCCRegister(inc);
        }

        return builder.getDoc();
    }

}
