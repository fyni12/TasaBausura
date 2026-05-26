package com.mycompany.proyecto_si2.support.nif;

import java.util.Locale;
/**
 * Clase de utilidad encargada de validar y corregir valores de NIF y NIE.
 * Su función principal es normalizar la entrada recibida, comprobar si el identificador fiscal
 * tiene un formato válido, calcular la letra de control cuando sea necesario y devolver un
 * resultado que indique si el valor era válido, subsanable, erróneo o estaba en blanco.
 *
 * Funciones de la clase:
 *
 * - NifUtils():
 *   Constructor privado que impide la creación de instancias, ya que esta clase está diseñada
 *   únicamente como utilidad estática.
 *
 * - validar(String raw):
 *   Método principal que valida un NIF o NIE recibido como texto. Primero normaliza el valor,
 *   comprueba si está en blanco, si le falta la letra final o si ya incluye letra de control.
 *   En función del caso, calcula la letra cuando sea necesario y devuelve un objeto Resultado
 *   indicando el estado final del identificador y su valor corregido o validado.
 *
 * - calcularLetra(String cuerpo):
 *   Calcula la letra de control correspondiente al cuerpo numérico de un NIF o NIE. En el caso
 *   del NIE, primero transforma la letra inicial X, Y o Z en su equivalente numérico antes de
 *   realizar el cálculo del resto módulo 23.
 *
 * - normalizar(String raw):
 *   Método auxiliar privado que limpia el valor recibido eliminando espacios y guiones, lo convierte
 *   a mayúsculas y devuelve null si la entrada no contiene información útil.
 *
 * Clases internas:
 *
 * - Estado:
 *   Enumeración que representa el resultado de la validación del NIF/NIE, pudiendo indicar que el
 *   valor es válido, subsanado, erróneo o está en blanco.
 *
 * - Resultado:
 *   Clase inmutable que encapsula el resultado del proceso de validación de un NIF/NIE, incluyendo
 *   el estado obtenido y el valor final calculado o corregido.
 *
 *   Funciones de la clase Resultado:
 *
 *   - Resultado(Estado estado, String valorFinal):
 *     Constructor que inicializa el estado y el valor final resultantes del proceso de validación.
 *
 *   - getEstado():
 *     Devuelve el estado final del NIF o NIE validado.
 *
 *   - getValorFinal():
 *     Devuelve el valor final del identificador, ya sea el original validado o el valor corregido.
 *
 *   - esValidoOSubsanado():
 *     Indica si el NIF o NIE puede considerarse utilizable, es decir, si era correcto o ha podido
 *     ser corregido satisfactoriamente.
 */
public final class NifUtils {

    private static final String LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private NifUtils() {
    }

    public enum Estado {
        VALIDO, SUBSANADO, ERRONEO, BLANCO
    }

    public static final class Resultado {
        private final Estado estado;
        private final String valorFinal;

        public Resultado(Estado estado, String valorFinal) {
            this.estado = estado;
            this.valorFinal = valorFinal;
        }

        public Estado getEstado() {
            return estado;
        }

        public String getValorFinal() {
            return valorFinal;
        }

        public boolean esValidoOSubsanado() {
            return estado == Estado.VALIDO || estado == Estado.SUBSANADO;
        }
    }

    public static Resultado validar(String raw) {
        String value = normalizar(raw);

        if (value == null) {
            return new Resultado(Estado.BLANCO, null);
        }

        if (value.matches("\\d{8}")) {
            return new Resultado(Estado.SUBSANADO, value + calcularLetra(value));
        }

        if (value.matches("[XYZ]\\d{7}")) {
            return new Resultado(Estado.SUBSANADO, value + calcularLetra(value));
        }

        if (value.matches("\\d{8}[A-Z]") || value.matches("[XYZ]\\d{7}[A-Z]")) {
            String cuerpo = value.substring(0, value.length() - 1);
            char actual = value.charAt(value.length() - 1);
            char calculada = calcularLetra(cuerpo);
            String corregido = cuerpo + calculada;
            return actual == calculada
                    ? new Resultado(Estado.VALIDO, corregido)
                    : new Resultado(Estado.SUBSANADO, corregido);
        }

        return new Resultado(Estado.ERRONEO, value);
    }

    public static char calcularLetra(String cuerpo) {
        String numeric = cuerpo.toUpperCase(Locale.ROOT)
                .replace('X', '0')
                .replace('Y', '1')
                .replace('Z', '2');

        int numero = Integer.parseInt(numeric);
        return LETTERS.charAt(numero % 23);
    }

    private static String normalizar(String raw) {
        if (raw == null) {
            return null;
        }

        String value = raw.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[\\s-]+", "");

        return value.isEmpty() ? null : value;
    }
}