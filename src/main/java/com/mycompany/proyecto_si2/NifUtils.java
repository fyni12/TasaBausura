package com.mycompany.proyecto_si2;

import java.util.Locale;

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