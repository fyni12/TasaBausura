/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyecto_si2;

import java.math.BigInteger;

/**
 *
 * @author Sahira
 */
public class CCCUtils {

    public static boolean isValid(String ccc) throws IllegalArgumentException {
        if (ccc == null || ccc.length() != 20) {
            throw new IllegalArgumentException("ccc invalido");
        } else {
            for (char letra : ccc.toCharArray()) {
                if (!Character.isDigit(letra)) {
                    throw new IllegalArgumentException("ccc invalido");

                }
            }
        }

        String first = ccc.substring(0, 8);
        String control = ccc.substring(8, 10);
        String last = ccc.substring(10, 20);

        return control.equals(calcDigit(first) + calcDigit(last));
    }

    private static String calcDigit(String subSecuence) {
        String cadena = String.format("%10s", subSecuence).replace(' ', '0');

        int suma
                = Character.getNumericValue(cadena.charAt(0)) * 1
                + Character.getNumericValue(cadena.charAt(1)) * 2
                + Character.getNumericValue(cadena.charAt(2)) * 4
                + Character.getNumericValue(cadena.charAt(3)) * 8
                + Character.getNumericValue(cadena.charAt(4)) * 5
                + Character.getNumericValue(cadena.charAt(5)) * 10
                + Character.getNumericValue(cadena.charAt(6)) * 9
                + Character.getNumericValue(cadena.charAt(7)) * 7
                + Character.getNumericValue(cadena.charAt(8)) * 3
                + Character.getNumericValue(cadena.charAt(9)) * 6;

        int result = 11 - (suma % 11);

        if (result == 10) {
            result = 1;
        } else if (result == 11) {
            result = 0;
        }

        return String.valueOf(result);
    }

    public static String fix(String ccc) {
        if (ccc == null || ccc.length() != 20) {
            throw new IllegalArgumentException("ccc invalido");
        }

        String first = ccc.substring(0, 8);
        String last = ccc.substring(10, 20);

        return first + calcDigit(first) + calcDigit(last) + last;
    }

    public static String calcularIBAN(String ccc, String paisCCC) {
        if (ccc == null || paisCCC == null) {
            throw new IllegalArgumentException("CCC o país nulo");
        }

        ccc = ccc.trim().replace(" ", "");
        paisCCC = paisCCC.trim().toUpperCase();

        if (ccc.length() != 20) {
            throw new IllegalArgumentException("El CCC debe tener 20 dígitos");
        }

        if (paisCCC.length() != 2 || !Character.isLetter(paisCCC.charAt(0)) || !Character.isLetter(paisCCC.charAt(1))) {
            throw new IllegalArgumentException("El país debe tener 2 letras");
        }

        String reordenado = ccc + paisCCC + "00";

        String numerico = convertirLetrasANumeros(reordenado);

        BigInteger numero = new BigInteger(numerico);
        int resto = numero.mod(BigInteger.valueOf(97)).intValue();
        int control = 98 - resto;

        String digitosControl = String.format("%02d", control);

        return paisCCC + digitosControl + ccc;
    }

    private static String convertirLetrasANumeros(String texto) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < texto.length(); i++) {
            char ch = Character.toUpperCase(texto.charAt(i));

            if (Character.isDigit(ch)) {
                sb.append(ch);
            } else if (Character.isLetter(ch)) {
                sb.append((ch - 'A') + 10);
            } else {
                throw new IllegalArgumentException("Caracter no válido: " + ch);
            }
        }

        return sb.toString();
    }

}
