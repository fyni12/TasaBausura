/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyecto_si2;

import POJOS.Ordenanza;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.poi.ss.usermodel.Row;

/**
 *
 * @author david
 */
public class Prac3 {

    private final Path excelPath;
    private final Path resourcesDir;
    private OrdenanzaManager ordmanager = new OrdenanzaManager();

    public Prac3(Path excelPath, Path resourcesDir) {

        this.excelPath = excelPath;
        this.resourcesDir = resourcesDir;
    }

    public void procesar() throws Exception {

        ArrayList<Recibo> recibos = new ArrayList();
        int idrecibo = 0;
        try (ExcelManager excel = new ExcelManager(excelPath, "Contribuyente"); ExcelManager ordenanzas = new ExcelManager(excelPath, "Ordenanza")) {
            //cargamos las ordenanzas 
            for (Row row : ordenanzas.getDataRows()) {
                int id = ordenanzas.getInt(row, ExcelColumn.ID_ORDENANZA);
                String concepto = ordenanzas.getString(row, ExcelColumn.CONCEPTO);
                String subconcepto = ordenanzas.getString(row, ExcelColumn.SUBCONCEPTO);
                String descripcion = ordenanzas.getString(row, ExcelColumn.DESCRIPCION);
                String pueblo = ordenanzas.getString(row, ExcelColumn.PUEBLO);
                String tipoCalculo = ordenanzas.getString(row, ExcelColumn.TIPO_CALCULO);
                String acumulable = ordenanzas.getString(row, ExcelColumn.ACUMULABLE);
                int precioFijo = ordenanzas.getInt(row, ExcelColumn.PRECIO_FIJO);
                int kgIncluidos = ordenanzas.getInt(row, ExcelColumn.KG_INCLUIDOS);
                double precioKg = ordenanzas.getDouble(row, ExcelColumn.PRECIO_KG);
                double porcentajeSobreOtroConcepto = ordenanzas.getDouble(row, ExcelColumn.PORCENTAJE_SOBRE_OTRO_CONCEPTO);
                int sobreQueConcepto = ordenanzas.getInt(row, ExcelColumn.SOBRE_QUE_CONCEPTO);
                double iva = ordenanzas.getDouble(row, ExcelColumn.IVA);

                Ordenanza ord = new Ordenanza();
                ord.setIdOrdenanza(id);
                ord.setConcepto(concepto);
                ord.setSubconcepto(subconcepto);
                ord.setDescripcion(descripcion);
                ord.setPueblo(pueblo);
                ord.setTipoCalculo(tipoCalculo);
                ord.setAcumulable(acumulable);
                ord.setPrecioFijo(precioFijo);
                ord.setKgincluidos(kgIncluidos);
                ord.setPreciokg(precioKg);
                ord.setPorcentaje(porcentajeSobreOtroConcepto);
                ord.setConceptoRelacionado(sobreQueConcepto);
                ord.setIva(iva);
            }
            for (Row row : excel.getDataRows()) {
                double baseImponible = 0;
                double iva = 0;
                String email = excel.getString(row, ExcelColumn.EMAIL);
                if (email == null || email.isBlank()) {
                    continue;
                }

                char exento = excel.getString(row, ExcelColumn.EXENCION).toLowerCase().charAt(0);

                if (exento == 'n') {
                    int bonificacion = excel.getInt(row, ExcelColumn.BONIFICACION);

                    String[] conceptos = excel.getString(row, ExcelColumn.CONCEPTOS_A_COBRAR)
                            .trim()
                            .split("\\s+");

                    for (String concepto : conceptos) {
                        baseImponible += ordmanager.calculate(bonificacion, excel.getInt(row, ExcelColumn.KG_GENERADOS), Integer.valueOf(concepto));
                        iva += baseImponible * ordmanager.getIva(Integer.valueOf(concepto));
                    }

                }

                Recibo recibo = new Recibo();
                recibo.setBaseImponibleRecibo(baseImponible);
                recibo.setExencion(Character.toString(exento));
                recibo.setIban(excel.getString(row, ExcelColumn.IBAN));
                recibo.setIdFilaExcel(excel.getExcelRowId(row));
                recibo.setIdRecibo(idrecibo);
                recibo.setIvaRecibo(iva);
                recibo.setKgGenerados(Integer.valueOf(excel.getInt(row, ExcelColumn.KG_GENERADOS)));
                recibo.setNif(excel.getString(row, ExcelColumn.NIFNIE));
                recibo.setNombre(excel.getString(row, ExcelColumn.NOMBRE));
                recibo.setPrimerApellido(excel.getString(row, ExcelColumn.APELLIDO1));
                recibo.setSegundoApellido(excel.getString(row, ExcelColumn.APELLIDO2));
                recibo.setTotalRecibo(baseImponible + iva);
                idrecibo++;
                recibos.add(recibo);
            }

        }

    }

    //solo calcula el importe a añadir de 
    private double tipoFijo(int precio, int iva) {
        return (double) precio + precio * ((double) iva / 100);
    }

}
