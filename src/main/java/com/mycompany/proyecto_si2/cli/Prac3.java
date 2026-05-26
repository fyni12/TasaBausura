package com.mycompany.proyecto_si2.cli;

import com.mycompany.proyecto_si2.domain.model.Recibo;
import com.mycompany.proyecto_si2.domain.model.PeriodoImpositivo;
import com.mycompany.proyecto_si2.domain.model.RegistroBD;
import POJOS.Ordenanza;
import com.mycompany.proyecto_si2.infra.excel.ExcelManager;
import com.mycompany.proyecto_si2.domain.service.OrdenanzaManager;
import com.mycompany.proyecto_si2.infra.pdf.PDFGenerator;
import com.mycompany.proyecto_si2.infra.persistence.Prac3DatabasePersister;
import com.mycompany.proyecto_si2.processing.Prac3OrdenanzaLoader;
import com.mycompany.proyecto_si2.processing.Prac3RowProcessor;
import com.mycompany.proyecto_si2.infra.xml.XmlManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import org.apache.poi.ss.usermodel.Row;

/**
 * Clase encargada de procesar la información necesaria para generar los recibos de un período impositivo.
 * Su responsabilidad principal es leer los datos del Excel, cargar las ordenanzas, procesar cada contribuyente,
 * calcular los importes acumulados del padrón, generar los ficheros de salida y persistir en base de datos
 * los registros obtenidos. [oracle](https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html)
 *
 * Funciones de la clase:
 *
 * - Prac3(Path excelPath, Path resourcesDir, EntityManager em):
 *   Constructor que inicializa la ruta del fichero Excel de trabajo, el directorio de recursos donde se
 *   guardarán los resultados generados y el EntityManager necesario para las operaciones de persistencia
 *   en base de datos. [users.csc.calpoly](http://users.csc.calpoly.edu/~jdalbey/SWE/Design/WritingJavadoc.html)
 *
 * - procesar(PeriodoImpositivo periodo):
 *   Método principal que ejecuta el flujo completo de la práctica 3 para un período impositivo concreto.
 *   Crea las colecciones de recibos y registros de base de datos, prepara los acumuladores de importes,
 *   obtiene la información temporal del período y asegura la existencia del directorio donde se almacenarán
 *   los recibos PDF. [oracle](https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html)
 *
 *   A continuación, abre las hojas del Excel de contribuyentes y ordenanzas, carga las ordenanzas agrupadas
 *   por identificador y crea el procesador encargado de tratar cada fila del padrón. Recorre todas las filas
 *   del Excel de contribuyentes, procesa cada una de ellas y, si el resultado es válido, añade el recibo
 *   generado, almacena el registro asociado para base de datos y acumula la base imponible, el IVA y el
 *   número total de recibos. [users.csc.calpoly](http://users.csc.calpoly.edu/~jdalbey/SWE/Design/WritingJavadoc.html)
 *
 *   Una vez finalizado el recorrido, calcula el importe total del padrón, genera el fichero XML con todos
 *   los recibos del período, crea un PDF resumen con los importes acumulados y el número de recibos, y
 *   persiste en la base de datos los registros procesados junto con la relación de ordenanzas cargadas.
 *   Finalmente, muestra por consola un mensaje indicando que el fichero de recibos se ha generado
 *   correctamente. [oracle](https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html)
 */

public class Prac3 {

    private final Path excelPath;
    private final Path resourcesDir;
    private final EntityManager em;
    private final OrdenanzaManager ordmanager = new OrdenanzaManager();

    public Prac3(Path excelPath, Path resourcesDir, EntityManager em) {
        this.excelPath = excelPath;
        this.resourcesDir = resourcesDir;
        this.em = em;
    }

    public void procesar(PeriodoImpositivo periodo) throws Exception {
        List<Recibo> recibos = new ArrayList<>();
        List<RegistroBD> registrosBD = new ArrayList<>();

        BigDecimal totalBasePadron = BigDecimal.ZERO;
        BigDecimal totalIvaPadron = BigDecimal.ZERO;

        Map<Integer, List<Ordenanza>> ordenanzasPorId = new HashMap<>();

        int numeroTotalRecibos = 0;

        int trimestre = periodo.getTrimestre();
        int year = periodo.getYear();
        LocalDate inicioPeriodo = periodo.getFechaInicio();
        LocalDate finPeriodo = periodo.getFechaFin();
        String fechaPadronXml = trimestre + "T " + year;

        Path recibosDir = resourcesDir.resolve("recibos");
        Files.createDirectories(recibosDir);

        try (ExcelManager excel = new ExcelManager(excelPath, "Contribuyente");
             ExcelManager ordenanzas = new ExcelManager(excelPath, "Ordenanza")) {

            Prac3OrdenanzaLoader ordenanzaLoader = new Prac3OrdenanzaLoader(ordmanager);
            ordenanzaLoader.cargarOrdenanzas(ordenanzas, ordenanzasPorId);

            Prac3RowProcessor rowProcessor = new Prac3RowProcessor(
                    recibosDir,
                    ordmanager,
                    ordenanzasPorId,
                    trimestre,
                    year,
                    inicioPeriodo,
                    finPeriodo,
                    fechaPadronXml
            );

            for (Row row : excel.getDataRows()) {
                Prac3RowProcessor.Resultado resultado = rowProcessor.procesarFila(row, excel);
                if (resultado == null) {
                    continue;
                }

                recibos.add(resultado.getRecibo());

                if (resultado.getRegistroBD() != null) {
                    registrosBD.add(resultado.getRegistroBD());
                }

                totalBasePadron = totalBasePadron.add(resultado.getBaseImponible()).setScale(2, RoundingMode.HALF_UP);
                totalIvaPadron = totalIvaPadron.add(resultado.getIva()).setScale(2, RoundingMode.HALF_UP);
                numeroTotalRecibos++;
            }

            BigDecimal totalPadron = totalBasePadron
                    .add(totalIvaPadron)
                    .setScale(2, RoundingMode.HALF_UP);

            Path xmlPath = resourcesDir.resolve("Recibos.xml");
            XmlManager.escribirRecibos(
                    xmlPath,
                    fechaPadronXml,
                    totalPadron.doubleValue(),
                    numeroTotalRecibos,
                    recibos
            );

            PDFGenerator.generateResumenPdf(
                    recibosDir.resolve("resumen.pdf").toString(),
                    fechaPadronXml,
                    totalBasePadron,
                    totalIvaPadron,
                    numeroTotalRecibos
            );

            Prac3DatabasePersister databasePersister = new Prac3DatabasePersister(em);
            databasePersister.persistirRegistrosBD(registrosBD, ordenanzasPorId);

            System.out.println("Fichero Recibos.xml generado correctamente con " + recibos.size() + " recibos.");
        }
    }
}