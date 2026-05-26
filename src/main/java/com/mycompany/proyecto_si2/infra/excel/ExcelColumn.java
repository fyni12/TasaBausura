package com.mycompany.proyecto_si2.infra.excel;
/**
 * Enumeración que define las columnas utilizadas en las hojas Excel del proyecto.
 * Su objetivo es centralizar los nombres de cabecera asociados a cada campo para facilitar
 * el acceso consistente a los datos del fichero Excel y evitar el uso de cadenas literales
 * dispersas por el código.
 *
 * Funciones de la clase:
 *
 * - ExcelColumn(String header):
 *   Constructor del enumerado que asocia a cada constante el nombre exacto de la cabecera
 *   que debe utilizarse en el fichero Excel.
 *
 * - getHeader():
 *   Devuelve el texto de la cabecera asociado a la columna concreta del enumerado, permitiendo
 *   identificarla y utilizarla al leer o escribir datos en el Excel.
 */
public enum ExcelColumn {
    DIRECCION("Direccion"),
    NUMERO("Numero"),
    PAIS_CCC("PaisCCC"),
    NOMBRE("Nombre"),
    APELLIDO1("Apellido1"),
    APELLIDO2("Apellido2"),
    NIFNIE("NIFNIE"),
    CCC("CCC"),
    IBAN("IBAN"),
    EMAIL("Email"),
    EXENCION("Exencion"),
    BONIFICACION("Bonificacion"),
    KG_GENERADOS("kg generados"),
    FECHA_ALTA("FechaAlta"),
    FECHA_BAJA("FechaBaja"),
    CONCEPTOS_A_COBRAR("conceptosACobrar"),
    ID_ORDENANZA("idOrdenanza"),
    CONCEPTO("Concepto"),
    SUBCONCEPTO("Subconcepto"),
    DESCRIPCION("Descripcion"),
    PUEBLO("Pueblo"),
    TIPO_CALCULO("TipoCalculo"),
    ACUMULABLE("Acumulable"),
    PRECIO_FIJO("Precio fijo"),
    KG_INCLUIDOS("kg incluidos"),
    PRECIO_KG("Precio kg"),
    PORCENTAJE_SOBRE_OTRO_CONCEPTO("PorcentajeSobreOtroConcepto"),
    SOBRE_QUE_CONCEPTO("SobreQueConcepto"),
    IVA("IVA");
    

    private final String header;

    ExcelColumn(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }
}