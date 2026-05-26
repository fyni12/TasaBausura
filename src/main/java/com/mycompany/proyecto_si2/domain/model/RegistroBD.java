package com.mycompany.proyecto_si2.domain.model;

import com.mycompany.proyecto_si2.infra.pdf.PDFGenerator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
/**
 * Clase de datos que representa un registro intermedio con toda la información necesaria para persistir
 * en base de datos y generar la documentación asociada a un recibo. Su función es agrupar en una sola
 * estructura los datos identificativos del contribuyente, los datos del período, la información económica,
 * las fechas relevantes y las colecciones de conceptos y líneas que intervienen en el procesamiento del
 * recibo.
 *
 * Funciones de la clase:
 *
 * - RegistroBD():
 *   Constructor por defecto implícito que permite crear una instancia vacía del registro.
 *
 * Esta clase no define métodos propios de comportamiento, sino que actúa como contenedor de datos
 * tipo DTO o estructura de transporte, dejando sus atributos públicos para facilitar la carga, lectura
 * y transferencia de la información entre distintas capas de la aplicación.
 */
public class RegistroBD {

    public int numeroReciboTemporal;
    public int trimestre;
    public int anio;

    public String nif;
    public String nombre;
    public String apellido1;
    public String apellido2;
    public String direccion;
    public String numero;
    public String ayuntamiento;
    public String iban;
    public String paisCCC;
    public String ccc;
    public String exencion;
    public String email;
    public String idContribuyente;

    public int bonificacion;
    public int kgGenerados;

    public LocalDate fechaAlta;
    public LocalDate fechaBaja;
    public LocalDate fechaPadron;
    public LocalDate fechaRecibo;

    public BigDecimal baseImponible;
    public BigDecimal iva;
    public BigDecimal totalRecibo;

    public List<Integer> idsConcepto = new ArrayList<>();
    public List<PDFGenerator.LineaConcepto> lineas = new ArrayList<>();
}