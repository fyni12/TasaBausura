/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyecto_si2;

import POJOS.Ordenanza;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author david
 */
public class OrdenanzaManager {

    private ArrayList<Ordenanza> ordenanzas = new ArrayList();
    private boolean sorted = false;

    public OrdenanzaManager() {
    }

    public void add(Ordenanza ordenanza) {
        ordenanzas.add(ordenanza);
        sorted = false;
    }

    public double calculate(int bonificacion, int kgGen, int id) {
        double result = 0;

        if (!sorted) {
            ordenanzas.sort((e1, e2) -> e1.getKgincluidos().compareTo(e2.getKgincluidos()));
            sorted = true;
        }

        ArrayList<Ordenanza> aplicables = new ArrayList<>();

        for (Ordenanza ord : ordenanzas) {
            if (ord.getIdOrdenanza() == id) {
                aplicables.add(ord);
            }
        }

        if (aplicables.isEmpty()) {
            return 0;
        }

        for (Ordenanza ord : aplicables) {
            String acumulable = ord.getAcumulable();
            int umbral = ord.getKgincluidos();
            double preciokg = ord.getPreciokg();

            result += ord.getPrecioFijo();

            if (acumulable != null) {
                if (isAcumulable(acumulable)) {
                    if (kgGen > 0) {
                        if (kgGen > umbral) {
                            result += umbral * preciokg;
                            kgGen -= umbral;
                        } else {
                            result += kgGen * preciokg;
                            kgGen = 0;
                        }
                    }
                } else {
                    if (kgGen <= umbral) {
                        result += kgGen * preciokg;
                        break;
                    }
                }
            }
        }

        Ordenanza elem = aplicables.get(0);

        if (elem.getConceptoRelacionado() != 0) {
            result += calculate(0, kgGen, elem.getConceptoRelacionado()) * (elem.getPorcentaje() * 0.01);
        }

        result *= 1 - (double) bonificacion * 0.01;

        return result;
    }
    
    public double getIva(int id){
        for (Ordenanza ord : ordenanzas) {
            if (ord.getIdOrdenanza() == id) {
                return ord.getIva();
            }
        }
        
        return 0;
    }

    private boolean isAcumulable(String acu) {
        return acu.toLowerCase().equals("s");
    }

}
