package com.mycompany.proyecto_si2;

import POJOS.Ordenanza;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;

public class Prac3 {

    private final Path excelPath;
    private final Path resourcesDir;
    private final OrdenanzaManager ordmanager = new OrdenanzaManager();
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public Prac3(Path excelPath, Path resourcesDir) {
        this.excelPath = excelPath;
        this.resourcesDir = resourcesDir;
    }

    public void procesar(String periodo) throws Exception {

        ArrayList<Recibo> recibos = new ArrayList<>();
        int idrecibo = 0;
        int numeroTotalRecibos = 0;

        BigDecimal totalBasePadron = BigDecimal.ZERO;
        BigDecimal totalIvaPadron = BigDecimal.ZERO;

        Map<Integer, List<Ordenanza>> ordenanzasPorId = new HashMap<>();

        String[] partes = periodo.trim().split(" ");
        int trimestre = Integer.parseInt(partes[0].substring(0, 1));
        int year = Integer.parseInt(partes[1]);

        int mesInicio = (trimestre - 1) * 3 + 1;
        LocalDate inicioPeriodo = LocalDate.of(year, mesInicio, 1);
        LocalDate finPeriodo = inicioPeriodo.plusMonths(3).minusDays(1);

        Path recibosDir = resourcesDir.resolve("recibos");
        Files.createDirectories(recibosDir);

        try (ExcelManager excel = new ExcelManager(excelPath, "Contribuyente"); ExcelManager ordenanzas = new ExcelManager(excelPath, "Ordenanza")) {

            for (Row row : ordenanzas.getDataRows()) {
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

                ordmanager.add(ord);
                ordenanzasPorId.computeIfAbsent(id, k -> new ArrayList<>()).add(ord);
            }

            for (Row row : excel.getDataRows()) {

                String email = excel.getString(row, ExcelColumn.EMAIL);
                if (email == null || email.isBlank()) {
                    continue;
                }

                LocalDate fechaAlta = excel.getDate(row, ExcelColumn.FECHA_ALTA);
                LocalDate fechaBaja = excel.getDate(row, ExcelColumn.FECHA_BAJA);

                if (fechaAlta != null && !fechaAlta.isAfter(finPeriodo)) {
                    if (fechaBaja == null || !fechaBaja.isBefore(inicioPeriodo)) {

                        BigDecimal baseImponible = BigDecimal.ZERO;
                        BigDecimal iva = BigDecimal.ZERO;

                        String exentoStr = excel.getString(row, ExcelColumn.EXENCION);
                        char exento = (exentoStr != null && !exentoStr.isBlank())
                                ? exentoStr.toLowerCase().charAt(0) : 'n';

                        Integer kgObj = excel.getInt(row, ExcelColumn.KG_GENERADOS);
                        int kgGenerados = (kgObj != null) ? kgObj : 0;

                        Integer boniObj = excel.getInt(row, ExcelColumn.BONIFICACION);
                        int bonificacion = (boniObj != null) ? boniObj : 0;

                        String nombre = safe(excel.getString(row, ExcelColumn.NOMBRE));
                        String apellido1 = safe(excel.getString(row, ExcelColumn.APELLIDO1));
                        String apellido2 = safe(excel.getString(row, ExcelColumn.APELLIDO2));
                        String nif = safe(excel.getString(row, ExcelColumn.NIFNIE));
                        String iban = safe(excel.getString(row, ExcelColumn.IBAN));

                        System.out.println("================================================================================");
                        System.out.printf("Contribuyente: %s %s %s, NIF: %s, IBAN: %s, Fecha alta: %s, Exención: %c%n",
                                nombre, apellido1, apellido2, nif, iban, fechaAlta, exento);
                        System.out.printf("Fecha del recibo: %s | Fecha del padron: %s%n", LocalDate.now(), inicioPeriodo);
                        System.out.printf("Lectura de los kg de basura generados: %d%n", kgGenerados);
                        System.out.println("Líneas del recibo:");

                        PDFGenerator.ReciboData pdfData = new PDFGenerator.ReciboData();
                        pdfData.codigoRecibo = "REC-" + String.format("%05d", idrecibo + 1);
                        pdfData.fechaGeneracionRecibo = LocalDate.now().format(DF);
                        pdfData.fechaAlta = fechaAlta != null ? fechaAlta.format(DF) : "";
                        pdfData.iban = iban;
                        pdfData.nombreDestinatario = (nombre + " " + apellido1 + " " + apellido2).trim();
                        pdfData.dniDestinatario = nif;
                        pdfData.referenciaDestinatario = "TRIMESTRE " + trimestre + "/" + year;
                        pdfData.lecturaActual = kgGenerados;
                        pdfData.lecturaAnterior = 0;
                        pdfData.kgGenerados = kgGenerados;
                        pdfData.tituloRecibo = "Recibo basura: " + ordinalTrimestre(trimestre) + " trimestre de " + year;
                        pdfData.situacionContribuyente = (exento == 's') ? "Contribuyente con exención" : "Contribuyente sin exención";
                        pdfData.textoBonificacion = (bonificacion > 0)
                                ? "Recibo con bonificación del " + bonificacion + "%."
                                : "Recibo sin bonificación.";

                        String conceptosRaw = excel.getString(row, ExcelColumn.CONCEPTOS_A_COBRAR);
                        String[] conceptos = (conceptosRaw == null || conceptosRaw.isBlank())
                                ? new String[0] : conceptosRaw.trim().split("\\s+");

                        String puebloPdf = "";
                        String tipoCalculoPdf = "Ordinario";

                        for (String conceptoStr : conceptos) {
                            int idConcepto = Integer.parseInt(conceptoStr);
                            List<Ordenanza> aplicables = ordenanzasPorId.getOrDefault(idConcepto, List.of());

                            if (!aplicables.isEmpty()) {
                                if (puebloPdf.isBlank()) {
                                    puebloPdf = safe(aplicables.get(0).getPueblo());
                                }
                                if (aplicables.get(0).getTipoCalculo() != null && !aplicables.get(0).getTipoCalculo().isBlank()) {
                                    tipoCalculoPdf = aplicables.get(0).getTipoCalculo();
                                }
                            }

                            if (exento == 'n') {
                                BigDecimal baseLineaConcepto = bd(ordmanager.calculate(bonificacion, kgGenerados, idConcepto));
                                BigDecimal ivaLineaConcepto = bd(ordmanager.calculateIva(bonificacion, kgGenerados, idConcepto));
                                double porcentajeIva = ordmanager.getIva(idConcepto);

                                baseImponible = baseImponible.add(baseLineaConcepto);
                                iva = iva.add(ivaLineaConcepto);

                                System.out.printf(" -> Concepto ID: %d | Base imp: %.2f € | IVA: %.2f%% | Imp. IVA: %.2f € | Bonific: %d%%%n",
                                        idConcepto, baseLineaConcepto.doubleValue(), porcentajeIva, ivaLineaConcepto.doubleValue(), bonificacion);

                                pdfData.lineas.addAll(buildPdfLineas(idConcepto, aplicables, kgGenerados, bonificacion));
                            }
                        }

                        if (exento == 's') {
                            System.out.println(" -> Contribuyente EXENTO de pago.");
                        }

                        pdfData.entidadEmisora = puebloPdf.isBlank() ? "Ayuntamiento" : puebloPdf;
                        pdfData.direccionEmisorLinea1 = "";
                        pdfData.direccionEmisorLinea2 = puebloPdf;
                        pdfData.tipoCalculo = tipoCalculoPdf;
                        pdfData.poblacionDestinatario = puebloPdf;

                        BigDecimal totalRecibo = baseImponible.add(iva);
                        totalBasePadron = totalBasePadron.add(baseImponible);
                        totalIvaPadron = totalIvaPadron.add(iva);

                        pdfData.totalBase = baseImponible;
                        pdfData.totalIva = iva;
                        pdfData.totalRecibo = totalRecibo;

                        System.out.printf("Tipo calculo: %s | Total Base Imponible: %.2f€ | Total IVA: %.2f€ | TOTAL RECIBO: %.2f€%n",
                                tipoCalculoPdf, baseImponible.doubleValue(), iva.doubleValue(), totalRecibo.doubleValue());
                        System.out.println("================================================================================\n");

                        String nombrePdf = sanitizeFileName(
                                nif + "_" + nombre + "_" + apellido1 + "_" + apellido2 + "_T" + trimestre + "_" + year + ".pdf"
                        );
                        PDFGenerator.generatePdf(recibosDir.resolve(nombrePdf).toString(), pdfData);

                        Recibo recibo = new Recibo();
                        recibo.setBaseImponibleRecibo(baseImponible.doubleValue());
                        recibo.setExencion(Character.toString(exento));
                        recibo.setIban(iban);
                        recibo.setIdFilaExcel(excel.getExcelRowId(row));
                        recibo.setIdRecibo(idrecibo);
                        recibo.setIvaRecibo(iva.doubleValue());
                        recibo.setKgGenerados(kgGenerados);
                        recibo.setNif(nif);
                        recibo.setNombre(nombre);
                        recibo.setPrimerApellido(apellido1);
                        recibo.setSegundoApellido(apellido2);
                        recibo.setTotalRecibo(totalRecibo.doubleValue());

                        idrecibo++;
                        numeroTotalRecibos++;
                        recibos.add(recibo);
                    }
                }
            }

            String fechaPadron = inicioPeriodo.toString();
            Path xmlPath = resourcesDir.resolve("Recibos.xml");
            XmlManager.escribirRecibos(xmlPath, periodo,
                    totalBasePadron.add(totalIvaPadron).doubleValue(),
                    numeroTotalRecibos, recibos);

            PDFGenerator.generateResumenPdf(
                    recibosDir.resolve("resumen.pdf").toString(),
                    periodo,
                    totalBasePadron,
                    totalIvaPadron,
                    numeroTotalRecibos
            );

            System.out.println("Fichero Recibos.xml generado correctamente con " + recibos.size() + " recibos.");
        }
    }

    private List<PDFGenerator.LineaConcepto> buildPdfLineas(int idConcepto, List<Ordenanza> aplicables,
            int kgGenerados, int bonificacion) {
        List<PDFGenerator.LineaConcepto> out = new ArrayList<>();
        if (aplicables == null || aplicables.isEmpty()) {
            return out;
        }

        List<Ordenanza> ordenadas = new ArrayList<>(aplicables);
        ordenadas.sort(Comparator
                .comparing((Ordenanza o) -> o.getPrecioFijo() > 0 ? 0 : 1)
                .thenComparing(o -> normalizarKg(o.getKgincluidos())));

        Ordenanza cabecera = ordenadas.get(0);
        BigDecimal ivaPct = bd(cabecera.getIva());
        BigDecimal factorBonif = BigDecimal.ONE.subtract(
                BigDecimal.valueOf(bonificacion).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
        );

        if (cabecera.getConceptoRelacionado() != 0) {
            BigDecimal base = bd(ordmanager.calculate(bonificacion, kgGenerados, idConcepto));
            out.add(linea(cabecera.getConcepto(),
                    decorateSubconcepto(cabecera.getSubconcepto(), bonificacion),
                    BigDecimal.ZERO, base, ivaPct));
            return out;
        }

        int kgMinIncluido = 0;
        for (Ordenanza ord : ordenadas) {
            if (ord.getPrecioFijo() > 0) {
                kgMinIncluido = Math.max(0, ord.getKgincluidos());
                BigDecimal baseFija = bd(ord.getPrecioFijo()).multiply(factorBonif).setScale(2, RoundingMode.HALF_UP);
                out.add(linea(ord.getConcepto(),
                        decorateSubconcepto(ord.getSubconcepto(), bonificacion),
                        BigDecimal.ZERO, baseFija, ivaPct));
                break;
            }
        }

        List<Ordenanza> tramos = new ArrayList<>();
        for (Ordenanza ord : ordenadas) {
            if (ord.getPreciokg() > 0) {
                tramos.add(ord);
            }
        }

        if (tramos.isEmpty()) {
            return out;
        }

        int exceso = Math.max(0, kgGenerados - kgMinIncluido);
        if (exceso == 0) {
            return out;
        }

        boolean tramoUnico = false;
        for (Ordenanza ord : ordenadas) {
            if (ord.getAcumulable() != null && !ord.getAcumulable().isBlank()) {
                tramoUnico = "s".equalsIgnoreCase(ord.getAcumulable().trim());
                break;
            }
        }

        if (tramoUnico) {
            int pendiente = exceso;
            Ordenanza seleccionada = tramos.get(tramos.size() - 1);

            for (Ordenanza tramo : tramos) {
                int ancho = normalizarKg(tramo.getKgincluidos());
                if (ancho == Integer.MAX_VALUE || pendiente <= ancho) {
                    seleccionada = tramo;
                    break;
                }
                pendiente -= ancho;
            }

            BigDecimal base = bd(seleccionada.getPreciokg())
                    .multiply(BigDecimal.valueOf(kgGenerados))
                    .multiply(factorBonif)
                    .setScale(2, RoundingMode.HALF_UP);

            out.add(linea(seleccionada.getConcepto(),
                    decorateSubconcepto(seleccionada.getSubconcepto(), bonificacion),
                    BigDecimal.valueOf(kgGenerados), base, ivaPct));
            return out;
        }

        int restante = exceso;
        BigDecimal ultimoPrecio = BigDecimal.ZERO;
        Ordenanza ultimoTramo = tramos.get(tramos.size() - 1);

        for (Ordenanza tramo : tramos) {
            if (restante <= 0) {
                break;
            }

            int ancho = normalizarKg(tramo.getKgincluidos());
            int kgEnTramo = (ancho == Integer.MAX_VALUE) ? restante : Math.min(restante, ancho);
            if (kgEnTramo <= 0) {
                continue;
            }

            BigDecimal precioKg = bd(tramo.getPreciokg());
            BigDecimal base = precioKg.multiply(BigDecimal.valueOf(kgEnTramo))
                    .multiply(factorBonif)
                    .setScale(2, RoundingMode.HALF_UP);

            out.add(linea(tramo.getConcepto(),
                    decorateSubconcepto(tramo.getSubconcepto(), bonificacion),
                    BigDecimal.valueOf(kgEnTramo), base, ivaPct));

            restante -= kgEnTramo;
            ultimoPrecio = precioKg;
            ultimoTramo = tramo;
        }

        if (restante > 0) {
            BigDecimal baseExtra = ultimoPrecio.multiply(BigDecimal.valueOf(restante))
                    .multiply(factorBonif)
                    .setScale(2, RoundingMode.HALF_UP);

            out.add(linea(ultimoTramo.getConcepto(),
                    decorateSubconcepto(ultimoTramo.getSubconcepto(), bonificacion),
                    BigDecimal.valueOf(restante), baseExtra, ivaPct));
        }

        return out;
    }

    private PDFGenerator.LineaConcepto linea(String concepto, String subconcepto,
            BigDecimal kg, BigDecimal base, BigDecimal ivaPct) {
        PDFGenerator.LineaConcepto l = new PDFGenerator.LineaConcepto();
        l.concepto = safe(concepto);
        l.subconcepto = safe(subconcepto);
        l.kgIncluidos = kg == null ? BigDecimal.ZERO : kg.setScale(2, RoundingMode.HALF_UP);
        l.baseImponible = base == null ? BigDecimal.ZERO : base.setScale(2, RoundingMode.HALF_UP);
        l.porcentajeIva = ivaPct == null ? BigDecimal.ZERO : ivaPct.setScale(2, RoundingMode.HALF_UP);
        l.importeIva = l.baseImponible.multiply(l.porcentajeIva)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return l;
    }

    private int normalizarKg(Integer kg) {
        if (kg == null || kg <= 0) {
            return Integer.MAX_VALUE;
        }
        return kg;
    }

    private String decorateSubconcepto(String subconcepto, int bonificacion) {
        if (bonificacion > 0) {
            return safe(subconcepto) + " (Bonif. " + bonificacion + "%)";
        }
        return safe(subconcepto);
    }

    private String ordinalTrimestre(int trimestre) {
        return switch (trimestre) {
            case 1 ->
                "Primer";
            case 2 ->
                "Segundo";
            case 3 ->
                "Tercer";
            case 4 ->
                "Cuarto";
            default ->
                "Trimestre";
        };
    }

    private BigDecimal bd(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
    }

    private String safe(String v) {
        return v == null ? "" : v.trim();
    }

    private String sanitizeFileName(String s) {
        return s.replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("\\s+", "_");
    }
}
