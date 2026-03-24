package com.mycompany.proyecto_si2;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class EmailGenerator {

    private final Set<String> usados = new HashSet<>();

    public void registrarExistente(String email) {
        if (email != null && !email.trim().isEmpty()) {
            usados.add(email.trim().toLowerCase(Locale.ROOT));
        }
    }

    public String generar(String nombre, String apellido1, String apellido2) {
        String base = normalizar(inicial(nombre) + inicial(apellido1) + inicial(apellido2));
        if (base.isEmpty()) {
            throw new IllegalArgumentException("No hay datos suficientes para generar el email");
        }

        int contador = 0;
        while (true) {
            String email = String.format("%s%02d@tasabasura2026.com", base, contador);
            String key = email.toLowerCase(Locale.ROOT);
            if (!usados.contains(key)) {
                usados.add(key);
                return email;
            }
            contador++;
        }
    }

    private String inicial(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        return String.valueOf(text.trim().charAt(0));
    }

    private String normalizar(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^A-Za-z]", "")
                .toLowerCase(Locale.ROOT);
    }
}