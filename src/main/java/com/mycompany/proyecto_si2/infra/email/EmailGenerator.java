package com.mycompany.proyecto_si2.infra.email;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
/**
 * Clase de utilidad encargada de generar direcciones de correo electrónico únicas a partir del nombre
 * y apellidos de una persona. También mantiene un registro interno de los correos ya existentes para
 * evitar duplicados durante el proceso de generación.
 *
 * Funciones de la clase:
 *
 * - registrarExistente(String email):
 *   Registra un correo electrónico ya existente dentro del conjunto interno de direcciones usadas.
 *   Antes de guardarlo, comprueba que no sea nulo ni vacío y lo normaliza a minúsculas para evitar
 *   duplicados por diferencias de formato.
 *
 * - generar(String nombre, String apellido1, String apellido2):
 *   Genera una dirección de correo electrónico a partir de las iniciales del nombre y de los dos
 *   apellidos. Primero construye una base con esas iniciales, la normaliza eliminando acentos y
 *   caracteres no válidos, y después añade un contador numérico con dos dígitos seguido del dominio
 *   fijo. Si el correo generado no existe todavía, lo registra y lo devuelve; en caso contrario,
 *   incrementa el contador hasta encontrar uno disponible. Si no hay datos suficientes para formar
 *   la base del correo, lanza una excepción.
 *
 * - inicial(String text):
 *   Método auxiliar privado que obtiene la inicial de un texto. Si el valor es nulo o está vacío,
 *   devuelve una cadena vacía.
 *
 * - normalizar(String text):
 *   Método auxiliar privado que normaliza una cadena eliminando signos diacríticos, descartando
 *   caracteres no alfabéticos y convirtiendo el resultado a minúsculas.
 */
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