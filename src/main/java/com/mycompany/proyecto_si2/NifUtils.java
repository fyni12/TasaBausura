/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyecto_si2;

/**
 *
 * @author Sahira
 */
public class NifUtils {

    private static final String LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    public static boolean isValidNIF(String nif) throws IllegalArgumentException {
        if (nif == null || nif.isBlank()) {
            throw new IllegalArgumentException("NIF en blanco");
        }

        nif = nif.trim().toUpperCase();

        if (nif.length() == 8) {
            return false; // parece un DNI sin letra
        }

        if (nif.length() != 9) {
            throw new IllegalArgumentException("NIF erróneo");
        }

        char primero = nif.charAt(0);
        char ultimo = nif.charAt(8);

        if (!Character.isDigit(primero) && primero != 'X' && primero != 'Y' && primero != 'Z') {
            throw new IllegalArgumentException("La primera posicion debe ser un numero o X, Y, Z");
        }

        for (int i = 1; i < 8; i++) {
            if (!Character.isDigit(nif.charAt(i))) {
                throw new IllegalArgumentException("Debe haber digitos en las posiciones intermedias");
            }
        }

        if (!Character.isLetter(ultimo)) {
            throw new IllegalArgumentException("La ultima posicion debe ser una letra");
        }

        return ultimo == getLetter(getNumericPart(nif));
    }

    public static char getLetter(int nifNumber) {
        return LETTERS.charAt(nifNumber % 23);
    }

    public static int getNumericPart(String nif) {
        String numericPart = nif.substring(0, nif.length() - 1).toUpperCase()
                .replace("X", "0")
                .replace("Y", "1")
                .replace("Z", "2");

        int numeric = Integer.parseInt(numericPart);
        return numeric;
    }

    public static char getLetterPart(String nif) {
        return nif.charAt(nif.length() - 1);
    }

}
