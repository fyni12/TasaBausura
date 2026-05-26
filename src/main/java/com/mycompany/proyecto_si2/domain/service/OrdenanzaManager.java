package com.mycompany.proyecto_si2.domain.service;

import POJOS.Ordenanza;
import com.mycompany.proyecto_si2.infra.pdf.PDFGenerator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Clase de servicio encargada de gestionar las ordenanzas y de calcular los importes económicos
 * asociados a un concepto de recibo. Su función principal es almacenar las ordenanzas cargadas,
 * organizarlas por identificador y aplicar la lógica necesaria para obtener la base imponible,
 * el IVA, el total y el detalle de líneas que aparecerán en el PDF del recibo.
 *
 * Funciones de la clase:
 *
 * - OrdenanzaManager():
 *   Constructor vacío que crea una instancia preparada para almacenar y gestionar ordenanzas.
 *
 * - add(Ordenanza ordenanza):
 *   Añade una ordenanza a la colección interna y marca el índice como no actualizado para que
 *   vuelva a generarse cuando sea necesario.
 *
 * - calculate(int bonificacion, int kgGen, int id):
 *   Calcula la base imponible final de una ordenanza aplicando, si corresponde, la bonificación
 *   indicada sobre los kilogramos generados y el identificador del concepto.
 *
 * - calculateIva(int bonificacion, int kgGen, int id):
 *   Calcula el importe del IVA correspondiente a una ordenanza a partir de la base bonificada
 *   y del porcentaje de IVA asociado al identificador indicado.
 *
 * - calculateTotal(int bonificacion, int kgGen, int id):
 *   Calcula el importe total del concepto sumando la base imponible bonificada y el IVA resultante.
 *
 * - getIva(int id):
 *   Devuelve el porcentaje de IVA asociado a las ordenanzas de un identificador concreto. Si no
 *   existen ordenanzas para ese identificador, devuelve cero.
 *
 * - calcularBaseBonificada(int id, int kgGen, int bonificacion):
 *   Método auxiliar privado que calcula la base imponible final aplicando la bonificación
 *   correspondiente sobre la base sin bonificar.
 *
 * - calcularBaseSinBonificar(int id, int kgGen):
 *   Método auxiliar privado que calcula la base imponible sin descuentos. Tiene en cuenta el precio
 *   fijo, los posibles conceptos relacionados y el importe variable en función de los kilogramos.
 *
 * - calcularImportePorKg(List<Ordenanza> aplicables, int kgGenerados):
 *   Calcula la parte variable del importe según los tramos de kilos definidos en las ordenanzas.
 *   Determina si el cálculo debe hacerse por tramo único o de forma progresiva.
 *
 * - calcularTramosProgresivos(List<Ordenanza> tramos, int exceso):
 *   Reparte el exceso de kilogramos entre los distintos tramos acumulables y suma el importe
 *   correspondiente a cada uno de ellos.
 *
 * - calcularTramoUnico(List<Ordenanza> tramos, int kgGenerados, int exceso):
 *   Selecciona el tramo que corresponde según el exceso de kilogramos y aplica un único precio
 *   por kilogramo al total de kilos generados.
 *
 * - getOrdenanzasById(int id):
 *   Recupera la lista de ordenanzas asociadas a un identificador concreto, reindexando previamente
 *   la colección si fuera necesario.
 *
 * - reindexar():
 *   Reconstruye el índice interno agrupando las ordenanzas por su identificador y ordenándolas
 *   para que las que tengan precio fijo queden en primer lugar.
 *
 * - getTramos(List<Ordenanza> aplicables):
 *   Devuelve únicamente las ordenanzas que representan tramos variables por precio por kilogramo.
 *
 * - getKgMinimoIncluido(List<Ordenanza> aplicables):
 *   Obtiene la cantidad mínima de kilogramos incluidos en el precio fijo de una ordenanza.
 *
 * - obtenerPrecioFijo(List<Ordenanza> aplicables):
 *   Devuelve el precio fijo definido para un conjunto de ordenanzas, o cero si no existe.
 *
 * - esProgresivo(List<Ordenanza> aplicables):
 *   Determina si una ordenanza debe calcularse de forma acumulable o progresiva en función
 *   de su configuración.
 *
 * - isAcumulable(String acu):
 *   Interpreta el valor textual del campo acumulable y devuelve si indica que la ordenanza
 *   debe tratarse como acumulable.
 *
 * - normalizarKgTramo(Integer kg):
 *   Normaliza el valor de kilos de un tramo, devolviendo un valor máximo cuando no exista
 *   límite definido o el dato no sea válido.
 *
 * - normalizarKgOrdenacion(Integer kg):
 *   Normaliza el valor de kilos pensado para ordenación interna de tramos, usando la misma
 *   lógica de control de límites.
 *
 * - porcentaje(double valor):
 *   Convierte un valor porcentual decimal a su equivalente en formato BigDecimal.
 *
 * - porcentaje(int valor):
 *   Convierte un valor porcentual entero a su equivalente en formato BigDecimal.
 *
 * - bd(double valor):
 *   Convierte un valor numérico de tipo double a BigDecimal.
 *
 * - redondear(BigDecimal valor):
 *   Redondea un valor BigDecimal a dos decimales usando el modo HALF_UP.
 *
 * - buildPdfLineas(int bonificacion, int kgGen, int id):
 *   Genera la lista de líneas de concepto que se incluirán en el PDF del recibo. Construye
 *   el detalle económico teniendo en cuenta el precio fijo, los conceptos relacionados, la
 *   bonificación y la parte variable por kilogramos.
 *
 * - linea(String concepto, String subconcepto, BigDecimal kg, BigDecimal base, BigDecimal ivaPct):
 *   Crea y rellena una línea de concepto para el PDF con su descripción, kilos, base imponible,
 *   porcentaje de IVA e importe de IVA calculado.
 *
 * - decorateSubconcepto(String subconcepto, int bonificacion):
 *   Añade al subconcepto una indicación textual de la bonificación aplicada cuando exista.
 *
 * - safe(String value):
 *   Devuelve una cadena segura, evitando valores nulos y eliminando espacios sobrantes.
 */
public class OrdenanzaManager {

    private final List<Ordenanza> ordenanzas = new ArrayList<>();
    private final Map<Integer, List<Ordenanza>> indexadas = new HashMap<>();
    private boolean indexed = false;

    public OrdenanzaManager() {
    }

    public void add(Ordenanza ordenanza) {
        ordenanzas.add(ordenanza);
        indexed = false;
    }

    public double calculate(int bonificacion, int kgGen, int id) {
        return calcularBaseBonificada(id, kgGen, bonificacion).doubleValue();
    }

    public double calculateIva(int bonificacion, int kgGen, int id) {
        BigDecimal base = calcularBaseBonificada(id, kgGen, bonificacion);
        
        BigDecimal iva = porcentaje(getIva(id));
        return redondear(base.multiply(iva)).doubleValue();
    }

    public double calculateTotal(int bonificacion, int kgGen, int id) {
        BigDecimal base = calcularBaseBonificada(id, kgGen, bonificacion);
       
        BigDecimal iva = redondear(base.multiply(porcentaje(getIva(id))));
        return redondear(base.add(iva)).doubleValue();
    }

    public double getIva(int id) {
        List<Ordenanza> aplicables = getOrdenanzasById(id);
        if (aplicables.isEmpty()) {
            return 0d;
        }
        return aplicables.get(0).getIva();
    }

    private BigDecimal calcularBaseBonificada(int id, int kgGen, int bonificacion) {
        BigDecimal base = calcularBaseSinBonificar(id, kgGen);
        
        

        if (bonificacion > 0) {
            BigDecimal factor = BigDecimal.ONE.subtract(porcentaje(bonificacion));
            base = base.multiply(factor);
        }
        return redondear(base);
    }

    private BigDecimal calcularBaseSinBonificar(int id, int kgGen) {
        List<Ordenanza> aplicables = getOrdenanzasById(id);

        if (aplicables.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int kgOriginal = Math.max(0, kgGen);
        Ordenanza cabecera = aplicables.get(0);

        BigDecimal base = obtenerPrecioFijo(aplicables);

        if (cabecera.getConceptoRelacionado() != 0) {
            
            BigDecimal baseRelacionado = calcularBaseSinBonificar(cabecera.getConceptoRelacionado(), kgOriginal);
            BigDecimal porcentajeRelacionado = porcentaje(cabecera.getPorcentaje());
            return redondear(base.add(baseRelacionado.multiply(porcentajeRelacionado)));
        }
        base = base.add(calcularImportePorKg(aplicables, kgOriginal));
        return redondear(base);
    }

   private BigDecimal calcularImportePorKg(List<Ordenanza> aplicables, int kgGenerados) {
    List<Ordenanza> tramos = getTramos(aplicables);

    if (tramos.isEmpty()) {
        return BigDecimal.ZERO;
    }

    int kgMinimoIncluido = getKgMinimoIncluido(aplicables);
    if (kgGenerados <= kgMinimoIncluido) {
        return BigDecimal.ZERO;
    }

    boolean acumulable = esProgresivo(aplicables);
    int exceso = kgGenerados - kgMinimoIncluido; // ← CORRECCIÓN: restar siempre

    if (acumulable) {
        return calcularTramoUnico(tramos, kgGenerados, exceso);
    } else {
        return calcularTramosProgresivos(tramos, exceso); // ahora recibe 111, no 121
    }
}

    private BigDecimal calcularTramosProgresivos(List<Ordenanza> tramos, int exceso) {
        int restante = exceso;
        BigDecimal lastPrice=BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (Ordenanza tramo : tramos) {
            if (restante <= 0) {
                break;
            }

            int ancho = normalizarKgTramo(tramo.getKgincluidos());
            BigDecimal precioKg = bd(tramo.getPreciokg());

            int kgEnTramo;
            if (ancho == Integer.MAX_VALUE) {
                kgEnTramo = restante;
            } else {
                kgEnTramo = Math.min(restante, ancho);
            }

            total = total.add(precioKg.multiply(BigDecimal.valueOf(kgEnTramo)));
            restante -= kgEnTramo;
            lastPrice=precioKg;
        }
        
        
        if(restante>0){
           total= total.add(lastPrice.multiply(new BigDecimal(restante)));
        }
        
        

        return redondear(total);
    }

   private BigDecimal calcularTramoUnico(List<Ordenanza> tramos, int kgGenerados, int exceso) {
    int pendiente = exceso;
    BigDecimal precioSeleccionado = BigDecimal.ZERO;

    for (Ordenanza tramo : tramos) {
        int ancho = normalizarKgTramo(tramo.getKgincluidos());

        if (ancho == Integer.MAX_VALUE || pendiente <= ancho) {
            precioSeleccionado = bd(tramo.getPreciokg());
            break;
        }

        pendiente -= ancho;
    }

    if (precioSeleccionado.compareTo(BigDecimal.ZERO) == 0) {
        precioSeleccionado = bd(tramos.get(tramos.size() - 1).getPreciokg());
    }

    return redondear(precioSeleccionado.multiply(BigDecimal.valueOf(kgGenerados)));
}
    


    private List<Ordenanza> getOrdenanzasById(int id) {
        if (!indexed) {
            reindexar();
        }
        return indexadas.getOrDefault(id, List.of());
    }

    private void reindexar() {
    indexadas.clear();

    for (Ordenanza ord : ordenanzas) {
        indexadas.computeIfAbsent(ord.getIdOrdenanza(), k -> new ArrayList<>()).add(ord);
    }

    for (List<Ordenanza> lista : indexadas.values()) {
        lista.sort(Comparator.comparing((Ordenanza o) -> o.getPrecioFijo() > 0 ? 0 : 1));
    }

    indexed = true;
}

    private List<Ordenanza> getTramos(List<Ordenanza> aplicables) {
    List<Ordenanza> tramos = new ArrayList<>();

    for (Ordenanza ord : aplicables) {
        if (ord.getPreciokg() > 0) {
            tramos.add(ord);
        }
    }

    return tramos;
}

    private int getKgMinimoIncluido(List<Ordenanza> aplicables) {
        for (Ordenanza ord : aplicables) {
            if (ord.getPrecioFijo() > 0) {
                return Math.max(0, ord.getKgincluidos());
            }
        }
        return 0;
    }

    private BigDecimal obtenerPrecioFijo(List<Ordenanza> aplicables) {
        for (Ordenanza ord : aplicables) {
            if (ord.getPrecioFijo() > 0) {
                return bd(ord.getPrecioFijo());
            }
        }
        return BigDecimal.ZERO;
    }

    private boolean esProgresivo(List<Ordenanza> aplicables) {
        for (Ordenanza ord : aplicables) {
            if (ord.getAcumulable() != null && !ord.getAcumulable().isBlank()) {
                return isAcumulable(ord.getAcumulable());
            }
        }
        return false;
    }

    private boolean isAcumulable(String acu) {
        return Objects.equals("s", acu.trim().toLowerCase());
    }

    private int normalizarKgTramo(Integer kg) {
        if (kg == null || kg <= 0) {
            return Integer.MAX_VALUE;
        }
        return kg;
    }

    private int normalizarKgOrdenacion(Integer kg) {
        if (kg == null || kg <= 0) {
            return Integer.MAX_VALUE;
        }
        return kg;
    }

    private BigDecimal porcentaje(double valor) {
        return BigDecimal.valueOf(valor).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
    }

    private BigDecimal porcentaje(int valor) {
        return BigDecimal.valueOf(valor).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
    }

    private BigDecimal bd(double valor) {
        return BigDecimal.valueOf(valor);
    }

    private BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    public List<PDFGenerator.LineaConcepto> buildPdfLineas(int bonificacion, int kgGen, int id) {
        List<Ordenanza> aplicables = getOrdenanzasById(id);

        if (aplicables.isEmpty()) {
            return Collections.emptyList();
        }

        List<PDFGenerator.LineaConcepto> out = new ArrayList<>();
        Ordenanza cabecera = aplicables.get(0);
        BigDecimal ivaPct = bd(cabecera.getIva());
        BigDecimal factorBonif = BigDecimal.ONE.subtract(porcentaje(bonificacion));

        BigDecimal precioFijo = obtenerPrecioFijo(aplicables);
        if (precioFijo.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal baseFija = redondear(precioFijo.multiply(factorBonif));
            out.add(linea(
                    cabecera.getConcepto(),
                    decorateSubconcepto(cabecera.getSubconcepto(), bonificacion),
                    BigDecimal.ZERO,
                    baseFija,
                    ivaPct
            ));
        }

        if (cabecera.getConceptoRelacionado() != 0) {
            BigDecimal baseRelacionado = calcularBaseBonificada(cabecera.getConceptoRelacionado(), kgGen, bonificacion);
            BigDecimal baseRelacionada = redondear(baseRelacionado.multiply(porcentaje(cabecera.getPorcentaje())));

            out.add(linea(
                    cabecera.getConcepto(),
                    decorateSubconcepto(cabecera.getSubconcepto(), bonificacion),
                    BigDecimal.ZERO,
                    baseRelacionada,
                    ivaPct
            ));

            return out;
        }

        List<Ordenanza> tramos = getTramos(aplicables);
        if (tramos.isEmpty()) {
            return out;
        }

        int kgMinimoIncluido = getKgMinimoIncluido(aplicables);
        if (kgGen <= kgMinimoIncluido) {
            return out;
        }

        boolean acumulable = esProgresivo(aplicables);
        int exceso = kgGen - kgMinimoIncluido;

        if (acumulable) {
            int pendiente = exceso;
            Ordenanza seleccionada = tramos.get(tramos.size() - 1);

            for (Ordenanza tramo : tramos) {
                int ancho = normalizarKgTramo(tramo.getKgincluidos());

                if (ancho == Integer.MAX_VALUE || pendiente <= ancho) {
                    seleccionada = tramo;
                    break;
                }

                pendiente -= ancho;
            }

            BigDecimal base = redondear(
                    bd(seleccionada.getPreciokg())
                            .multiply(BigDecimal.valueOf(kgGen))
                            .multiply(factorBonif)
            );

            out.add(linea(
                    seleccionada.getConcepto(),
                    decorateSubconcepto(seleccionada.getSubconcepto(), bonificacion),
                    BigDecimal.valueOf(kgGen),
                    base,
                    ivaPct
            ));

            return out;
        }

        int restante = exceso;
        BigDecimal ultimoPrecio = BigDecimal.ZERO;
        Ordenanza ultimoTramo = tramos.get(tramos.size() - 1);

        for (Ordenanza tramo : tramos) {
            if (restante <= 0) {
                break;
            }

            int ancho = normalizarKgTramo(tramo.getKgincluidos());
            int kgEnTramo = (ancho == Integer.MAX_VALUE) ? restante : Math.min(restante, ancho);

            if (kgEnTramo <= 0) {
                continue;
            }

            BigDecimal precioKg = bd(tramo.getPreciokg());
            BigDecimal base = redondear(
                    precioKg.multiply(BigDecimal.valueOf(kgEnTramo)).multiply(factorBonif)
            );

            out.add(linea(
                    tramo.getConcepto(),
                    decorateSubconcepto(tramo.getSubconcepto(), bonificacion),
                    BigDecimal.valueOf(kgEnTramo),
                    base,
                    ivaPct
            ));

            restante -= kgEnTramo;
            ultimoPrecio = precioKg;
            ultimoTramo = tramo;
        }

        if (restante > 0) {
            BigDecimal baseExtra = redondear(
                    ultimoPrecio.multiply(BigDecimal.valueOf(restante)).multiply(factorBonif)
            );

            out.add(linea(
                    ultimoTramo.getConcepto(),
                    decorateSubconcepto(ultimoTramo.getSubconcepto(), bonificacion),
                    BigDecimal.valueOf(restante),
                    baseExtra,
                    ivaPct
            ));
        }

        return out;
    }

   

  

    private PDFGenerator.LineaConcepto linea(String concepto, String subconcepto,
                                             BigDecimal kg, BigDecimal base, BigDecimal ivaPct) {
        PDFGenerator.LineaConcepto l = new PDFGenerator.LineaConcepto();
        l.concepto = safe(concepto);
        l.subconcepto = safe(subconcepto);
        l.kgIncluidos = kg == null ? BigDecimal.ZERO : kg.setScale(2, RoundingMode.HALF_UP);
        l.baseImponible = base == null ? BigDecimal.ZERO : base.setScale(2, RoundingMode.HALF_UP);
        l.porcentajeIva = ivaPct == null ? BigDecimal.ZERO : ivaPct.setScale(2, RoundingMode.HALF_UP);
        l.importeIva = l.baseImponible.multiply(l.porcentajeIva)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return l;
    }

    private String decorateSubconcepto(String subconcepto, int bonificacion) {
        if (bonificacion > 0) {
            return safe(subconcepto) + " (Bonif. " + bonificacion + "%)";
        }
        return safe(subconcepto);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    
}