package com.mycompany.proyecto_si2.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Clase inmutable que representa un período impositivo trimestral dentro de un año concreto.
 * Su finalidad es encapsular el trimestre, el año y las fechas de inicio y fin calculadas automáticamente
 * a partir de esos valores, garantizando además que el período creado sea válido. [howtodoinjava](https://howtodoinjava.com/java/date-time/current-quarter-start-end/)
 *
 * Funciones de la clase:
 *
 * - PeriodoImpositivo(int trimestre, int year):
 *   Constructor que crea un período impositivo validando que el trimestre esté entre 1 y 4 y que el año
 *   tenga un valor razonable. A partir del trimestre y del año calcula automáticamente la fecha de inicio
 *   del período y la fecha de fin correspondiente al último día del trimestre. [docs.oracle](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)
 *
 * - getTrimestre():
 *   Devuelve el número de trimestre asociado al período impositivo. [howtodoinjava](https://howtodoinjava.com/java/date-time/current-quarter-start-end/)
 *
 * - getYear():
 *   Devuelve el año al que pertenece el período impositivo. [docs.oracle](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)
 *
 * - getFechaInicio():
 *   Devuelve la fecha de inicio del período, correspondiente al primer día del primer mes del trimestre
 *   indicado. [howtodoinjava](https://howtodoinjava.com/java/date-time/current-quarter-start-end/)
 *
 * - getFechaFin():
 *   Devuelve la fecha de fin del período, correspondiente al último día del tercer mes del trimestre
 *   indicado. [docs.oracle](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)
 *
 * - toConsoleFormat():
 *   Devuelve una representación en texto del período con formato pensado para consola, por ejemplo
 *   "1T 2025". [howtodoinjava](https://howtodoinjava.com/java/date-time/current-quarter-start-end/)
 *
 * - toString():
 *   Devuelve la representación textual del objeto reutilizando el formato preparado para consola, de modo
 *   que el período pueda mostrarse de forma legible al imprimirse directamente. [docs.oracle](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html)
 *
 * - hashCode():
 *   Devuelve un código hash calculado a partir de los atributos principales del objeto, lo que permite su
 *   uso correcto en colecciones basadas en hash y mantiene coherencia con el método equals. [baeldung](https://www.baeldung.com/java-equals-hashcode-contracts)
 *
 * - equals(Object obj):
 *   Compara este objeto con otro para determinar si representan el mismo período impositivo. Dos instancias
 *   se consideran iguales cuando tienen el mismo trimestre, el mismo año y las mismas fechas de inicio y fin
 *   asociadas. [test-king](https://www.test-king.com/blog/common-pitfalls-and-best-practices-for-overriding-equals-and-hashcode-in-java/)
 */

public final class PeriodoImpositivo {

    private final int trimestre;
    private final int year;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;

    public PeriodoImpositivo(int trimestre, int year) {
        if (trimestre < 1 || trimestre > 4) {
            throw new IllegalArgumentException("El trimestre debe estar entre 1 y 4");
        }
        if (year < 1900 || year > 3000) {
            throw new IllegalArgumentException("El año no es válido");
        }

        this.trimestre = trimestre;
        this.year = year;

        int mesInicio = (trimestre - 1) * 3 + 1;
        this.fechaInicio = LocalDate.of(year, mesInicio, 1);
        this.fechaFin = this.fechaInicio.plusMonths(3).minusDays(1);
    }

    public int getTrimestre() {
        return trimestre;
    }

    public int getYear() {
        return year;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public String toConsoleFormat() {
        return trimestre + "T " + year;
    }

    @Override
    public String toString() {
        return toConsoleFormat();
    }

    @Override
    public int hashCode() {
        return Objects.hash(trimestre, year, fechaInicio, fechaFin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PeriodoImpositivo other)) return false;
        return trimestre == other.trimestre
                && year == other.year
                && Objects.equals(fechaInicio, other.fechaInicio)
                && Objects.equals(fechaFin, other.fechaFin);
    }
}