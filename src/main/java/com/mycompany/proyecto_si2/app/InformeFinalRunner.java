package com.mycompany.proyecto_si2.app;

import com.mycompany.proyecto_si2.domain.service.InformeRecibosQueriesService;
import com.mycompany.proyecto_si2.infra.pdf.InformeRecibosPdfGenerator;
import javax.persistence.EntityManager;

public class InformeFinalRunner {

    private InformeFinalRunner() {
    }

    public static void generar(EntityManager em, String route) {
        try {
            InformeRecibosQueriesService queriesService =
                    new InformeRecibosQueriesService(em);

            String rutaPdf = String.format("%s/informe_final.pdf", route);

            InformeRecibosPdfGenerator.generarPdf(
                    rutaPdf,
                    queriesService.obtenerReciboMaximoInferiorMedia(),
                    queriesService.getHqlReciboMaximoInferiorMedia(),
                    queriesService.obtenerResumenAyuntamientos()
            );

            System.out.println("PDF generado correctamente en: " + rutaPdf);

        } catch (Exception e) {
            throw new RuntimeException("Error generando el informe final en PDF", e);
        }
    }
}