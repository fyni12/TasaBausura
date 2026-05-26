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
/**
 * Clase principal de la aplicación encargada de coordinar la ejecución completa del proyecto.
 * Su responsabilidad es preparar las rutas de trabajo, inicializar la conexión con la base de datos,
 * ejecutar el procesamiento del Excel, solicitar al usuario el período impositivo y lanzar la segunda
 * parte del tratamiento de datos. También se encarga de liberar correctamente los recursos al finalizar.
 *
 * Funciones de la clase:
 *
 * - run():
 *   Método principal de ejecución de la aplicación. Define las rutas del directorio de recursos y del
 *   fichero Excel de trabajo, crea el EntityManagerFactory y el EntityManager para acceder a la base
 *   de datos, ejecuta el servicio que procesa y corrige el Excel, solicita por consola el período
 *   impositivo al usuario y lanza el procesamiento posterior asociado a la práctica 3. Finalmente,
 *   muestra un mensaje de finalización correcta y garantiza el cierre de los recursos de persistencia,
 *   incluso si se produce una excepción.
 *
 * - vaciarTablas(EntityManager em):
 *   Método auxiliar estático que elimina todos los registros de varias tablas de la base de datos.
 *   Inicia una transacción, ejecuta sentencias SQL nativas de borrado sobre las tablas implicadas y
 *   confirma los cambios si todo se realiza correctamente. Si ocurre algún error, revierte la transacción
 *   para mantener la integridad de los datos y muestra la excepción por consola.
 */
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
            
           // vaciarTablas(em);

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