package com.mycompany.proyecto_si2.domain.service;

import com.mycompany.proyecto_si2.domain.model.PeriodoImpositivo;
/**
 * Clase de utilidad encargada de convertir una cadena de texto introducida por el usuario en un objeto
 * PeriodoImpositivo. Su función es validar el formato de entrada, extraer el trimestre y el año, y crear
 * el período impositivo correspondiente a partir de esos datos.
 *
 * Funciones de la clase:
 *
 * - PeriodoImpositivoParser():
 *   Constructor privado que impide instanciar esta clase, ya que está diseñada exclusivamente para
 *   ofrecer funcionalidad estática de parseo.
 *
 * - parse(String raw):
 *   Método estático que recibe una cadena con el período impositivo en formato de texto, por ejemplo
 *   "1T 2025". Primero valida que la entrada no sea nula ni esté vacía, después normaliza el texto,
 *   localiza la posición del separador del trimestre, extrae el número de trimestre y el año, convierte
 *   ambos valores a enteros y finalmente crea y devuelve un objeto PeriodoImpositivo. Si el formato
 *   introducido no es válido, lanza una excepción indicando el formato esperado.
 */
public final class PeriodoImpositivoParser {

    private PeriodoImpositivoParser() {
    }

    public static PeriodoImpositivo parse(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe introducir un periodo, por ejemplo: 1T 2025");
        }

        String limpio = raw.trim().toUpperCase().replaceAll("\\s+", " ");
        int posT = limpio.indexOf('T');

        if (posT <= 0 || posT == limpio.length() - 1) {
            throw new IllegalArgumentException("Formato inválido. Use: 1T 2025");
        }

        String trimestreStr = limpio.substring(0, posT).trim();
        String yearStr = limpio.substring(posT + 1).trim();

        int trimestre;
        int year;

        try {
            trimestre = Integer.parseInt(trimestreStr);
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato inválido. Use: 1T 2025", e);
        }

        return new PeriodoImpositivo(trimestre, year);
    }
}