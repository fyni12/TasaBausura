package com.mycompany.proyecto_si2.cli;

import com.mycompany.proyecto_si2.domain.model.PeriodoImpositivo;
import com.mycompany.proyecto_si2.domain.service.PeriodoImpositivoParser;
import java.util.Scanner;
/**
 * Clase de utilidad encargada de leer desde consola el período impositivo introducido por el usuario.
 * Su objetivo es solicitar una entrada en formato de texto, obtenerla mediante un objeto Scanner y
 * transformarla en un objeto de tipo PeriodoImpositivo usando el parser correspondiente.
 *
 * Funciones de la clase:
 *
 * - ConsolaPeriodoReader():
 *   Constructor privado que impide la creación de instancias de esta clase, ya que está diseñada
 *   únicamente para ofrecer funcionalidad estática de apoyo.
 *
 * - leer(Scanner scanner):
 *   Método estático que muestra por consola un mensaje solicitando el período impositivo, lee la
 *   línea introducida por el usuario y la convierte a un objeto PeriodoImpositivo mediante
 *   PeriodoImpositivoParser.parse().
 */
public final class ConsolaPeriodoReader {

    private ConsolaPeriodoReader() {
    }

    public static PeriodoImpositivo leer(Scanner scanner) {
        System.out.print("Introduzca el periodo impositivo (Ej. 1T 2025): ");
        String entrada = scanner.nextLine();
        return PeriodoImpositivoParser.parse(entrada);
    }
}