package com.mycompany.proyecto_si2;

public final class CCCIncidencia {

    private final int idFilaExcel;
    private final String nombre;
    private final String apellidos;
    private final String nifNie;
    private final String cccErroneo;
    private final String ibanCorrecto;
    private final String tipoError;

    public CCCIncidencia(int idFilaExcel, String nombre, String apellidos, String nifNie,
                         String cccErroneo, String ibanCorrecto, String tipoError) {
        this.idFilaExcel = idFilaExcel;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.nifNie = nifNie;
        this.cccErroneo = cccErroneo;
        this.ibanCorrecto = ibanCorrecto;
        this.tipoError = tipoError;
    }

    public int getIdFilaExcel() {
        return idFilaExcel;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getNifNie() {
        return nifNie;
    }

    public String getCccErroneo() {
        return cccErroneo;
    }

    public String getIbanCorrecto() {
        return ibanCorrecto;
    }

    public String getTipoError() {
        return tipoError;
    }
}