package com.mycompany.proyecto_si2;

import java.util.Scanner;

public final class ConsolaPeriodoReader {

    private ConsolaPeriodoReader() {
    }

    public static PeriodoImpositivo leer(Scanner scanner) {
        System.out.print("Introduzca el periodo impositivo (Ej. 1T 2025): ");
        String entrada = scanner.nextLine();
        return PeriodoImpositivoParser.parse(entrada);
    }
}