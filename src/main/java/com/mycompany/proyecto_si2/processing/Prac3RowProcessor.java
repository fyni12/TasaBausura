package com.mycompany.proyecto_si2.processing;

import com.mycompany.proyecto_si2.infra.pdf.PDFGenerator;
import com.mycompany.proyecto_si2.infra.excel.ExcelManager;
import com.mycompany.proyecto_si2.infra.excel.ExcelColumn;
import com.mycompany.proyecto_si2.domain.service.OrdenanzaManager;
import com.mycompany.proyecto_si2.domain.model.Recibo;
import com.mycompany.proyecto_si2.domain.model.RegistroBD;
import POJOS.Ordenanza;
import com.mycompany.proyecto_si2.support.ccc.CCCUtils;
import com.mycompany.proyecto_si2.support.nif.NifUtils;
import com.mycompany.proyecto_si2.support.Prac3Support;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.poi.ss.usermodel.Row;
/**
 * Clase encargada de procesar una fila individual del Excel de contribuyentes durante la práctica 3.
 * Su función principal es comprobar si una fila debe generar recibo, calcular los importes asociados,
 * construir los datos necesarios para el PDF, preparar el objeto Recibo para el XML y, cuando procede,
 * generar también el registro que se persistirá en base de datos.
 *
 * Funciones de la clase:
 *
 * - Prac3RowProcessor(Path recibosDir, OrdenanzaManager ordmanager, Map<Integer, List<Ordenanza>> ordenanzasPorId, int trimestre, int year, LocalDate inicioPeriodo, LocalDate finPeriodo, String fechaPadronXml):
 *   Constructor que inicializa el procesador con el directorio de salida de los PDF, el gestor de
 *   ordenanzas, las ordenanzas agrupadas por identificador y toda la información temporal necesaria
 *   para generar los recibos del período impositivo correspondiente.
 *
 * - procesarFila(Row row, ExcelManager excel):
 *   Método principal que procesa una fila concreta del Excel. Primero comprueba si la fila debe generar
 *   recibo XML según su estado y fechas, obtiene los datos del contribuyente y del período, calcula los
 *   importes de base imponible e IVA en función de los conceptos aplicables, prepara la información del
 *   PDF, genera el fichero del recibo en disco, construye el objeto Recibo para exportación XML y, si el
 *   contribuyente es apto para persistencia, crea también el objeto RegistroBD con todos los datos
 *   necesarios para la base de datos. Finalmente, devuelve un objeto Resultado con toda la información
 *   calculada para esa fila.
 *
 * - esAptoParaXML(ExcelManager excel, Row row, Set<String> nifVistosXml):
 *   Método auxiliar privado que comprueba si una fila es apta para formar parte del XML según la presencia
 *   y unicidad del NIF normalizado. Evita incluir registros sin NIF o con NIF duplicado.
 *
 * - debeGenerarReciboXml(Row row, ExcelManager excel, LocalDate inicioPeriodo, LocalDate finPeriodo):
 *   Método auxiliar privado que determina si una fila debe generar recibo para el XML. Verifica que la fila
 *   no esté vacía, que el contribuyente esté activo dentro del período y que disponga de correo electrónico.
 *
 * - debeGenerarRecibo(Row row, ExcelManager excel, LocalDate inicioPeriodo, LocalDate finPeriodo):
 *   Método auxiliar privado que evalúa si una fila tiene todos los datos mínimos necesarios para generar
 *   un recibo completo, comprobando la validez temporal y la existencia de nombre, NIF, conceptos e IBAN.
 *
 * - estaActivoEnPeriodo(LocalDate fechaAlta, LocalDate fechaBaja, LocalDate inicioPeriodo, LocalDate finPeriodo):
 *   Método auxiliar privado que determina si un contribuyente está activo dentro del período impositivo
 *   indicado en función de sus fechas de alta y baja.
 *
 * - esAptoParaBD(ExcelManager excel, Row row, Set<String> nifVistosBd):
 *   Método auxiliar privado que comprueba si una fila puede generar un registro válido para base de datos.
 *   Valida el NIF/NIE, descarta registros en blanco y evita persistir contribuyentes con NIF duplicado.
 *
 * - normalizeNif(String nif):
 *   Método auxiliar privado que valida y normaliza un NIF/NIE, devolviendo la versión corregida cuando
 *   existe o el valor original cuando no puede corregirse.
 *
 * - resolveIban(ExcelManager excel, Row row):
 *   Método auxiliar privado que obtiene el IBAN de una fila. Si puede recalcularlo correctamente a partir
 *   del CCC y del país, utiliza ese valor; en caso contrario, conserva el IBAN ya presente en el Excel.
 *
 * Clase interna:
 *
 * - Resultado:
 *   Clase contenedora utilizada para devolver el resultado del procesamiento de una fila. Agrupa el objeto
 *   Recibo generado, el posible RegistroBD asociado y los importes de base imponible e IVA calculados.
 *
 *   Funciones de la clase Resultado:
 *
 *   - Resultado(Recibo recibo, RegistroBD registroBD, BigDecimal baseImponible, BigDecimal iva):
 *     Constructor que inicializa todos los datos resultantes del procesamiento de una fila.
 *
 *   - getRecibo():
 *     Devuelve el objeto Recibo generado para la fila procesada.
 *
 *   - getRegistroBD():
 *     Devuelve el objeto RegistroBD generado para persistencia, o null si la fila no es apta para base de datos.
 *
 *   - getBaseImponible():
 *     Devuelve la base imponible total calculada para la fila procesada.
 *
 *   - getIva():
 *     Devuelve el importe total de IVA calculado para la fila procesada.
 */
public class Prac3RowProcessor {

    private final Path recibosDir;
    private final OrdenanzaManager ordmanager;
    private final Map<Integer, List<Ordenanza>> ordenanzasPorId;
    private final int trimestre;
    private final int year;
    private final LocalDate inicioPeriodo;
    private final LocalDate finPeriodo;
    private final String fechaPadronXml;
    private final Set<String> nifVistosBd = new HashSet<>();

    private int idrecibo = 0;

    public Prac3RowProcessor(
            Path recibosDir,
            OrdenanzaManager ordmanager,
            Map<Integer, List<Ordenanza>> ordenanzasPorId,
            int trimestre,
            int year,
            LocalDate inicioPeriodo,
            LocalDate finPeriodo,
            String fechaPadronXml
    ) {
        this.recibosDir = recibosDir;
        this.ordmanager = ordmanager;
        this.ordenanzasPorId = ordenanzasPorId;
        this.trimestre = trimestre;
        this.year = year;
        this.inicioPeriodo = inicioPeriodo;
        this.finPeriodo = finPeriodo;
        this.fechaPadronXml = fechaPadronXml;
    }

    public Resultado procesarFila(Row row, ExcelManager excel) throws Exception {
        if (!debeGenerarReciboXml(row, excel, inicioPeriodo, finPeriodo)) {
            return null;
        }

        int idReciboActual = idrecibo;

        LocalDate fechaAlta = excel.getDate(row, ExcelColumn.FECHA_ALTA);
        LocalDate fechaBaja = excel.getDate(row, ExcelColumn.FECHA_BAJA);

        BigDecimal baseImponible = BigDecimal.ZERO;
        BigDecimal iva = BigDecimal.ZERO;

        String exentoStr = excel.getString(row, ExcelColumn.EXENCION);
        char exento = (exentoStr != null && !exentoStr.isBlank())
                ? Character.toUpperCase(exentoStr.trim().charAt(0)) : 'N';

        Integer kgObj = excel.getInt(row, ExcelColumn.KG_GENERADOS);
        int kgGenerados = kgObj != null ? kgObj : 0;

        Integer boniObj = excel.getInt(row, ExcelColumn.BONIFICACION);
        int bonificacion = boniObj != null ? boniObj : 0;

        String nombre = Prac3Support.safe(excel.getString(row, ExcelColumn.NOMBRE));
        String apellido1 = Prac3Support.safe(excel.getString(row, ExcelColumn.APELLIDO1));
        String apellido2 = Prac3Support.safe(excel.getString(row, ExcelColumn.APELLIDO2));
        String nif = normalizeNif(Prac3Support.safe(excel.getString(row, ExcelColumn.NIFNIE)));
        String iban = resolveIban(excel, row);
        String direccion = Prac3Support.safe(excel.getString(row, ExcelColumn.DIRECCION));
        String email = Prac3Support.safe(excel.getString(row, ExcelColumn.EMAIL));
        String numero = Prac3Support.safe(excel.getString(row, ExcelColumn.NUMERO));
        String paisCCC = Prac3Support.safe(excel.getString(row, ExcelColumn.PAIS_CCC));
        String ccc = Prac3Support.safe(excel.getString(row, ExcelColumn.CCC));

        System.out.println("================================================================================");
        System.out.printf("Contribuyente: %s %s %s, NIF: %s, IBAN: %s, Fecha alta: %s, Exención: %c%n",
                nombre, apellido1, apellido2, nif, iban, fechaAlta, exento);
        System.out.printf("Fecha del recibo: %s | Fecha del padrón: %s%n", LocalDate.now(), fechaPadronXml);
        System.out.printf("Lectura de los kg de basura generados: %d%n", kgGenerados);
        System.out.println("Líneas del recibo:");

        PDFGenerator.ReciboData pdfData = new PDFGenerator.ReciboData();
        pdfData.codigoRecibo = "REC-" + String.format("%05d", idReciboActual + 1);
        pdfData.fechaGeneracionRecibo = LocalDate.now().format(Prac3Support.DF);
        pdfData.fechaAlta = fechaAlta != null ? fechaAlta.format(Prac3Support.DF) : "";
        pdfData.iban = iban;
        pdfData.nombreDestinatario = (nombre + " " + apellido1 + " " + apellido2).trim();
        pdfData.dniDestinatario = nif;
        pdfData.referenciaDestinatario = "TRIMESTRE " + trimestre + "/" + year;
        pdfData.lecturaActual = kgGenerados;
        pdfData.lecturaAnterior = 0;
        pdfData.kgGenerados = kgGenerados;
        pdfData.tituloRecibo = "Recibo basura: " + Prac3Support.ordinalTrimestre(trimestre) + " trimestre de " + year;
        pdfData.situacionContribuyente = (exento == 'S')
                ? "Contribuyente con exención"
                : "Contribuyente sin exención";
        pdfData.textoBonificacion = (bonificacion > 0)
                ? "Recibo con bonificación del " + bonificacion + "%."
                : "Recibo sin bonificación.";

        String conceptosRaw = excel.getString(row, ExcelColumn.CONCEPTOS_A_COBRAR);
        String[] conceptos = (conceptosRaw == null || conceptosRaw.isBlank())
                ? new String[0]
                : conceptosRaw.trim().split("\\s+");

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
                    puebloPdf = Prac3Support.safe(aplicables.get(0).getPueblo());
                }
                if (aplicables.get(0).getTipoCalculo() != null
                        && !aplicables.get(0).getTipoCalculo().isBlank()) {
                    tipoCalculoPdf = aplicables.get(0).getTipoCalculo();
                }
            }

            if (exento == 'N') {
                BigDecimal baseLineaConcepto = Prac3Support.bd(ordmanager.calculate(bonificacion, kgGenerados, idConcepto));
                BigDecimal ivaLineaConcepto = Prac3Support.bd(ordmanager.calculateIva(bonificacion, kgGenerados, idConcepto));
                double porcentajeIva = ordmanager.getIva(idConcepto);

                baseImponible = baseImponible.add(baseLineaConcepto).setScale(2, RoundingMode.HALF_UP);
                iva = iva.add(ivaLineaConcepto).setScale(2, RoundingMode.HALF_UP);

                System.out.printf(
                        " -> Concepto ID: %d | Base imp: %.2f € | IVA: %.2f%% | Imp. IVA: %.2f € | Bonific: %d%%%n",
                        idConcepto,
                        baseLineaConcepto.doubleValue(),
                        porcentajeIva,
                        ivaLineaConcepto.doubleValue(),
                        bonificacion
                );

                pdfData.lineas.addAll(ordmanager.buildPdfLineas(bonificacion, kgGenerados, idConcepto));
            }
        }

        if (exento == 'S') {
            System.out.println(" -> Contribuyente EXENTO de pago.");
        }

        baseImponible = baseImponible.setScale(2, RoundingMode.HALF_UP);
        iva = iva.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalRecibo = baseImponible.add(iva).setScale(2, RoundingMode.HALF_UP);

        pdfData.entidadEmisora = puebloPdf.isBlank() ? "Ayuntamiento" : puebloPdf;
        pdfData.direccionEmisorLinea1 = "";
        pdfData.direccionEmisorLinea2 = puebloPdf;
        pdfData.tipoCalculo = tipoCalculoPdf;
        pdfData.poblacionDestinatario = puebloPdf;
        pdfData.totalBase = baseImponible;
        pdfData.totalIva = iva;
        pdfData.totalRecibo = totalRecibo;

        System.out.printf(
                "Tipo calculo: %s | Total Base Imponible: %.2f€ | Total IVA: %.2f€ | TOTAL RECIBO: %.2f€%n",
                tipoCalculoPdf,
                baseImponible.doubleValue(),
                iva.doubleValue(),
                totalRecibo.doubleValue()
        );
        System.out.println("================================================================================\n");

        String nifParaNombre = nif.isBlank() ? "SIN_NIF" : nif;
        String nombrePdf = Prac3Support.sanitizeFileName(
                nifParaNombre + "_" + nombre + "_" + apellido1 + "_" + apellido2 + "_T" + trimestre + "_" + year + ".pdf"
        );

        PDFGenerator.generatePdf(recibosDir.resolve(nombrePdf).toString(), pdfData);

        Recibo recibo = new Recibo();
        recibo.setBaseImponibleRecibo(Prac3Support.toXmlDouble(baseImponible));
        recibo.setExencion(Character.toString(exento));
        recibo.setIban(iban);
        recibo.setIdFilaExcel(excel.getExcelRowId(row));
        recibo.setIdRecibo(idReciboActual);
        recibo.setIvaRecibo(Prac3Support.toXmlDouble(iva));
        recibo.setKgGenerados(kgGenerados);
        recibo.setNif(nif);
        recibo.setNombre(nombre);
        recibo.setPrimerApellido(apellido1);
        recibo.setSegundoApellido(apellido2);
        recibo.setTotalRecibo(Prac3Support.toXmlDouble(totalRecibo));

        RegistroBD reg = null;
        if (esAptoParaBD(excel, row, nifVistosBd)) {
            reg = new RegistroBD();
            reg.numeroReciboTemporal = idReciboActual;
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
            reg.fechaBaja = fechaBaja;

            reg.baseImponible = baseImponible;
            reg.iva = iva;
            reg.totalRecibo = totalRecibo;

            reg.idsConcepto.addAll(idsConceptoRecibo);
            reg.lineas.addAll(Prac3Support.copiarLineas(pdfData.lineas));

            reg.numero = numero;
            reg.paisCCC = paisCCC;
            reg.ccc = ccc;
        }

        idrecibo++;
        return new Resultado(recibo, reg, baseImponible, iva);
    }

    private boolean esAptoParaXML(ExcelManager excel, Row row, Set<String> nifVistosXml) {
        String nifOriginal = Prac3Support.safe(excel.getString(row, ExcelColumn.NIFNIE));
        if (nifOriginal.isBlank()) {
            return false;
        }

        String nifNormalizado = normalizeNif(nifOriginal).toUpperCase();
        if (nifNormalizado.isBlank()) {
            nifNormalizado = nifOriginal.toUpperCase();
        }

        if (nifVistosXml.contains(nifNormalizado)) {
            return false;
        }

        nifVistosXml.add(nifNormalizado);
        return true;
    }

    private boolean debeGenerarReciboXml(
            Row row,
            ExcelManager excel,
            LocalDate inicioPeriodo,
            LocalDate finPeriodo
    ) {
        if (row == null || excel.isEmpty(row)) {
            return false;
        }

        LocalDate fechaAlta = excel.getDate(row, ExcelColumn.FECHA_ALTA);
        LocalDate fechaBaja = excel.getDate(row, ExcelColumn.FECHA_BAJA);

        if (!estaActivoEnPeriodo(fechaAlta, fechaBaja, inicioPeriodo, finPeriodo)) {
            return false;
        }

        String email = Prac3Support.safe(excel.getString(row, ExcelColumn.EMAIL));
        return !email.isBlank();
    }

    private boolean debeGenerarRecibo(Row row, ExcelManager excel, LocalDate inicioPeriodo, LocalDate finPeriodo) {
        if (row == null || excel.isEmpty(row)) {
            return false;
        }

        LocalDate fechaAlta = excel.getDate(row, ExcelColumn.FECHA_ALTA);
        LocalDate fechaBaja = excel.getDate(row, ExcelColumn.FECHA_BAJA);

        if (!estaActivoEnPeriodo(fechaAlta, fechaBaja, inicioPeriodo, finPeriodo)) {
            return false;
        }

        String nombre = Prac3Support.safe(excel.getString(row, ExcelColumn.NOMBRE));
        String nif = Prac3Support.safe(excel.getString(row, ExcelColumn.NIFNIE));
        String conceptosRaw = Prac3Support.safe(excel.getString(row, ExcelColumn.CONCEPTOS_A_COBRAR));
        String iban = Prac3Support.safe(resolveIban(excel, row));

        if (nombre.isBlank()) {
            return false;
        }

        if (nif.isBlank()) {
            return false;
        }

        if (conceptosRaw.isBlank()) {
            return false;
        }

        if (iban.isBlank()) {
            return false;
        }

        return true;
    }

    private boolean estaActivoEnPeriodo(
            LocalDate fechaAlta,
            LocalDate fechaBaja,
            LocalDate inicioPeriodo,
            LocalDate finPeriodo
    ) {
        if (fechaAlta == null || inicioPeriodo == null || finPeriodo == null) {
            return false;
        }

        if (fechaBaja != null && fechaBaja.isBefore(fechaAlta)) {
            fechaBaja = null;
        }

        boolean altaValida = !fechaAlta.isAfter(finPeriodo);
        boolean bajaValida = (fechaBaja == null) || !fechaBaja.isBefore(inicioPeriodo);

        return altaValida && bajaValida;
    }

    private boolean esAptoParaBD(ExcelManager excel, Row row, Set<String> nifVistosBd) {
        String nifOriginal = excel.getString(row, ExcelColumn.NIFNIE);

        NifUtils.Resultado nifResultado = NifUtils.validar(nifOriginal);

        if (nifResultado.getEstado() == NifUtils.Estado.BLANCO) {
            return false;
        }

        String nifFinal = Prac3Support.safe(nifResultado.getValorFinal());

        if (!nifFinal.isBlank()) {
            if (nifVistosBd.contains(nifFinal)) {
                return false;
            }
            nifVistosBd.add(nifFinal);
        }

        return true;
    }

    private String normalizeNif(String nif) {
        NifUtils.Resultado res = NifUtils.validar(nif);
        String fin = Prac3Support.safe(res.getValorFinal());
        return fin.isBlank() ? Prac3Support.safe(nif) : fin;
    }

    private String resolveIban(ExcelManager excel, Row row) {
        String ibanExcel = Prac3Support.safe(excel.getString(row, ExcelColumn.IBAN));
        String paisCCC = excel.getString(row, ExcelColumn.PAIS_CCC);
        String ccc = excel.getString(row, ExcelColumn.CCC);

        CCCUtils.Resultado res = CCCUtils.validarYCorregir(ccc, paisCCC);
        if (res.getEstado() != CCCUtils.Estado.ERRONEO && res.getIban() != null && !res.getIban().isBlank()) {
            return res.getIban();
        }
        return ibanExcel;
    }

    public static class Resultado {

        private final Recibo recibo;
        private final RegistroBD registroBD;
        private final BigDecimal baseImponible;
        private final BigDecimal iva;

        Resultado(Recibo recibo, RegistroBD registroBD, BigDecimal baseImponible, BigDecimal iva) {
            this.recibo = recibo;
            this.registroBD = registroBD;
            this.baseImponible = baseImponible;
            this.iva = iva;
        }

        public Recibo getRecibo() {
            return recibo;
        }

        public RegistroBD getRegistroBD() {
            return registroBD;
        }

        public BigDecimal getBaseImponible() {
            return baseImponible;
        }

        public BigDecimal getIva() {
            return iva;
        }
    }
}