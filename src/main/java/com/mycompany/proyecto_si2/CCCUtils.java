package com.mycompany.proyecto_si2;

import java.math.BigInteger;
import java.util.Locale;

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