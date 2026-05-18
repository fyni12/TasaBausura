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