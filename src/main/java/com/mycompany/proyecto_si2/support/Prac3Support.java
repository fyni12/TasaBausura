package com.mycompany.proyecto_si2.support;

import com.mycompany.proyecto_si2.infra.pdf.PDFGenerator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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