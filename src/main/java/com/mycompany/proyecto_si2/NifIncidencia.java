package com.mycompany.proyecto_si2;

public final class NifIncidencia {

    private final int idFilaExcel;
    private final String nifNie;
    private final String nombre;
    private final String apellido1;
    private final String apellido2;
    private final String tipoError;

    public NifIncidencia(int idFilaExcel, String nifNie, String nombre, String apellido1, String apellido2, String tipoError) {
        this.idFilaExcel = idFilaExcel;
        this.nifNie = nifNie;
        this.nombre = nombre;
        this.apellido1 = apellido1;
        this.apellido2 = apellido2;
        this.tipoError = tipoError;
    }

    public int getIdFilaExcel() {
        return idFilaExcel;
    }

    public String getNifNie() {
        return nifNie;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido1() {
        return apellido1;
    }

    public String getApellido2() {
        return apellido2;
    }

    public String getTipoError() {
        return tipoError;
    }
}