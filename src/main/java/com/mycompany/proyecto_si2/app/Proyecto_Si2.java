/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.proyecto_si2.app;

import com.mycompany.proyecto_si2.app.ProyectoSi2App;
import com.mycompany.proyecto_si2.app.InformeFinalRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;



/**
 *
 * @author Sahira Cañon Alcalde
 */
public class Proyecto_Si2 {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_Proyecto_Si2_jar_1.0-SNAPSHOTPU");
        EntityManager em = emf.createEntityManager();

        /**
         * Scanner sc = new Scanner(System.in);
         *
         * System.out.print("INTRODUZCA EL NIF: ");
         *
         * String nifNie = sc.nextLine();//pedir nif por pantalla
         *
         * //APARTADO 1------------- String contribuyenteQuery = "FROM
         * Contribuyente c WHERE c.nifnie=:p1 "; Query query =
         * em.createQuery(contribuyenteQuery); query.setParameter("p1", nifNie);
         * List<Contribuyente> resultadoContribuyente = query.getResultList();
         *
         * if (resultadoContribuyente.isEmpty()) { System.out.println("NO SE HA
         * ENCONTRADO EL CONTRIBUYENTE"); return; }
         *
         * for (Contribuyente contri : resultadoContribuyente) {
         * System.out.println(String.format( "nombre: %s \n apellidos: %s %s\n
         * Nif: %s\nDireccion: %s\n", contri.getNombre(), contri.getApellido1(),
         * (contri.getApellido2() != null) ? contri.getApellido2() : "",
         * contri.getNifnie(), contri.getDireccion() )); }
         *
         * //APARTADO 2----------------------- String reciboQuery = "FROM
         * Recibos r WHERE r.nifContribuyente =:p2"; Query reciboConsulta =
         * em.createQuery(reciboQuery); reciboConsulta.setParameter("p2",
         * nifNie); List<Recibos> recibos = reciboConsulta.getResultList();
         *
         * for (Recibos reci : recibos) {
         * System.out.println(String.format("\nIdRecibo: %s\nbase imponible:
         * %s", reci.getNumeroRecibo(), reci.getTotalBaseImponible()));
         *
         * reci.setTotalBaseImponible(500); System.out.println(String.format(
         * "Despues de modificacion: %s", reci.getTotalBaseImponible() ));
         *
         * }
         *
         * //APARTADO 3 -------------------- String recibAVG = "SELECT
         * AVG(r.totalBaseImponible) FROM Recibos r"; Query recibAVGQuery =
         * em.createQuery(recibAVG); Double resultAVG = (Double)
         * recibAVGQuery.getSingleResult();
         *
         * em.getTransaction().begin();
         *
         * String removeString = "DELETE FROM Recibos r WHERE
         * r.totalBaseImponible > :media"; Query removeQuery =
         * em.createQuery(removeString); removeQuery.setParameter("media",
         * resultAVG); int borrados = removeQuery.executeUpdate();
         *
         * em.getTransaction().commit();
         *
         * System.out.println(String.format("Eliminados: %d", borrados));
         *
         * em.close(); emf.close();
         *
         * FIN PRÁCTICA 1
         *
         */
        //INICIO PRÁCTICA 2
        /*try {
        try {
            //vaciarTablas(em);
            Path resources = Paths.get("resources");
            Path excel = resources.resolve("SistemasBasura.xlsx");

            PracticaBasuraService service = new PracticaBasuraService(excel, resources);
            service.procesar();

            //INICIO PRACTICA 3
            Scanner sc = new Scanner(System.in);
            System.out.print("Introduzca el periodo impositivo (Ej. 1T 2026): ");
            String periodo = sc.nextLine();

            Prac3 prac3 = new Prac3(excel, resources, em);
            prac3.procesar(periodo);

            System.out.println("Practica completada correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        
        new ProyectoSi2App().run();
        
        

    }

    
}
