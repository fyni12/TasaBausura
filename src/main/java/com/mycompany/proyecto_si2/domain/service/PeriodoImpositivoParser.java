package com.mycompany.proyecto_si2.domain.service;

import com.mycompany.proyecto_si2.domain.model.PeriodoImpositivo;

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