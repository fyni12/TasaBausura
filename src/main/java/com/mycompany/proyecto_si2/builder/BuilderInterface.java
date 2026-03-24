/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyecto_si2.builder;

import org.jdom2.Document;

/**
 *
 * @author Sahira
 */
public interface BuilderInterface {
    public void addNifNie(String nifnie);
    public void addID(String id);
    public void addNombre(String nombre);
    public void addApellidos(String apellido1, String apellido2);
    public void addError(String error);
    public void addCCCErroneo(String ccc);
    public void addIbanCorrecto(String iban);
    public void addElement();
    public Document getDoc();
    public void clear();
    
    
}