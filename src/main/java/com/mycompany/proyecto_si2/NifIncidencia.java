/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyecto_si2;

import POJOS.Contribuyente;

/**
 *
 * @author Sahira
 */
public class NifIncidencia {
    // Clase auxiliar para agrupar el contribuyente con su tipo de error de NIF
    private Contribuyente contribuyente;
    private String tipoError;

    public NifIncidencia(Contribuyente contribuyente, String tipoError) {
        this.contribuyente = contribuyente;
        this.tipoError = tipoError;
    }

    public Contribuyente getContribuyente() { return contribuyente; }
    public String getTipoError() { return tipoError; }
}

