package com.mycompany.proyecto_si2.app;

import com.mycompany.proyecto_si2.cli.ConsolaPeriodoReader;
import com.mycompany.proyecto_si2.domain.model.PeriodoImpositivo;
import com.mycompany.proyecto_si2.cli.Prac3;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public final class ProyectoSi2App {

    private static final String PERSISTENCE_UNIT =
            "com.mycompany_Proyecto_Si2_jar_1.0-SNAPSHOTPU";

    public void run() {
        Path resources = Paths.get("resources");
        Path excel = resources.resolve("SistemasBasura.xlsx");

        EntityManagerFactory emf = null;
        EntityManager em = null;

        try (Scanner scanner = new Scanner(System.in)) {
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
            em = emf.createEntityManager();
            
           vaciarTablas(em);

            PracticaBasuraService service = new PracticaBasuraService(excel, resources);
            service.procesar();

            PeriodoImpositivo periodo = ConsolaPeriodoReader.leer(scanner);

            Prac3 prac3 = new Prac3(excel, resources, em);
            prac3.procesar(periodo);

            System.out.println("Práctica completada correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
            if (emf != null && emf.isOpen()) {
                emf.close();
            }
        }
    }
    
    private static void vaciarTablas(EntityManager em) {
        try {
            em.getTransaction().begin();

            em.createNativeQuery("DELETE FROM lineasrecibo").executeUpdate();
            em.createNativeQuery("DELETE FROM lecturas").executeUpdate();
            em.createNativeQuery("DELETE FROM recibos").executeUpdate();
            em.createNativeQuery("DELETE FROM ordenanza").executeUpdate();
            em.createNativeQuery("DELETE FROM contribuyente").executeUpdate();
            em.createNativeQuery("DELETE FROM rel_contribuyente_ordenanza").executeUpdate();


            em.getTransaction().commit();
            System.out.println("Tablas vaciadas correctamente.");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        }
    }
}