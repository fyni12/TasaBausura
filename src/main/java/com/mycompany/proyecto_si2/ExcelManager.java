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
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author Sahira
 */
public class ExcelManager {

    private static ExcelManager instance;

    // Usamos una sola lista para todas las incidencias de NIF (errores, blancos o duplicados)
    private ArrayList<NifIncidencia> nifIncidencias = new ArrayList<>();

    private ArrayList<CCCIncidencia> badCCC = new ArrayList<>();
    private final DataFormatter formatter = new DataFormatter();
    private final Set<String> emailsUsados = new HashSet<>();
    private final Map<String, Integer> repeticionesEmail = new HashMap<>();

    private ExcelManager() {
    }

    public static ExcelManager getInstance() {
        if (instance == null) {
            instance = new ExcelManager();
        } 
        return instance;
    }

    public void processExcel(String excelRoute, String xmlRoute) throws IOException, InvalidFormatException {
        XSSFWorkbook libro = new XSSFWorkbook(new File(excelRoute));
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
    System.out.println("------ INICIANDO LIMPIEZA OPTIMIZADA ------------");

    int rowNumber = hoja.getLastRowNum();
    emailsUsados.clear();
    repeticionesEmail.clear();
    Set<String> nifsVistos = new HashSet<>();
    nifIncidencias.clear(); // Limpiamos la nueva lista unificada
    badCCC.clear();

    for (int i = 1; i <= rowNumber; i++) {
        XSSFRow fila = hoja.getRow(i);
        if (isRowEmpty(fila)) continue;

        // 1. Extracción de datos básicos
        Integer id = getIntCell(fila, 1);
        String nombre = getStringCell(fila, 3);
        String apellido1 = getStringCell(fila, 4);
        String apellido2 = getStringCell(fila, 5);
        String nifnie = getStringCell(fila, 6);
        String paisCCC = getStringCell(fila, 2);
        String ccc = getStringCell(fila, 7);
        String direccion = getStringCell(fila, 0);
        Date fechaAlta = getDateCell(fila, 13);

        // Creamos el objeto base para los XML de errores
        Contribuyente individuo = new Contribuyente(id, nombre, apellido1, apellido2, nifnie, direccion, fechaAlta);

        // 2. VALIDACIÓN DE NIF/NIE
        if (nifnie == null || nifnie.isBlank()) {
            nifIncidencias.add(new NifIncidencia(individuo, "NIF BLANCO"));
            continue; // No se genera nada más para este contribuyente
        }

        try {
            if (!NifUtils.isValidNIF(nifnie)) {
                // Intentamos subsanar
                int numbers = NifUtils.getNumericPart(nifnie);
                String newNifnie = String.format("%d%c", numbers, NifUtils.getLetter(numbers));
                modifyCell(fila, 6, newNifnie);
                nifnie = newNifnie; // Actualizamos para el resto del bucle
                individuo.setNifnie(newNifnie);
            }
        } catch (IllegalArgumentException e) {
            nifIncidencias.add(new NifIncidencia(individuo, "NIF ERRONEO"));
            continue; // Error no subsanable: fin de proceso para esta fila
        }

        // Comprobación de duplicados
        if (nifsVistos.contains(nifnie)) {
            nifIncidencias.add(new NifIncidencia(individuo, "NIF DUPLICADO"));
            continue;
        }
        nifsVistos.add(nifnie);

        // 3. VALIDACIÓN DE CCC e IBAN
        try {
            if (!CCCUtils.isValid(ccc)) {
                String cccCorregido = CCCUtils.fix(ccc);
                String ibanGenerado = CCCUtils.calcularIBAN(cccCorregido, paisCCC);
                
                // Registramos la incidencia (subsanada)
                badCCC.add(new CCCIncidencia(individuo, ccc, ibanGenerado, null));
                
                // Actualizamos Excel y variables
                ccc = cccCorregido;
                modifyCell(fila, 7, ccc);
                modifyCell(fila, 8, ibanGenerado);
            } else {
                String ibanGenerado = CCCUtils.calcularIBAN(ccc, paisCCC);
                modifyCell(fila, 8, ibanGenerado);
            }
        } catch (IllegalArgumentException e) {
            // Error de CCC no subsanable
            badCCC.add(new CCCIncidencia(individuo, ccc, null, "IMPOSIBLE GENERAR IBAN"));
            continue; // Según el guion, si el CCC falla, no se genera el correo
        }

        // 4. GENERACIÓN DE EMAIL (Solo si NIF y CCC son correctos/subsanados)
        String emailActual = getStringCell(fila, 9);
        if (emailActual == null || emailActual.isBlank()) {
            try {
                String nuevoEmail = generarEmail(nombre, apellido1, apellido2);
                modifyCell(fila, 9, nuevoEmail);
            } catch (IllegalArgumentException e) {
                // Error silencioso o log si no se pueden obtener iniciales
            }
        } else {
            // Si ya tiene email, lo registramos para evitar duplicados en los generados
            emailsUsados.add(emailActual.trim().toLowerCase());
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
    // 1. Obtener iniciales y normalizar (quitar tildes, eñes y pasar a minúsculas)
    String base = (inicial(nombre) + inicial(apellido1) + inicial(apellido2))
                  .toLowerCase(Locale.ROOT);
    
    base = normalizar(base);

    if (base.isEmpty()) {
        throw new IllegalArgumentException("No hay datos suficientes para generar iniciales de email");
    }

    // 2. Control de repeticiones para el sufijo numérico
    // Si la base no existe, empieza en 0. Si existe, recupera el siguiente número.
    int contador = repeticionesEmail.getOrDefault(base, 0);
    String emailGenerado;

    // 3. Bucle para asegurar que el email sea único
    do {
        // Formato: %02d asegura que el número tenga siempre 2 dígitos (00, 01, 02...)
        emailGenerado = String.format("%s%02d@tasabasura2026.com", base, contador);
        contador++;
    } while (emailsUsados.contains(emailGenerado));

    // 4. Actualizar los mapas para la siguiente vez que aparezca esta base
    repeticionesEmail.put(base, contador);
    emailsUsados.add(emailGenerado);

    return emailGenerado;
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

    // Recorremos la lista única que centraliza todos los tipos de errores de DNI
    for (NifIncidencia inc : nifIncidencias) {
        // Usamos el tipo de error almacenado individualmente en cada incidencia
        direc.buildNifNieRegister(inc.getContribuyente(), inc.getTipoError());
    }

    return builder.getDoc();
}

    private Document GenerateCCCDoc(Director direc, BuilderInterface builder) {
        direc.setBuilder(builder);

        for (CCCIncidencia inc : badCCC) {
            //System.out.println(inc.getContribuyente().getIdContribuyente());
            direc.buildCCCRegister(inc);
        }

        return builder.getDoc();
    }

}
