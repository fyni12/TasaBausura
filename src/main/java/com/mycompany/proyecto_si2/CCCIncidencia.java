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

public class CCCIncidencia {
    private final Contribuyente contribuyente;
    private final String cccErroneo;
    private final String ibanCorrecto;
    private final String tipoError;

    public CCCIncidencia(Contribuyente contribuyente, String cccErroneo, String ibanCorrecto, String tipoError) {
        this.contribuyente = contribuyente;
        this.cccErroneo = cccErroneo;
        this.ibanCorrecto = ibanCorrecto;
        this.tipoError = tipoError;
    }

    public Contribuyente getContribuyente() {
        return contribuyente;
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