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
public class CCCBuilder implements BuilderInterface {

    private final Document doc;
    private final Element raiz;
    private final String elemString = "Cuentas";
    private Element cuenta;

    public CCCBuilder(String raiz) {
        this.raiz = new Element(raiz);
        this.doc = new Document(this.raiz);
        this.cuenta = new Element(elemString);
    }

    @Override
    public void addNifNie(String nifnie) {
        cuenta.addContent(new Element("NIFNIE").setText(nifnie));

    }

    @Override
    public void addNombre(String nombre) {
        cuenta.addContent(new Element("Nombre").setText(nombre));

    }

    @Override
    public void addApellidos(String apellido1, String apellido2) {
        String ap1 = (apellido1 == null) ? "" : apellido1.trim();
        String ap2 = (apellido2 == null) ? "" : apellido2.trim();

        String apellidos = (ap1 + " " + ap2).trim();
        cuenta.addContent(new Element("Apellidos").setText(apellidos));
    }

    @Override
    public void addError(String error) {
        cuenta.addContent(new Element("TipoError").setText(error));
    }

    @Override
    public void addCCCErroneo(String ccc) {
        cuenta.addContent(new Element("CCCErroneo").setText(ccc));

    }

    @Override
    public void addIbanCorrecto(String iban) {
        cuenta.addContent(new Element("IBANCorrecto").setText(iban));

    }

    @Override
    public void addElement() {
        if (!cuenta.getContent().isEmpty() || !cuenta.getAttributes().isEmpty()) {
            raiz.addContent(cuenta);
            this.cuenta = new Element(elemString);
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
        cuenta.setAttribute("id", id);
    }

}
