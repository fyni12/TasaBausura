package com.mycompany.proyecto_si2.support;

import com.mycompany.proyecto_si2.infra.pdf.PDFGenerator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de utilidad que agrupa métodos auxiliares comunes utilizados durante el procesamiento de la
 * práctica 3. Su finalidad es centralizar conversiones, normalización de textos, formateo de datos,
 * cálculo de bonificaciones y copia de estructuras relacionadas con los recibos.
 *
 * Funciones de la clase:
 *
 * - Prac3Support():
 *   Constructor privado que impide la creación de instancias, ya que la clase está diseñada para
 *   ofrecer únicamente métodos estáticos de apoyo.
 *
 * - bd(double valor):
 *   Convierte un valor numérico de tipo double en un BigDecimal con dos decimales y redondeo HALF_UP.
 *
 * - toXmlDouble(BigDecimal valor):
 *   Convierte un BigDecimal en un valor double normalizado para su escritura en XML, asegurando dos
 *   decimales y eliminando ceros innecesarios al final.
 *
 * - safe(String v):
 *   Devuelve una cadena segura, sustituyendo valores nulos por una cadena vacía y eliminando espacios
 *   sobrantes al principio y al final.
 *
 * - unirApellidos(String apellido1, String apellido2):
 *   Une los dos apellidos en una única cadena segura, evitando nulos y espacios sobrantes.
 *
 * - sanitizeFileName(String s):
 *   Convierte una cadena en un nombre de fichero válido, sustituyendo caracteres no permitidos y
 *   reemplazando espacios por guiones bajos.
 *
 * - emptyToNull(String s):
 *   Devuelve null si una cadena está vacía tras normalizarla; en caso contrario, devuelve su contenido.
 *
 * - charOrNull(String s):
 *   Devuelve el primer carácter de una cadena normalizada o null si no contiene información útil.
 *
 * - bonificacionOrNull(int bonificacion):
 *   Devuelve el valor de bonificación como Double cuando es positivo, o null si no hay bonificación aplicable.
 *
 * - calcularImporteBonificacion(BigDecimal baseBonificada, int bonificacion):
 *   Calcula el importe económico correspondiente a la bonificación aplicada sobre una base imponible
 *   ya bonificada. Si la bonificación no es válida o no puede calcularse, devuelve null.
 *
 * - ordinalTrimestre(int trimestre):
 *   Devuelve la representación textual ordinal de un trimestre, por ejemplo "Primer", "Segundo",
 *   "Tercer" o "Cuarto".
 *
 * - copiarLineas(List<PDFGenerator.LineaConcepto> origen):
 *   Crea y devuelve una copia de la lista de líneas de concepto utilizada en los recibos PDF, generando
 *   nuevos objetos con los mismos valores para evitar compartir referencias con la lista original.
 */
final public class Prac3Support {

    public static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Prac3Support() {
    }

    public static BigDecimal bd(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
    }

    public static double toXmlDouble(BigDecimal valor) {
        if (valor == null) {
            return 0.0;
        }
        BigDecimal normalizado = valor.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        return Double.parseDouble(normalizado.toPlainString());
    }

    public static String safe(String v) {
        return v == null ? "" : v.trim();
    }

    public static String unirApellidos(String apellido1, String apellido2) {
        String a1 = safe(apellido1);
        String a2 = safe(apellido2);
        return (a1 + " " + a2).trim();
    }

    public static String sanitizeFileName(String s) {
        return s.replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("\\s+", "_");
    }

    public static String emptyToNull(String s) {
        String v = safe(s);
        return v.isBlank() ? null : v;
    }

    public static Character charOrNull(String s) {
        String v = safe(s);
        return v.isBlank() ? null : v.charAt(0);
    }

    public static Double bonificacionOrNull(int bonificacion) {
        return bonificacion > 0 ? Double.valueOf(bonificacion) : null;
    }

    public static Double calcularImporteBonificacion(BigDecimal baseBonificada, int bonificacion) {
        if (bonificacion <= 0 || bonificacion >= 100 || baseBonificada == null) {
            return null;
        }

        BigDecimal factor = BigDecimal.ONE.subtract(
                BigDecimal.valueOf(bonificacion).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
        );

        if (factor.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        BigDecimal baseOriginal = baseBonificada.divide(factor, 2, RoundingMode.HALF_UP);
        BigDecimal importeBonif = baseOriginal.subtract(baseBonificada).setScale(2, RoundingMode.HALF_UP);
        return importeBonif.doubleValue();
    }

    public static String ordinalTrimestre(int trimestre) {
        return switch (trimestre) {
            case 1 -> "Primer";
            case 2 -> "Segundo";
            case 3 -> "Tercer";
            case 4 -> "Cuarto";
            default -> "Trimestre";
        };
    }

    public static List<PDFGenerator.LineaConcepto> copiarLineas(List<PDFGenerator.LineaConcepto> origen) {
        List<PDFGenerator.LineaConcepto> copia = new ArrayList<>();
        for (PDFGenerator.LineaConcepto l : origen) {
            PDFGenerator.LineaConcepto x = new PDFGenerator.LineaConcepto();
            x.concepto = l.concepto;
            x.subconcepto = l.subconcepto;
            x.kgIncluidos = l.kgIncluidos;
            x.baseImponible = l.baseImponible;
            x.porcentajeIva = l.porcentajeIva;
            x.importeIva = l.importeIva;
            copia.add(x);
        }
        return copia;
    }
}