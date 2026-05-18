package com.mycompany.proyecto_si2.domain.model;

import java.time.LocalDate;
import java.util.Objects;

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