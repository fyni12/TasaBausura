package com.mycompany.proyecto_si2.domain.model;
/**
 * Clase de modelo que representa un recibo asociado a un contribuyente.
 * Su finalidad es almacenar toda la información relevante del recibo, como su identificador, los datos
 * personales del titular, la cuenta bancaria, los kilogramos generados y los importes económicos
 * calculados, permitiendo acceder y modificar estos valores mediante métodos getter y setter propios
 * de una clase tipo POJO. [campus.datacamp](https://campus.datacamp.com/courses/data-types-and-exceptions-in-java/advanced-java-types?ex=1)
 *
 * Funciones de la clase:
 *
 * - Recibo():
 *   Constructor vacío que permite crear una instancia del recibo sin inicializar sus atributos en el
 *   momento de la creación. [campus.datacamp](https://campus.datacamp.com/courses/data-types-and-exceptions-in-java/advanced-java-types?ex=1)
 *
 * - getIdRecibo():
 *   Devuelve el identificador del recibo. [docs.oracle](https://docs.oracle.com/javaee/7/tutorial/cdi-basic010.htm)
 *
 * - setIdRecibo(int idRecibo):
 *   Asigna el identificador del recibo. [docs.oracle](https://docs.oracle.com/javaee/7/tutorial/cdi-basic010.htm)
 *
 * - getExencion():
 *   Devuelve la información relativa a la exención aplicada al recibo. [campus.datacamp](https://campus.datacamp.com/courses/data-types-and-exceptions-in-java/advanced-java-types?ex=1)
 *
 * - setExencion(String exencion):
 *   Asigna la información relativa a la exención aplicada al recibo. [docs.oracle](https://docs.oracle.com/javaee/7/tutorial/cdi-basic010.htm)
 *
 * - getIdFilaExcel():
 *   Devuelve el número o identificador de la fila del Excel de la que procede el recibo. [campus.datacamp](https://campus.datacamp.com/courses/data-types-and-exceptions-in-java/advanced-java-types?ex=1)
 *
 * - setIdFilaExcel(int idFilaExcel):
 *   Asigna el número o identificador de la fila del Excel de la que procede el recibo. [docs.oracle](https://docs.oracle.com/javaee/7/tutorial/cdi-basic010.htm)
 *
 * - getNombre():
 *   Devuelve el nombre del titular del recibo. [campus.datacamp](https://campus.datacamp.com/courses/data-types-and-exceptions-in-java/advanced-java-types?ex=1)
 *
 * - setNombre(String nombre):
 *   Asigna el nombre del titular del recibo. [docs.oracle](https://docs.oracle.com/javaee/7/tutorial/cdi-basic010.htm)
 *
 * - getPrimerApellido():
 *   Devuelve el primer apellido del titular del recibo. [campus.datacamp](https://campus.datacamp.com/courses/data-types-and-exceptions-in-java/advanced-java-types?ex=1)
 *
 * - setPrimerApellido(String primerApellido):
 *   Asigna el primer apellido del titular del recibo. [docs.oracle](https://docs.oracle.com/javaee/7/tutorial/cdi-basic010.htm)
 *
 * - getSegundoApellido():
 *   Devuelve el segundo apellido del titular del recibo. [campus.datacamp](https://campus.datacamp.com/courses/data-types-and-exceptions-in-java/advanced-java-types?ex=1)
 *
 * - setSegundoApellido(String segundoApellido):
 *   Asigna el segundo apellido del titular del recibo. [docs.oracle](https://docs.oracle.com/javaee/7/tutorial/cdi-basic010.htm)
 *
 * - getNif():
 *   Devuelve el NIF asociado al titular del recibo. [campus.datacamp](https://campus.datacamp.com/courses/data-types-and-exceptions-in-java/advanced-java-types?ex=1)
 *
 * - setNif(String nif):
 *   Asigna el NIF asociado al titular del recibo. [docs.oracle](https://docs.oracle.com/javaee/7/tutorial/cdi-basic010.htm)
 *
 * - getIban():
 *   Devuelve el IBAN asociado al recibo para la domiciliación o referencia bancaria. [campus.datacamp](https://campus.datacamp.com/courses/data-types-and-exceptions-in-java/advanced-java-types?ex=1)
 *
 * - setIban(String iban):
 *   Asigna el IBAN asociado al recibo. [docs.oracle](https://docs.oracle.com/javaee/7/tutorial/cdi-basic010.htm)
 *
 * - getKgGenerados():
 *   Devuelve la cantidad de kilogramos generados asociada al cálculo del recibo. [campus.datacamp](https://campus.datacamp.com/courses/data-types-and-exceptions-in-java/advanced-java-types?ex=1)
 *
 * - setKgGenerados(int kgGenerados):
 *   Asigna la cantidad de kilogramos generados asociada al cálculo del recibo. [docs.oracle](https://docs.oracle.com/javaee/7/tutorial/cdi-basic010.htm)
 *
 * - getBaseImponibleRecibo():
 *   Devuelve la base imponible calculada para el recibo. [campus.datacamp](https://campus.datacamp.com/courses/data-types-and-exceptions-in-java/advanced-java-types?ex=1)
 *
 * - setBaseImponibleRecibo(double baseImponibleRecibo):
 *   Asigna la base imponible calculada para el recibo. [docs.oracle](https://docs.oracle.com/javaee/7/tutorial/cdi-basic010.htm)
 *
 * - getIvaRecibo():
 *   Devuelve el importe del IVA correspondiente al recibo. [campus.datacamp](https://campus.datacamp.com/courses/data-types-and-exceptions-in-java/advanced-java-types?ex=1)
 *
 * - setIvaRecibo(double ivaRecibo):
 *   Asigna el importe del IVA correspondiente al recibo. [docs.oracle](https://docs.oracle.com/javaee/7/tutorial/cdi-basic010.htm)
 *
 * - getTotalRecibo():
 *   Devuelve el importe total del recibo, normalmente resultado de sumar la base imponible y el IVA. [campus.datacamp](https://campus.datacamp.com/courses/data-types-and-exceptions-in-java/advanced-java-types?ex=1)
 *
 * - setTotalRecibo(double totalRecibo):
 *   Asigna el importe total del recibo. [docs.oracle](https://docs.oracle.com/javaee/7/tutorial/cdi-basic010.htm)
 */
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