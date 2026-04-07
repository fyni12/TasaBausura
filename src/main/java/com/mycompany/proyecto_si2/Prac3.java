/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyecto_si2;

import POJOS.Ordenanza;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
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

    public void procesar(String periodo) throws Exception {

        ArrayList<Recibo> recibos = new ArrayList<>();
        int idrecibo = 0;
        int numeroTotalRecibos = 0;

        // Parseamos la fecha introducida
        String[] partes = periodo.trim().split(" ");
        int trimestre = Integer.parseInt(partes[0].substring(0, 1));
        int year = Integer.parseInt(partes[1]);

        // Calcular fechas de inicio y fin del trimestre
        int mesInicio = (trimestre - 1) * 3 + 1;
        LocalDate inicioPeriodo = LocalDate.of(year, mesInicio, 1);
        LocalDate finPeriodo = inicioPeriodo.plusMonths(3).minusDays(1);

        try (ExcelManager excel = new ExcelManager(excelPath, "Contribuyente"); ExcelManager ordenanzas = new ExcelManager(excelPath, "Ordenanza")) {

            // 1. Cargamos las ordenanzas 
            for (Row row : ordenanzas.getDataRows()) {
                // Comprobamos si el ID es nulo para saltar filas vacías al final del excel
                Integer idObj = ordenanzas.getInt(row, ExcelColumn.ID_ORDENANZA);
                if (idObj == null) {
                    continue;
                }
                int id = idObj;

                String concepto = ordenanzas.getString(row, ExcelColumn.CONCEPTO);
                String subconcepto = ordenanzas.getString(row, ExcelColumn.SUBCONCEPTO);
                String descripcion = ordenanzas.getString(row, ExcelColumn.DESCRIPCION);
                String pueblo = ordenanzas.getString(row, ExcelColumn.PUEBLO);
                String tipoCalculo = ordenanzas.getString(row, ExcelColumn.TIPO_CALCULO);
                String acumulable = ordenanzas.getString(row, ExcelColumn.ACUMULABLE);

                // Lectura segura de números: si la celda está vacía (null), ponemos un 0
                Integer pfObj = ordenanzas.getInt(row, ExcelColumn.PRECIO_FIJO);
                int precioFijo = (pfObj != null) ? pfObj : 0;

                Integer kgIncObj = ordenanzas.getInt(row, ExcelColumn.KG_INCLUIDOS);
                int kgIncluidos = (kgIncObj != null) ? kgIncObj : 0;

                Double pKgObj = ordenanzas.getDouble(row, ExcelColumn.PRECIO_KG);
                double precioKg = (pKgObj != null) ? pKgObj : 0.0;

                Double porcObj = ordenanzas.getDouble(row, ExcelColumn.PORCENTAJE_SOBRE_OTRO_CONCEPTO);
                double porcentajeSobreOtroConcepto = (porcObj != null) ? porcObj : 0.0;

                Integer sqcObj = ordenanzas.getInt(row, ExcelColumn.SOBRE_QUE_CONCEPTO);
                int sobreQueConcepto = (sqcObj != null) ? sqcObj : 0;

                Double ivaObj = ordenanzas.getDouble(row, ExcelColumn.IVA);
                double iva = (ivaObj != null) ? ivaObj : 0.0;

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

                //añadimps la ordenanza al manager
                ordmanager.add(ord);
            }

            double totalPadron = 0; // Variable para sumar el total de todos los recibos para el XML

            // 2. Procesamos los contribuyentes
            for (Row row : excel.getDataRows()) {

                String email = excel.getString(row, ExcelColumn.EMAIL);
                // Filtro de validez: si no tiene email, asumimos que tiene error
                if (email == null || email.isBlank()) {
                    continue;
                }

                // Obtenemos las fechas del contribuyente
                LocalDate fechaAlta = excel.getDate(row, ExcelColumn.FECHA_ALTA);
                LocalDate fechaBaja = excel.getDate(row, ExcelColumn.FECHA_BAJA);

                // Filtramos por fecha antes de meterlos en la lista
                // ¿Estaba dado de alta ANTES o DURANTE el fin del periodo?
                if (fechaAlta != null && !fechaAlta.isAfter(finPeriodo)) {
                    // ¿Y NO se dio de baja ANTES de empezar el periodo?
                    if (fechaBaja == null || !fechaBaja.isBefore(inicioPeriodo)) {

                        //Contribuyente activo y valido
                        double baseImponible = 0;
                        double iva = 0;
                        // Aseguramos que no de error si el campo está vacío (ej. null en excel)
                        String exentoStr = excel.getString(row, ExcelColumn.EXENCION);
                        char exento = (exentoStr != null && !exentoStr.isBlank()) ? exentoStr.toLowerCase().charAt(0) : 'n';

                        // Lectura segura de Kg Generados
                        Integer kgObj = excel.getInt(row, ExcelColumn.KG_GENERADOS);
                        int kgGenerados = (kgObj != null) ? kgObj : 0;

                        // Lectura segura de Bonificación
                        Integer boniObj = excel.getInt(row, ExcelColumn.BONIFICACION);
                        int bonificacion = (boniObj != null) ? boniObj : 0;

                        // Variables para la impresión por pantalla
                        String nombre = excel.getString(row, ExcelColumn.NOMBRE);
                        String apellido1 = excel.getString(row, ExcelColumn.APELLIDO1);
                        String apellido2 = excel.getString(row, ExcelColumn.APELLIDO2);
                        String nif = excel.getString(row, ExcelColumn.NIFNIE);
                        String iban = excel.getString(row, ExcelColumn.IBAN);

                        // Imprimimos la cabecera del recibo por consola
                        System.out.println("================================================================================");
                        System.out.printf("Contribuyente: %s %s %s, NIF: %s, IBAN: %s, Fecha alta: %s, Exención: %c\n",
                                nombre, apellido1, apellido2, nif, iban, fechaAlta, exento);
                        System.out.printf("Fecha del recibo: %s | Fecha del padron: %s\n", LocalDate.now(), inicioPeriodo);
                        System.out.printf("Lectura de los kg de basura generados: %d\n", kgGenerados);
                        System.out.println("Líneas del recibo:");

                        if (exento == 'n') {
                            String conceptosRaw = excel.getString(row, ExcelColumn.CONCEPTOS_A_COBRAR);
                            if (conceptosRaw == null || conceptosRaw.isBlank()) {
                                continue;
                            }
                            String[] conceptos = conceptosRaw.trim().split("\\s+");

                            for (String conceptoStr : conceptos) {
                                int idConcepto = Integer.parseInt(conceptoStr);

                                // Cálculo individual por línea de concepto
                                double baseLinea = ordmanager.calculate(bonificacion, kgGenerados, idConcepto);
                                double porcentajeIva = ordmanager.getIva(idConcepto);
                                double ivaLinea = ordmanager.calculateIva(bonificacion, kgGenerados, idConcepto);

                                baseImponible += baseLinea;
                                iva += ivaLinea;

                                System.out.printf(" -> Concepto ID: %d | Base imp: %.2f € | IVA: %.2f%% | Imp. IVA: %.2f € | Bonific: %d%%\n",
                                        idConcepto, baseLinea, porcentajeIva, ivaLinea, bonificacion);
                            }
                        } else {
                            System.out.println("  -> Contribuyente EXENTO de pago.");
                        }

                        double totalRecibo = baseImponible + iva;
                        totalPadron += totalRecibo;

                        System.out.printf("Tipo calculo: Ordinario | Total Base Imponible: %.2f€ | Total IVA: %.2f€ | TOTAL RECIBO: %.2f€\n",
                                baseImponible, iva, totalRecibo);
                        System.out.println("================================================================================\n");

                        //Rellenamos el recibo y lo añadimos a la lista
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
                        numeroTotalRecibos++;
                        recibos.add(recibo);
                    }

                }

            }
            //Generacion del XML
            String fechaPadron = inicioPeriodo.toString();
            Path xmlPath = resourcesDir.resolve("Recibos.xml");
            XmlManager.escribirRecibos(xmlPath, fechaPadron, totalPadron, numeroTotalRecibos, recibos);

            System.out.println("Fichero Recibos.xml generado correctamente con " + recibos.size() + " recibos.");
        }
    }
    //solo calcula el importe a añadir de 

    private double tipoFijo(int precio, int iva) {
        return (double) precio + precio * ((double) iva / 100);
    }

}
