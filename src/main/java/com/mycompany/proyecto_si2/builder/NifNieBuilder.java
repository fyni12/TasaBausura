/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyecto_si2.builder;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 *
 * @author Sahira
 */

public class NifNieBuilder implements BuilderInterface {

    private final Document doc;
    private final Element raiz;
    private final String elemString="Contribuyentes";
    private Element contribuyente;

    public NifNieBuilder(String raiz) {
        this.raiz = new Element(raiz);
        this.doc = new Document(this.raiz);

        this.contribuyente = new Element(elemString);
    }

    @Override
    public void addNifNie(String nifnie) {
        contribuyente.addContent(new Element("NIF_NIE").setText(nifnie));

    }

    @Override
    public void addNombre(String nombre) {
        contribuyente.addContent(new Element("Nombre").setText(nombre));

    }

    @Override
    public void addApellidos(String apellido1, String apellido2) {
        contribuyente.addContent(new Element("PrimerApellido").setText(apellido1));
        if(apellido2!=null) contribuyente.addContent(new Element("SegundoApellido").setText(apellido2));

    }

    @Override
    public void addError(String error) {
        contribuyente.addContent(new Element("TipoDeError").setText(error));
    }

    @Override
    public void addCCCErroneo(String ccc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addIbanCorrecto(String iban) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addElement() {
        if (!contribuyente.getContent().isEmpty() || !contribuyente.getAttributes().isEmpty()) {
            raiz.addContent(contribuyente);
            this.contribuyente = new Element(elemString);
        }
    }

    @Override
    public Document getDoc() {
        return doc;
    }

    @Override
    public void clear() {
        raiz.removeContent();
    }

    @Override
    public void addID(String id) {
        contribuyente.setAttribute("id", id);
    }

}
