package com.mycompany.proyecto_si2;

public class Recibo {

    private int idRecibo;
    private String exencion;
    private int idFilaExcel;
    private String nombre;
    private String primerApellido;
    private String segundoApellido;
    private String nif;
    private String iban;
    private int kgGenerados;
    private double baseImponibleRecibo;
    private double ivaRecibo;
    private double totalRecibo;

    public Recibo() {
    }

    public int getIdRecibo() {
        return idRecibo;
    }

    public void setIdRecibo(int idRecibo) {
        this.idRecibo = idRecibo;
    }

    public String getExencion() {
        return exencion;
    }

    public void setExencion(String exencion) {
        this.exencion = exencion;
    }

    public int getIdFilaExcel() {
        return idFilaExcel;
    }

    public void setIdFilaExcel(int idFilaExcel) {
        this.idFilaExcel = idFilaExcel;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public int getKgGenerados() {
        return kgGenerados;
    }

    public void setKgGenerados(int kgGenerados) {
        this.kgGenerados = kgGenerados;
    }

    public double getBaseImponibleRecibo() {
        return baseImponibleRecibo;
    }

    public void setBaseImponibleRecibo(double baseImponibleRecibo) {
        this.baseImponibleRecibo = baseImponibleRecibo;
    }

    public double getIvaRecibo() {
        return ivaRecibo;
    }

    public void setIvaRecibo(double ivaRecibo) {
        this.ivaRecibo = ivaRecibo;
    }

    public double getTotalRecibo() {
        return totalRecibo;
    }

    public void setTotalRecibo(double totalRecibo) {
        this.totalRecibo = totalRecibo;
    }
}