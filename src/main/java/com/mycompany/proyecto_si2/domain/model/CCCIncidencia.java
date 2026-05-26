package com.mycompany.proyecto_si2.domain.model;

/**
 * Clase inmutable que representa una incidencia detectada en el campo CCC de un contribuyente.
 * Su finalidad es almacenar de forma estructurada la información necesaria sobre el error encontrado,
 * incluyendo la fila del Excel en la que se produce, los datos identificativos del contribuyente, el
 * CCC incorrecto, el IBAN correcto si ha podido obtenerse y el tipo de error asociado. [algomaster](https://algomaster.io/learn/java/immutable-classes)
 *
 * Funciones de la clase:
 *
 * - CCCIncidencia(int idFilaExcel, String nombre, String apellidos, String nifNie, String cccErroneo, String ibanCorrecto, String tipoError):
 *   Constructor que inicializa todos los datos de la incidencia de CCC, dejando creado un objeto
 *   inmutable con toda la información necesaria para su consulta o posterior exportación. [digitalocean](https://www.digitalocean.com/community/tutorials/how-to-create-immutable-class-in-java)
 *
 * - getIdFilaExcel():
 *   Devuelve el identificador o número de fila del Excel en la que se ha detectado la incidencia. [algomaster](https://algomaster.io/learn/java/immutable-classes)
 *
 * - getNombre():
 *   Devuelve el nombre del contribuyente asociado a la incidencia. [digitalocean](https://www.digitalocean.com/community/tutorials/how-to-create-immutable-class-in-java)
 *
 * - getApellidos():
 *   Devuelve los apellidos del contribuyente asociado a la incidencia. [algomaster](https://algomaster.io/learn/java/immutable-classes)
 *
 * - getNifNie():
 *   Devuelve el NIF o NIE del contribuyente relacionado con la incidencia. [digitalocean](https://www.digitalocean.com/community/tutorials/how-to-create-immutable-class-in-java)
 *
 * - getCccErroneo():
 *   Devuelve el código CCC que ha sido detectado como incorrecto o problemático. [algomaster](https://algomaster.io/learn/java/immutable-classes)
 *
 * - getIbanCorrecto():
 *   Devuelve el IBAN correcto obtenido a partir del CCC cuando ha sido posible generarlo. [digitalocean](https://www.digitalocean.com/community/tutorials/how-to-create-immutable-class-in-java)
 *
 * - getTipoError():
 *   Devuelve una descripción del tipo de error asociado a la incidencia registrada. [algomaster](https://algomaster.io/learn/java/immutable-classes)
 */

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