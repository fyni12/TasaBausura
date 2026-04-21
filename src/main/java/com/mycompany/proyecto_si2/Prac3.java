package com.mycompany.proyecto_si2;

import POJOS.Ordenanza;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.poi.ss.usermodel.Row;

public class Prac3 {

    private final Path excelPath;
    private final Path resourcesDir;
    private final EntityManager em;
    private final OrdenanzaManager ordmanager = new OrdenanzaManager();
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public Prac3(Path excelPath, Path resourcesDir, EntityManager em) {
        this.excelPath = excelPath;
        this.resourcesDir = resourcesDir;
        this.em = em;
    }

    public void procesar(String periodo) throws Exception {

        ArrayList<Recibo> recibos = new ArrayList<>();
        List<RegistroBD> registrosBD = new ArrayList<>();
        Set<String> nifVistosBd = new HashSet<>();

        int idrecibo = 0;
        int numeroTotalRecibos = 0;

        BigDecimal totalBasePadron = BigDecimal.ZERO;
        BigDecimal totalIvaPadron = BigDecimal.ZERO;

        Map<Integer, List<Ordenanza>> ordenanzasPorId = new HashMap<>();

        String[] partes = periodo.trim().split(" ");

        String limpio = periodo.trim().toUpperCase();

// Extraer trimestre
        int trimestre = Integer.parseInt(limpio.substring(0, limpio.indexOf('T')));

// Extraer año
        int year = Integer.parseInt(limpio.substring(limpio.indexOf('T') + 1).trim());

        int mesInicio = (trimestre - 1) * 3 + 1;
        LocalDate inicioPeriodo = LocalDate.of(year, mesInicio, 1);
        LocalDate finPeriodo = inicioPeriodo.plusMonths(3).minusDays(1);

        Path recibosDir = resourcesDir.resolve("recibos");
        Files.createDirectories(recibosDir);

        try (ExcelManager excel = new ExcelManager(excelPath, "Contribuyente"); ExcelManager ordenanzas = new ExcelManager(excelPath, "Ordenanza")) {

            for (Row row : ordenanzas.getDataRows()) {
                if (ordenanzas.isEmpty(row)) {
                    continue;
                }

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
                int precioFijo = pfObj != null ? pfObj : 0;

                Integer kgIncObj = ordenanzas.getInt(row, ExcelColumn.KG_INCLUIDOS);
                int kgIncluidos = kgIncObj != null ? kgIncObj : 0;

                Double pKgObj = ordenanzas.getDouble(row, ExcelColumn.PRECIO_KG);
                double precioKg = pKgObj != null ? pKgObj : 0.0;

                Double porcObj = ordenanzas.getDouble(row, ExcelColumn.PORCENTAJE_SOBRE_OTRO_CONCEPTO);
                double porcentajeSobreOtroConcepto = porcObj != null ? porcObj : 0.0;

                Integer sqcObj = ordenanzas.getInt(row, ExcelColumn.SOBRE_QUE_CONCEPTO);
                int sobreQueConcepto = sqcObj != null ? sqcObj : 0;

                Double ivaObj = ordenanzas.getDouble(row, ExcelColumn.IVA);
                double iva = ivaObj != null ? ivaObj : 0.0;

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
                if (excel.isEmpty(row)) {
                    continue;
                }

                LocalDate fechaAlta = excel.getDate(row, ExcelColumn.FECHA_ALTA);
                LocalDate fechaBaja = excel.getDate(row, ExcelColumn.FECHA_BAJA);

                if (fechaAlta == null || fechaAlta.isAfter(finPeriodo)) {
                    continue;
                }

                if (fechaBaja != null && fechaBaja.isBefore(inicioPeriodo)) {
                    continue;
                }

                BigDecimal baseImponible = BigDecimal.ZERO;
                BigDecimal iva = BigDecimal.ZERO;

                String exentoStr = excel.getString(row, ExcelColumn.EXENCION);
                char exento = (exentoStr != null && !exentoStr.isBlank())
                        ? exentoStr.toLowerCase().charAt(0) : 'n';

                Integer kgObj = excel.getInt(row, ExcelColumn.KG_GENERADOS);
                int kgGenerados = kgObj != null ? kgObj : 0;

                Integer boniObj = excel.getInt(row, ExcelColumn.BONIFICACION);
                int bonificacion = boniObj != null ? boniObj : 0;

                String nombre = safe(excel.getString(row, ExcelColumn.NOMBRE));
                String apellido1 = safe(excel.getString(row, ExcelColumn.APELLIDO1));
                String apellido2 = safe(excel.getString(row, ExcelColumn.APELLIDO2));
                String nif = normalizeNif(safe(excel.getString(row, ExcelColumn.NIFNIE)));
                String iban = resolveIban(excel, row);
                String direccion = safe(excel.getString(row, ExcelColumn.DIRECCION));
                String email = safe(excel.getString(row, ExcelColumn.EMAIL));
                String numero = safe(excel.getString(row, ExcelColumn.NUMERO));
                String paisCCC = safe(excel.getString(row, ExcelColumn.PAIS_CCC));
                String ccc = safe(excel.getString(row, ExcelColumn.CCC));

                System.out.println("================================================================================");
                System.out.printf("Contribuyente: %s %s %s, NIF: %s, IBAN: %s, Fecha alta: %s, Exención: %c%n",
                        nombre, apellido1, apellido2, nif, iban, fechaAlta, exento);
                System.out.printf("Fecha del recibo: %s | Fecha del padron: %s%n", LocalDate.now(), inicioPeriodo);
                System.out.printf("Lectura de los kg de basura generados: %d%n", kgGenerados);
                System.out.println("Líneas del recibo:");

                PDFGenerator.ReciboData pdfData = new PDFGenerator.ReciboData();
                pdfData.codigoRecibo = "REC-" + String.format("%05d", idrecibo + 1);
                pdfData.fechaGeneracionRecibo = LocalDate.now().format(DF);
                pdfData.fechaAlta = fechaAlta.format(DF);
                pdfData.iban = iban;
                pdfData.nombreDestinatario = (nombre + " " + apellido1 + " " + apellido2).trim();
                pdfData.dniDestinatario = nif;
                pdfData.referenciaDestinatario = "TRIMESTRE " + trimestre + "/" + year;
                pdfData.lecturaActual = kgGenerados;
                pdfData.lecturaAnterior = 0;
                pdfData.kgGenerados = kgGenerados;
                pdfData.tituloRecibo = "Recibo basura: " + ordinalTrimestre(trimestre) + " trimestre de " + year;
                pdfData.situacionContribuyente = (exento == 's')
                        ? "Contribuyente con exención"
                        : "Contribuyente sin exención";
                pdfData.textoBonificacion = (bonificacion > 0)
                        ? "Recibo con bonificación del " + bonificacion + "%."
                        : "Recibo sin bonificación.";

                String conceptosRaw = excel.getString(row, ExcelColumn.CONCEPTOS_A_COBRAR);
                String[] conceptos = (conceptosRaw == null || conceptosRaw.isBlank())
                        ? new String[0] : conceptosRaw.trim().split("\\s+");

                String puebloPdf = "";
                String tipoCalculoPdf = "Ordinario";
                List<Integer> idsConceptoRecibo = new ArrayList<>();

                for (String conceptoStr : conceptos) {
                    if (conceptoStr == null || conceptoStr.isBlank()) {
                        continue;
                    }

                    int idConcepto;
                    try {
                        idConcepto = Integer.parseInt(conceptoStr.trim());
                    } catch (NumberFormatException e) {
                        continue;
                    }

                    idsConceptoRecibo.add(idConcepto);
                    List<Ordenanza> aplicables = ordenanzasPorId.getOrDefault(idConcepto, List.of());

                    if (!aplicables.isEmpty()) {
                        if (puebloPdf.isBlank()) {
                            puebloPdf = safe(aplicables.get(0).getPueblo());
                        }
                        if (aplicables.get(0).getTipoCalculo() != null
                                && !aplicables.get(0).getTipoCalculo().isBlank()) {
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
                                idConcepto,
                                baseLineaConcepto.doubleValue(),
                                porcentajeIva,
                                ivaLineaConcepto.doubleValue(),
                                bonificacion);

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
                if (esAptoParaBD(excel, row, nifVistosBd)) {

                    BigDecimal totalRecibo = baseImponible.add(iva);
                    totalBasePadron = totalBasePadron.add(baseImponible);
                    totalIvaPadron = totalIvaPadron.add(iva);

                    pdfData.totalBase = baseImponible;
                    pdfData.totalIva = iva;
                    pdfData.totalRecibo = totalRecibo;

                    System.out.printf("Tipo calculo: %s | Total Base Imponible: %.2f€ | Total IVA: %.2f€ | TOTAL RECIBO: %.2f€%n",
                            tipoCalculoPdf,
                            baseImponible.doubleValue(),
                            iva.doubleValue(),
                            totalRecibo.doubleValue());
                    System.out.println("================================================================================\n");

                    String nifParaNombre = nif.isBlank() ? "SIN_NIF" : nif;
                    String nombrePdf = sanitizeFileName(
                            nifParaNombre + "_" + nombre + "_" + apellido1 + "_" + apellido2 + "_T" + trimestre + "_" + year + ".pdf"
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

                    recibos.add(recibo);

                    RegistroBD reg = new RegistroBD();
                    reg.numeroReciboTemporal = idrecibo;
                    reg.trimestre = trimestre;
                    reg.anio = year;

                    reg.nif = nif;
                    reg.nombre = nombre;
                    reg.apellido1 = apellido1;
                    reg.apellido2 = apellido2;
                    reg.direccion = direccion;
                    reg.ayuntamiento = puebloPdf;
                    reg.iban = iban;
                    reg.exencion = Character.toString(exento);
                    reg.email = email;
                    reg.idContribuyente = null;

                    reg.bonificacion = bonificacion;
                    reg.kgGenerados = kgGenerados;

                    reg.fechaAlta = fechaAlta;
                    reg.fechaPadron = inicioPeriodo;
                    reg.fechaRecibo = LocalDate.now();

                    reg.baseImponible = baseImponible;
                    reg.iva = iva;
                    reg.totalRecibo = totalRecibo;

                    reg.idsConcepto.addAll(idsConceptoRecibo);
                    reg.lineas.addAll(copiarLineas(pdfData.lineas));
                    reg.numero = numero;
                    reg.paisCCC = paisCCC;
                    reg.ccc = ccc;
                    reg.fechaBaja = fechaBaja;

                    registrosBD.add(reg);

                    idrecibo++;
                    numeroTotalRecibos++;
                }

            }

            Path xmlPath = resourcesDir.resolve("Recibos.xml");
            XmlManager.escribirRecibos(
                    xmlPath,
                    periodo,
                    totalBasePadron.add(totalIvaPadron).doubleValue(),
                    numeroTotalRecibos,
                    recibos
            );

            PDFGenerator.generateResumenPdf(
                    recibosDir.resolve("resumen.pdf").toString(),
                    periodo,
                    totalBasePadron,
                    totalIvaPadron,
                    numeroTotalRecibos
            );

            persistirRegistrosBD(registrosBD, ordenanzasPorId);

            System.out.println("Fichero Recibos.xml generado correctamente con " + recibos.size() + " recibos.");
        }
    }

    private boolean esAptoParaBD(ExcelManager excel, Row row, Set<String> nifVistosBd) {
        String nifOriginal = excel.getString(row, ExcelColumn.NIFNIE);
        String paisCCC = excel.getString(row, ExcelColumn.PAIS_CCC);
        String cccOriginal = excel.getString(row, ExcelColumn.CCC);

        NifUtils.Resultado nifResultado = NifUtils.validar(nifOriginal);
        if (nifResultado.getEstado() == NifUtils.Estado.BLANCO) {
            return false;
        }
        if (nifResultado.getEstado() == NifUtils.Estado.ERRONEO) {
            return false;
        }

        String nifFinal = normalizeNif(nifOriginal);
        if (nifFinal.isBlank()) {
            return false;
        }

        if (nifVistosBd.contains(nifFinal)) {
            return false;
        }

        CCCUtils.Resultado cccResultado = CCCUtils.validarYCorregir(cccOriginal, paisCCC);
        if (cccResultado.getEstado() == CCCUtils.Estado.ERRONEO) {
            return false;
        }

        nifVistosBd.add(nifFinal);
        return true;
    }

    private String normalizeNif(String nif) {
        NifUtils.Resultado res = NifUtils.validar(nif);
        String fin = safe(res.getValorFinal());
        return fin.isBlank() ? safe(nif) : fin;
    }

    private String resolveIban(ExcelManager excel, Row row) {
        String ibanExcel = safe(excel.getString(row, ExcelColumn.IBAN));
        String paisCCC = excel.getString(row, ExcelColumn.PAIS_CCC);
        String ccc = excel.getString(row, ExcelColumn.CCC);

        CCCUtils.Resultado res = CCCUtils.validarYCorregir(ccc, paisCCC);
        if (res.getEstado() != CCCUtils.Estado.ERRONEO && res.getIban() != null && !res.getIban().isBlank()) {
            return res.getIban();
        }
        return ibanExcel;
    }

    private List<PDFGenerator.LineaConcepto> copiarLineas(List<PDFGenerator.LineaConcepto> origen) {
        List<PDFGenerator.LineaConcepto> copia = new ArrayList<>();
        for (PDFGenerator.LineaConcepto l : origen) {
            PDFGenerator.LineaConcepto x = new PDFGenerator.LineaConcepto();
            x.concepto = l.concepto;
            x.subconcepto = l.subconcepto;
            x.kgIncluidos = l.kgIncluidos;
            x.baseImponible = l.baseImponible;
            x.porcentajeIva = l.porcentajeIva;
            x.importeIva = l.importeIva;
            copia.add(x);
        }
        return copia;
    }

    private void persistirRegistrosBD(List<RegistroBD> registros, Map<Integer, List<Ordenanza>> ordenanzasPorId) {
        if (registros.isEmpty()) {
            return;
        }

        em.getTransaction().begin();
        try {
            Set<String> ordenanzasPersistidas = new HashSet<>();

            Set<String> contribuyentesProcesados = new HashSet<>();

            for (RegistroBD r : registros) {
                String keyContribuyente = safe(r.nombre) + "|" + safe(r.nif) + "|" + r.fechaAlta;

                if (contribuyentesProcesados.add(keyContribuyente)) {
                    upsertContribuyente(r);
                }

                r.idContribuyente = obtenerIdContribuyenteBD(r.nombre, r.nif, r.fechaAlta);
                upsertLectura(r);

                for (Integer idConcepto : r.idsConcepto) {
                    List<Ordenanza> lista = ordenanzasPorId.getOrDefault(idConcepto, List.of());
                    for (Ordenanza ord : lista) {
                        String key = ord.getIdOrdenanza() + "|" + safe(ord.getConcepto()) + "|" + safe(ord.getSubconcepto());
                        if (ordenanzasPersistidas.add(key)) {
                            upsertOrdenanza(ord);
                        }
                    }
                }

                int numeroReciboReal = upsertRecibo(r);
                upsertLineasRecibo(numeroReciboReal, r.lineas, r.bonificacion);
                if (r.idContribuyente != null && !r.idContribuyente.isBlank()) {
                    //Integer idContribuyente = Integer.valueOf(r.idContribuyente);

                    if (r.idContribuyente != null && !r.idContribuyente.isBlank()) {
                        Integer idContribuyente = Integer.valueOf(r.idContribuyente);

                        for (Integer idConcepto : r.idsConcepto) {
                            List<Ordenanza> lista = ordenanzasPorId.getOrDefault(idConcepto, List.of());

                            for (Ordenanza ord : lista) {
                                Integer idOrdenanzaBd = obtenerIdOrdenanzaBD(ord);
                                upsertRelContribuyenteOrdenanza(idContribuyente, idOrdenanzaBd);
                            }
                        }
                    }
                }
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    private void upsertContribuyente(RegistroBD r) {
        Query q = em.createNativeQuery(
                "SELECT idContribuyente "
                + "FROM contribuyente "
                + "WHERE nombre = ? AND NIFNIE = ? AND fechaAlta = ?"
        );
        q.setParameter(1, safe(r.nombre));
        q.setParameter(2, safe(r.nif));
        q.setParameter(3, Date.valueOf(r.fechaAlta));

        List<?> res = q.getResultList();

        if (!res.isEmpty()) {
            Object id = res.get(0);

            Query update = em.createNativeQuery(
                    "UPDATE contribuyente "
                    + "SET apellido1 = ?, "
                    + "apellido2 = ?, "
                    + "direccion = ?, "
                    + "numero = ?, "
                    + "paisCCC = ?, "
                    + "CCC = ?, "
                    + "IBAN = ?, "
                    + "eEmail = ?, "
                    + "exencion = ?, "
                    + "bonificacion = ?, "
                    + "fechaBaja = ? "
                    + "WHERE idContribuyente = ?"
            );
            update.setParameter(1, emptyToNull(r.apellido1));
            update.setParameter(2, emptyToNull(r.apellido2));
            update.setParameter(3, emptyToNull(r.direccion));
            update.setParameter(4, emptyToNull(r.numero));
            update.setParameter(5, emptyToNull(r.paisCCC));
            update.setParameter(6, emptyToNull(r.ccc));
            update.setParameter(7, emptyToNull(r.iban));
            update.setParameter(8, emptyToNull(r.email));
            update.setParameter(9, charOrNull(r.exencion));
            update.setParameter(10, bonificacionOrNull(r.bonificacion));
            update.setParameter(11, r.fechaBaja != null ? Date.valueOf(r.fechaBaja) : null);
            update.setParameter(12, id);

            update.executeUpdate();
        } else {
            Query insert = em.createNativeQuery(
                    "INSERT INTO contribuyente "
                    + "(nombre, apellido1, apellido2, NIFNIE, direccion, numero, paisCCC, CCC, IBAN, eEmail, fechaAlta, fechaBaja, exencion, bonificacion) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            insert.setParameter(1, safe(r.nombre));
            insert.setParameter(2, emptyToNull(r.apellido1));
            insert.setParameter(3, emptyToNull(r.apellido2));
            insert.setParameter(4, safe(r.nif));
            insert.setParameter(5, emptyToNull(r.direccion));
            insert.setParameter(6, emptyToNull(r.numero));
            insert.setParameter(7, emptyToNull(r.paisCCC));
            insert.setParameter(8, emptyToNull(r.ccc));
            insert.setParameter(9, emptyToNull(r.iban));
            insert.setParameter(10, emptyToNull(r.email));
            insert.setParameter(11, Date.valueOf(r.fechaAlta));
            insert.setParameter(12, r.fechaBaja != null ? Date.valueOf(r.fechaBaja) : null);
            insert.setParameter(13, charOrNull(r.exencion));
            insert.setParameter(14, bonificacionOrNull(r.bonificacion));

            insert.executeUpdate();
        }
    }

    private Character charOrNull(String s) {
        String v = safe(s);
        return v.isBlank() ? null : v.charAt(0);
    }

    private Double bonificacionOrNull(int bonificacion) {
        return bonificacion > 0 ? Double.valueOf(bonificacion) : null;
    }

    private void upsertLectura(RegistroBD r) {
        if (r.idContribuyente == null || r.idContribuyente.isBlank()) {
            return;
        }

        String ejercicio = String.valueOf(r.anio);
        String periodo = String.valueOf(r.trimestre);

        Query q = em.createNativeQuery(
                "SELECT COUNT(*) FROM lecturas WHERE idContribuyente = ? AND ejercicio = ? AND periodo = ?"
        );
        q.setParameter(1, r.idContribuyente);
        q.setParameter(2, ejercicio);
        q.setParameter(3, periodo);

        Number n = (Number) q.getSingleResult();

        if (n.intValue() > 0) {
            Query update = em.createNativeQuery(
                    "UPDATE lecturas SET kgGenerados = ? "
                    + "WHERE idContribuyente = ? AND ejercicio = ? AND periodo = ?"
            );
            update.setParameter(1, r.kgGenerados);
            update.setParameter(2, r.idContribuyente);
            update.setParameter(3, ejercicio);
            update.setParameter(4, periodo);
            update.executeUpdate();
        } else {
            Query insert = em.createNativeQuery(
                    "INSERT INTO lecturas (ejercicio, periodo, kgGenerados, idContribuyente) "
                    + "VALUES (?, ?, ?, ?)"
            );
            insert.setParameter(1, ejercicio);
            insert.setParameter(2, periodo);
            insert.setParameter(3, r.kgGenerados);
            insert.setParameter(4, r.idContribuyente);
            insert.executeUpdate();
        }
    }

    private void upsertOrdenanza(Ordenanza ord) {
        Query q = em.createNativeQuery(
                "SELECT COUNT(*) FROM ordenanza WHERE idOrdenanza = ? AND concepto = ? AND subconcepto = ?"
        );
        q.setParameter(1, ord.getIdOrdenanza());
        q.setParameter(2, ord.getConcepto());
        q.setParameter(3, ord.getSubconcepto());

        Number n = (Number) q.getSingleResult();

        if (n.intValue() > 0) {
            Query update = em.createNativeQuery(
                    "UPDATE ordenanza "
                    + "SET descripcion = ?, pueblo = ?, tipoCalculo = ?, acumulable = ?, precioFijo = ?, "
                    + "kgIncluidos = ?, precioKg = ?, porcentaje = ?, conceptoRelacionado = ?, iva = ? "
                    + "WHERE idOrdenanza = ? AND concepto = ? AND subconcepto = ?"
            );
            update.setParameter(1, ord.getDescripcion());
            update.setParameter(2, ord.getPueblo());
            update.setParameter(3, ord.getTipoCalculo());
            update.setParameter(4, ord.getAcumulable());
            update.setParameter(5, ord.getPrecioFijo());
            update.setParameter(6, ord.getKgincluidos());
            update.setParameter(7, ord.getPreciokg());
            update.setParameter(8, ord.getPorcentaje());
            update.setParameter(9, ord.getConceptoRelacionado());
            update.setParameter(10, ord.getIva());
            update.setParameter(11, ord.getIdOrdenanza());
            update.setParameter(12, ord.getConcepto());
            update.setParameter(13, ord.getSubconcepto());
            update.executeUpdate();
        } else {
            Query insert = em.createNativeQuery(
                    "INSERT INTO ordenanza "
                    + "(idOrdenanza, concepto, subconcepto, descripcion, pueblo, tipoCalculo, acumulable, "
                    + "precioFijo, kgIncluidos, precioKg, porcentaje, conceptoRelacionado, iva) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            insert.setParameter(1, ord.getIdOrdenanza());
            insert.setParameter(2, ord.getConcepto());
            insert.setParameter(3, ord.getSubconcepto());
            insert.setParameter(4, ord.getDescripcion());
            insert.setParameter(5, ord.getPueblo());
            insert.setParameter(6, ord.getTipoCalculo());
            insert.setParameter(7, ord.getAcumulable());
            insert.setParameter(8, ord.getPrecioFijo());
            insert.setParameter(9, ord.getKgincluidos());
            insert.setParameter(10, ord.getPreciokg());
            insert.setParameter(11, ord.getPorcentaje());
            insert.setParameter(12, ord.getConceptoRelacionado());
            insert.setParameter(13, ord.getIva());
            insert.executeUpdate();
        }
    }

    private int upsertRecibo(RegistroBD r) {
        Query q = em.createNativeQuery(
                "SELECT numeroRecibo FROM recibos "
                + "WHERE nifContribuyente = ? AND QUARTER(fechaPadron) = ? AND YEAR(fechaPadron) = ?"
        );
        q.setParameter(1, r.nif);
        q.setParameter(2, r.trimestre);
        q.setParameter(3, r.anio);

        List<?> resultados = q.getResultList();

        String apellidos = (safe(r.apellido1) + " " + safe(r.apellido2)).trim();
        String direccionCompleta = safe(r.direccion);

        if (!resultados.isEmpty()) {
            Number num = (Number) resultados.get(0);
            int numeroRecibo = num.intValue();

            Query update = em.createNativeQuery(
                    "UPDATE recibos SET "
                    + "direccionCompleta = ?, nombre = ?, apellidos = ?, fechaRecibo = ?, kgGenerados = ?, "
                    + "fechaPadron = ?, totalBaseImponible = ?, totalIVA = ?, totalRecibo = ?, "
                    + "IBAN = ?, email = ?, exencion = ?, idContribuyente = ? "
                    + "WHERE numeroRecibo = ?"
            );
            update.setParameter(1, direccionCompleta);
            update.setParameter(2, r.nombre);
            update.setParameter(3, apellidos);
            update.setParameter(4, Date.valueOf(r.fechaRecibo));
            update.setParameter(5, r.kgGenerados);
            update.setParameter(6, Date.valueOf(r.fechaPadron));
            update.setParameter(7, r.baseImponible.doubleValue());
            update.setParameter(8, r.iva.doubleValue());
            update.setParameter(9, r.totalRecibo.doubleValue());
            update.setParameter(10, emptyToNull(r.iban));
            update.setParameter(11, emptyToNull(r.email));
            update.setParameter(12, emptyToNull(r.exencion));
            update.setParameter(13, emptyToNull(r.idContribuyente));
            update.setParameter(14, numeroRecibo);
            update.executeUpdate();

            return numeroRecibo;
        } else {
            Query insert = em.createNativeQuery(
                    "INSERT INTO recibos ("
                    + "nifContribuyente, direccionCompleta, nombre, apellidos, fechaRecibo, kgGenerados, "
                    + "fechaPadron, totalBaseImponible, totalIVA, totalRecibo, IBAN, email, exencion, idContribuyente"
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            insert.setParameter(1, r.nif);
            insert.setParameter(2, direccionCompleta);
            insert.setParameter(3, r.nombre);
            insert.setParameter(4, apellidos);
            insert.setParameter(5, Date.valueOf(r.fechaRecibo));
            insert.setParameter(6, r.kgGenerados);
            insert.setParameter(7, Date.valueOf(r.fechaPadron));
            insert.setParameter(8, r.baseImponible.doubleValue());
            insert.setParameter(9, r.iva.doubleValue());
            insert.setParameter(10, r.totalRecibo.doubleValue());
            insert.setParameter(11, emptyToNull(r.iban));
            insert.setParameter(12, emptyToNull(r.email));
            insert.setParameter(13, emptyToNull(r.exencion));
            insert.setParameter(14, emptyToNull(r.idContribuyente));
            insert.executeUpdate();

            Query lastId = em.createNativeQuery("SELECT LAST_INSERT_ID()");
            Number id = (Number) lastId.getSingleResult();
            return id.intValue();
        }
    }

    private void upsertLineasRecibo(int numeroRecibo, List<PDFGenerator.LineaConcepto> lineas, int bonificacion) {
        for (PDFGenerator.LineaConcepto linea : lineas) {
            Query q = em.createNativeQuery(
                    "SELECT id FROM lineasrecibo WHERE idRecibo = ? AND concepto = ? AND subconcepto = ?"
            );
            q.setParameter(1, numeroRecibo);
            q.setParameter(2, linea.concepto);
            q.setParameter(3, linea.subconcepto);

            List<?> existentes = q.getResultList();
            Double importeBonificacion = calcularImporteBonificacion(linea.baseImponible, bonificacion);

            if (!existentes.isEmpty()) {
                int id = ((Number) existentes.get(0)).intValue();

                Query update = em.createNativeQuery(
                        "UPDATE lineasrecibo SET baseImponible = ?, porcentajeIVA = ?, importeiva = ?, "
                        + "kgincluidos = ?, bonificacion = ?, importeBonificacion = ? WHERE id = ?"
                );
                update.setParameter(1, linea.baseImponible.doubleValue());
                update.setParameter(2, linea.porcentajeIva.doubleValue());
                update.setParameter(3, linea.importeIva.doubleValue());
                update.setParameter(4, linea.kgIncluidos.doubleValue());
                update.setParameter(5, bonificacion > 0 ? Double.valueOf(bonificacion) : null);
                update.setParameter(6, importeBonificacion);
                update.setParameter(7, id);
                update.executeUpdate();
            } else {
                Query insert = em.createNativeQuery(
                        "INSERT INTO lineasrecibo "
                        + "(concepto, subconcepto, baseImponible, porcentajeIVA, importeiva, kgincluidos, "
                        + "bonificacion, importeBonificacion, idRecibo) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                );
                insert.setParameter(1, linea.concepto);
                insert.setParameter(2, linea.subconcepto);
                insert.setParameter(3, linea.baseImponible.doubleValue());
                insert.setParameter(4, linea.porcentajeIva.doubleValue());
                insert.setParameter(5, linea.importeIva.doubleValue());
                insert.setParameter(6, linea.kgIncluidos.doubleValue());
                insert.setParameter(7, bonificacion > 0 ? Double.valueOf(bonificacion) : null);
                insert.setParameter(8, importeBonificacion);
                insert.setParameter(9, numeroRecibo);
                insert.executeUpdate();
            }
        }
    }

    private Double calcularImporteBonificacion(BigDecimal baseBonificada, int bonificacion) {
        if (bonificacion <= 0 || bonificacion >= 100 || baseBonificada == null) {
            return null;
        }

        BigDecimal factor = BigDecimal.ONE.subtract(
                BigDecimal.valueOf(bonificacion).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
        );

        if (factor.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        BigDecimal baseOriginal = baseBonificada.divide(factor, 2, RoundingMode.HALF_UP);
        BigDecimal importeBonif = baseOriginal.subtract(baseBonificada).setScale(2, RoundingMode.HALF_UP);
        return importeBonif.doubleValue();
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

    private String unirApellidos(String apellido1, String apellido2) {
        String a1 = safe(apellido1);
        String a2 = safe(apellido2);
        return (a1 + " " + a2).trim();
    }

    private String sanitizeFileName(String s) {
        return s.replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("\\s+", "_");
    }

    private static class RegistroBD {

        int numeroReciboTemporal;
        int trimestre;
        int anio;

        String nif;
        String nombre;
        String apellido1;
        String apellido2;
        String direccion;
        String numero;       // AÑADIR
        String ayuntamiento;
        String iban;
        String paisCCC;      // AÑADIR
        String ccc;          // AÑADIR
        String exencion;
        String email;
        String idContribuyente;

        int bonificacion;
        int kgGenerados;

        LocalDate fechaAlta;
        LocalDate fechaBaja; // AÑADIR
        LocalDate fechaPadron;
        LocalDate fechaRecibo;

        BigDecimal baseImponible;
        BigDecimal iva;
        BigDecimal totalRecibo;

        List<Integer> idsConcepto = new ArrayList<>();
        List<PDFGenerator.LineaConcepto> lineas = new ArrayList<>();
    }

    private String emptyToNull(String s) {
        String v = safe(s);
        return v.isBlank() ? null : v;
    }

    private String obtenerIdContribuyenteBD(String nombre, String nif, LocalDate fechaAlta) {
        Query q = em.createNativeQuery(
                "SELECT idContribuyente FROM contribuyente WHERE nombre = ? AND nifnie = ? AND fechaAlta = ?"
        );
        q.setParameter(1, safe(nombre));
        q.setParameter(2, safe(nif));
        q.setParameter(3, Date.valueOf(fechaAlta));

        List<?> res = q.getResultList();
        if (res.isEmpty()) {
            return null;
        }
        return String.valueOf(res.get(0));
    }

    private void upsertRelContribuyenteOrdenanza(Integer idContribuyente, Integer idOrdenanzaBd) {
        if (idContribuyente == null || idOrdenanzaBd == null) {
            return;
        }

        Query q = em.createNativeQuery(
                "SELECT COUNT(*) FROM rel_contribuyente_ordenanza "
                + "WHERE idContribuyente = ? AND idOrdenanza = ?"
        );
        q.setParameter(1, idContribuyente);
        q.setParameter(2, idOrdenanzaBd);

        Number n = (Number) q.getSingleResult();

        if (n.intValue() == 0) {
            Query insert = em.createNativeQuery(
                    "INSERT INTO rel_contribuyente_ordenanza (idContribuyente, idOrdenanza) "
                    + "VALUES (?, ?)"
            );
            insert.setParameter(1, idContribuyente);
            insert.setParameter(2, idOrdenanzaBd);
            insert.executeUpdate();
        }
    }

    private Integer obtenerIdOrdenanzaBD(Ordenanza ord) {
        Query q = em.createNativeQuery(
                "SELECT id FROM ordenanza "
                + "WHERE idOrdenanza = ? AND concepto = ? AND subconcepto = ?"
        );
        q.setParameter(1, ord.getIdOrdenanza());
        q.setParameter(2, ord.getConcepto());
        q.setParameter(3, ord.getSubconcepto());

        List<?> res = q.getResultList();
        if (res.isEmpty()) {
            return null;
        }
        return ((Number) res.get(0)).intValue();
    }
}
