package com.mycompany.proyecto_si2.processing;

import com.mycompany.proyecto_si2.infra.excel.ExcelManager;
import com.mycompany.proyecto_si2.infra.excel.ExcelColumn;
import com.mycompany.proyecto_si2.domain.service.OrdenanzaManager;
import POJOS.Ordenanza;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;

/**
 * Clase encargada de cargar las ordenanzas desde la hoja correspondiente del fichero Excel.
 * Su función principal es recorrer las filas de datos, leer los valores de cada columna, construir
 * objetos de tipo Ordenanza con esa información y almacenarlos tanto en el gestor de ordenanzas
 * como en una estructura agrupada por identificador.
 *
 * Funciones de la clase:
 *
 * - Prac3OrdenanzaLoader(OrdenanzaManager ordmanager):
 *   Constructor que recibe el gestor de ordenanzas donde se irán registrando las ordenanzas
 *   cargadas desde el Excel.
 *
 * - cargarOrdenanzas(ExcelManager ordenanzas, Map<Integer, List<Ordenanza>> ordenanzasPorId):
 *   Método principal que recorre todas las filas de la hoja de ordenanzas del Excel. Ignora las
 *   filas vacías y aquellas que no tienen identificador de ordenanza, lee el resto de columnas,
 *   asigna valores por defecto cuando algún dato numérico no está informado, crea un objeto Ordenanza
 *   con los datos obtenidos y lo añade tanto al gestor interno como al mapa agrupado por id.
 */

public class Prac3OrdenanzaLoader {

    private final OrdenanzaManager ordmanager;

    public Prac3OrdenanzaLoader(OrdenanzaManager ordmanager) {
        this.ordmanager = ordmanager;
    }

    public void cargarOrdenanzas(ExcelManager ordenanzas, Map<Integer, List<Ordenanza>> ordenanzasPorId) {
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
    }
}