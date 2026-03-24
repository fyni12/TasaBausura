/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyecto_si2.builder;

import POJOS.Contribuyente;
import com.mycompany.proyecto_si2.CCCIncidencia;

/**
 *
 * @author Sahira
 */

public class Director {

    BuilderInterface builder;

    public void setBuilder(BuilderInterface builder) {
        this.builder = builder;
    }

    public void buildNifNieRegister(Contribuyente contribuyente, String motivo) {
        builder.addNifNie(contribuyente.getNifnie());
        builder.addID(String.valueOf(contribuyente.getIdContribuyente()));

        builder.addNombre(contribuyente.getNombre());
        builder.addApellidos(contribuyente.getApellido1(), contribuyente.getApellido2());

        builder.addError(motivo);

        builder.addElement();
    }

    public void buildCCCRegister(CCCIncidencia inc) {
        Contribuyente cont = inc.getContribuyente();

        builder.addID(String.valueOf(cont.getIdContribuyente()));
        builder.addNombre(cont.getNombre());
        builder.addApellidos(cont.getApellido1(), cont.getApellido2());
        builder.addNifNie(cont.getNifnie());
        builder.addCCCErroneo(inc.getCccErroneo());

        if (inc.getIbanCorrecto() != null) {
            builder.addIbanCorrecto(inc.getIbanCorrecto());
        }

        if (inc.getTipoError() != null) {
            builder.addError(inc.getTipoError());
        }

        builder.addElement();
    }

    public void clear() {
        builder.clear();
    }

}
