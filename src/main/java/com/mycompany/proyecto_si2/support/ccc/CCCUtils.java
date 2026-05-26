package com.mycompany.proyecto_si2.support.ccc;

import java.math.BigInteger;
import java.util.Locale;

/**
 * Clase de utilidad encargada de validar, corregir y transformar códigos CCC en su correspondiente IBAN.
 * Su función principal es normalizar los datos de entrada, comprobar si el CCC puede utilizarse,
 * recalcular sus dígitos de control cuando sea necesario y generar el IBAN asociado a partir del
 * país y de la cuenta corregida.
 *
 * Funciones de la clase:
 *
 * - CCCUtils():
 *   Constructor privado que impide la creación de instancias, ya que esta clase está diseñada
 *   únicamente como utilidad con métodos estáticos.
 *
 * - validarYCorregir(String rawCcc, String rawPais):
 *   Método principal que valida un CCC y el código de país recibido. Primero normaliza ambos valores,
 *   comprueba que tengan el formato esperado y, si son válidos, corrige los dígitos de control del CCC
 *   y genera el IBAN correspondiente. Devuelve un objeto Resultado indicando si el CCC era válido,
 *   si ha sido subsanado o si era erróneo e imposible de convertir a IBAN.
 *
 * - corregirCCC(String ccc):
 *   Recalcula los dos dígitos de control del CCC a partir de la entidad, oficina y número de cuenta,
 *   devolviendo el código completo corregido.
 *
 * - calcularIBAN(String ccc, String pais):
 *   Genera el IBAN correspondiente a partir de un CCC válido y un código de país, convirtiendo las
 *   letras del país a su representación numérica y calculando los dígitos de control internacionales.
 *
 * - calcularDigitoControl(String diezDigitos):
 *   Método auxiliar privado que calcula un dígito de control para una secuencia de diez dígitos
 *   aplicando el algoritmo de pesos definido para el CCC.
 *
 * - letrasANumeros(String text):
 *   Método auxiliar privado que convierte cada letra de un texto en su valor numérico equivalente
 *   según la codificación utilizada en el cálculo del IBAN.
 *
 * - normalizarCCC(String raw):
 *   Método auxiliar privado que limpia el CCC recibido eliminando espacios y devolviendo null si
 *   no contiene un valor útil.
 *
 * - normalizarPais(String raw):
 *   Método auxiliar privado que limpia y normaliza el código de país recibido, convirtiéndolo a
 *   mayúsculas y devolviendo null si no contiene un valor válido.
 *
 * Clases internas:
 *
 * - Estado:
 *   Enumeración que representa el estado del CCC tras el proceso de validación, pudiendo ser válido,
 *   subsanado o erróneo.
 *
 * - Resultado:
 *   Clase inmutable que encapsula el resultado del proceso de validación y corrección del CCC,
 *   incluyendo el estado final, el valor original, el valor corregido, el IBAN generado y el
 *   posible tipo de error asociado.
 *
 *   Funciones de la clase Resultado:
 *
 *   - Resultado(Estado estado, String cccOriginal, String cccFinal, String iban, String tipoError):
 *     Constructor que inicializa todos los datos resultantes del proceso de validación del CCC.
 *
 *   - getEstado():
 *     Devuelve el estado final del CCC tras su validación.
 *
 *   - getCccOriginal():
 *     Devuelve el CCC original recibido como entrada.
 *
 *   - getCccFinal():
 *     Devuelve el CCC corregido o validado resultante del proceso.
 *
 *   - getIban():
 *     Devuelve el IBAN generado a partir del CCC final y del país.
 *
 *   - getTipoError():
 *     Devuelve la descripción del error producido cuando el CCC no puede validarse ni corregirse.
 *
 *   - esValidoOSubsanado():
 *     Indica si el resultado final del CCC es utilizable, es decir, si ha sido validado correctamente
 *     o ha podido subsanarse.
 */
public final class CCCUtils {

    private static final int[] PESOS = {1, 2, 4, 8, 5, 10, 9, 7, 3, 6};

    private CCCUtils() {
    }

    public enum Estado {
        VALIDO, SUBSANADO, ERRONEO
    }

    public static final class Resultado {
        private final Estado estado;
        private final String cccOriginal;
        private final String cccFinal;
        private final String iban;
        private final String tipoError;

        public Resultado(Estado estado, String cccOriginal, String cccFinal, String iban, String tipoError) {
            this.estado = estado;
            this.cccOriginal = cccOriginal;
            this.cccFinal = cccFinal;
            this.iban = iban;
            this.tipoError = tipoError;
        }

        public Estado getEstado() {
            return estado;
        }

        public String getCccOriginal() {
            return cccOriginal;
        }

        public String getCccFinal() {
            return cccFinal;
        }

        public String getIban() {
            return iban;
        }

        public String getTipoError() {
            return tipoError;
        }

        public boolean esValidoOSubsanado() {
            return estado == Estado.VALIDO || estado == Estado.SUBSANADO;
        }
    }

   public static Resultado validarYCorregir(String rawCcc, String rawPais) {
    String ccc = normalizarCCC(rawCcc);
    String pais = normalizarPais(rawPais);

    if (ccc == null || !ccc.matches("\\d{20}") || pais == null || !pais.matches("[A-Z]{2}")) {
        return new Resultado(Estado.ERRONEO, rawCcc, null, null, "IMPOSIBLE GENERAR IBAN");
    }
    String corregido = corregirCCC(ccc);
    String iban = calcularIBAN(corregido, pais);

    if (ccc.equals(corregido)) {
        return new Resultado(Estado.VALIDO, ccc, corregido, iban, null);
    }

    return new Resultado(Estado.SUBSANADO, ccc, corregido, iban, null);
}

    public static String corregirCCC(String ccc) {
        String entidadOficina = ccc.substring(0, 8);
        String cuenta = ccc.substring(10, 20);
        char dc1 = calcularDigitoControl("00" + entidadOficina);
        char dc2 = calcularDigitoControl(cuenta);
        return entidadOficina + dc1 + dc2 + cuenta;
    }

    public static String calcularIBAN(String ccc, String pais) {
        String paisNumerico = letrasANumeros(pais);
        String cadena = ccc + paisNumerico + "00";
        BigInteger numero = new BigInteger(cadena);
        int control = 98 - numero.mod(BigInteger.valueOf(97)).intValue();
        return pais + String.format("%02d", control) + ccc;
    }

    private static char calcularDigitoControl(String diezDigitos) {
        int suma = 0;
        for (int i = 0; i < 10; i++) {
            suma += Character.getNumericValue(diezDigitos.charAt(i)) * PESOS[i];
        }

        int resultado = 11 - (suma % 11);
        if (resultado == 11) {
            resultado = 0;
        } else if (resultado == 10) {
            resultado = 1;
        }

        return (char) ('0' + resultado);
    }

    private static String letrasANumeros(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            sb.append((c - 'A') + 10);
        }
        return sb.toString();
    }

    private static String normalizarCCC(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim().replaceAll("\\s+", "");
        return value.isEmpty() ? null : value;
    }

    private static String normalizarPais(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim().toUpperCase(Locale.ROOT);
        return value.isEmpty() ? null : value;
    }
}