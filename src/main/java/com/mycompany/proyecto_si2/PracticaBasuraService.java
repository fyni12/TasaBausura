package com.mycompany.proyecto_si2;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.poi.ss.usermodel.Row;

public final class PracticaBasuraService {

    private final Path excelPath;
    private final Path resourcesDir;

    public PracticaBasuraService(Path excelPath, Path resourcesDir) {
        this.excelPath = excelPath;
        this.resourcesDir = resourcesDir;
    }

    public void procesar() throws Exception {
        List<NifIncidencia> nifIncidencias = new ArrayList<>();
        List<CCCIncidencia> cccIncidencias = new ArrayList<>();
        Set<String> nifVistos = new HashSet<>();
        EmailGenerator emailGenerator = new EmailGenerator();

        try (ExcelManager excel = new ExcelManager(excelPath, "Contribuyente")) {

            for (Row row : excel.getDataRows()) {
                if (excel.isEmpty(row)) {
                    continue;
                }
                emailGenerator.registrarExistente(excel.getString(row, ExcelColumn.EMAIL));
            }

            for (Row row : excel.getDataRows()) {
                if (excel.isEmpty(row)) {
                    continue;
                }

                int filaExcel = excel.getExcelRowId(row);
                String nombre = excel.getString(row, ExcelColumn.NOMBRE);
                String apellido1 = excel.getString(row, ExcelColumn.APELLIDO1);
                String apellido2 = excel.getString(row, ExcelColumn.APELLIDO2);
                String paisCCC = excel.getString(row, ExcelColumn.PAIS_CCC);

                // 1) NIF/NIE
                String nifOriginal = excel.getString(row, ExcelColumn.NIFNIE);
                NifUtils.Resultado nifResultado = NifUtils.validar(nifOriginal);

                String nifFinal = nifResultado.getValorFinal();
                boolean nifApto = false;

                if (nifResultado.getEstado() == NifUtils.Estado.BLANCO) {
                    nifIncidencias.add(new NifIncidencia(
                            filaExcel, nifOriginal, nombre, apellido1, apellido2, "NIF BLANCO"));
                } else if (nifResultado.getEstado() == NifUtils.Estado.ERRONEO) {
                    nifIncidencias.add(new NifIncidencia(
                            filaExcel, nifOriginal, nombre, apellido1, apellido2, "NIF ERRONEO"));
                } else {
                    if (nifResultado.getEstado() == NifUtils.Estado.SUBSANADO) {
                        excel.setString(row, ExcelColumn.NIFNIE, nifFinal);
                    }

                    if (nifVistos.contains(nifFinal)) {
                        nifIncidencias.add(new NifIncidencia(
                                filaExcel, nifFinal, nombre, apellido1, apellido2, "NIF DUPLICADO"));
                    } else {
                        nifVistos.add(nifFinal);
                        nifApto = true;
                    }
                }

                // 2) CCC: siempre se valida, incluso con NIF erróneo/duplicado/blanco
                String cccOriginal = excel.getString(row, ExcelColumn.CCC);
                CCCUtils.Resultado cccResultado = CCCUtils.validarYCorregir(cccOriginal, paisCCC);

                boolean cccApto = false;

                if (cccResultado.getEstado() == CCCUtils.Estado.ERRONEO) {
                    cccIncidencias.add(new CCCIncidencia(
                            filaExcel,
                            nombre,
                            unirApellidos(apellido1, apellido2),
                            nifFinal != null ? nifFinal : nifOriginal,
                            cccOriginal,
                            null,
                            "IMPOSIBLE GENERAR IBAN"));
                } else {
                    cccApto = true;

                    if (cccResultado.getEstado() == CCCUtils.Estado.SUBSANADO) {
                        excel.setString(row, ExcelColumn.CCC, cccResultado.getCccFinal());

                        cccIncidencias.add(new CCCIncidencia(
                                filaExcel,
                                nombre,
                                unirApellidos(apellido1, apellido2),
                                nifFinal != null ? nifFinal : nifOriginal,
                                cccResultado.getCccOriginal(),
                                cccResultado.getIban(),
                                null));
                    }
                }

                // 3) IBAN solo si NIF válido/subsanable y no duplicado + CCC válido/subsanable
                if (nifApto && cccApto) {
                    excel.setString(row, ExcelColumn.IBAN, cccResultado.getIban());

                    String emailActual = excel.getString(row, ExcelColumn.EMAIL);
                    if (emailActual == null || emailActual.trim().isEmpty()) {
                        String emailNuevo = emailGenerator.generar(nombre, apellido1, apellido2);
                        excel.setString(row, ExcelColumn.EMAIL, emailNuevo);
                    }
                }
            }

            excel.save(resourcesDir.resolve("SistemasBasura.xlsx"));
        }

        XmlManager.escribirErroresNifNie(resourcesDir.resolve("ErroresNifNie.xml"), nifIncidencias);
        XmlManager.escribirErroresCCC(resourcesDir.resolve("ErroresCCC.xml"), cccIncidencias);
    }

    private String unirApellidos(String apellido1, String apellido2) {
        String a1 = apellido1 == null ? "" : apellido1.trim();
        String a2 = apellido2 == null ? "" : apellido2.trim();
        return (a1 + " " + a2).trim();
    }
}