package com.mycompany.proyecto_si2.domain.model;

/**
 * Clase inmutable que representa una incidencia detectada en el campo NIF/NIE de un contribuyente.
 * Su objetivo es almacenar de forma estructurada la información básica asociada al error encontrado,
 * incluyendo la fila del Excel en la que aparece, el identificador fiscal analizado, los datos personales
 * del contribuyente y el tipo de incidencia detectada.
 *
 * Funciones de la clase:
 *
 * - NifIncidencia(int idFilaExcel, String nifNie, String nombre, String apellido1, String apellido2, String tipoError):
 *   Constructor que inicializa todos los atributos de la incidencia, dejando creado un objeto con la
 *   información necesaria para registrar, consultar o exportar el error detectado.
 *
 * - getIdFilaExcel():
 *   Devuelve el número o identificador de la fila del Excel en la que se ha detectado la incidencia.
 *
 * - getNifNie():
 *   Devuelve el valor del NIF o NIE asociado a la incidencia registrada.
 *
 * - getNombre():
 *   Devuelve el nombre del contribuyente relacionado con la incidencia.
 *
 * - getApellido1():
 *   Devuelve el primer apellido del contribuyente relacionado con la incidencia.
 *
 * - getApellido2():
 *   Devuelve el segundo apellido del contribuyente relacionado con la incidencia.
 *
 * - getTipoError():
 *   Devuelve la descripción o categoría del error asociado a la incidencia.
 */
public final class NifIncidencia {

    private final int idFilaExcel;
    private final String nifNie;
    private final String nombre;
    private final String apellido1;
    private final String apellido2;
    private final String tipoError;

    public NifIncidencia(int idFilaExcel, String nifNie, String nombre, String apellido1, String apellido2, String tipoError) {
        this.idFilaExcel = idFilaExcel;
        this.nifNie = nifNie;
        this.nombre = nombre;
        this.apellido1 = apellido1;
        this.apellido2 = apellido2;
        this.tipoError = tipoError;
    }

    public int getIdFilaExcel() {
        return idFilaExcel;
    }

    public String getNifNie() {
        return nifNie;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido1() {
        return apellido1;
    }

    public String getApellido2() {
        return apellido2;
    }

    public String getTipoError() {
        return tipoError;
    }
}