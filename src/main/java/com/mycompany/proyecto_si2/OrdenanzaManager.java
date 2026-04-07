package com.mycompany.proyecto_si2;

import POJOS.Ordenanza;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        System.out.println(base.toString());
        BigDecimal iva = porcentaje(getIva(id));
        return redondear(base.multiply(iva)).doubleValue();
    }

    public double calculateTotal(int bonificacion, int kgGen, int id) {
        BigDecimal base = calcularBaseBonificada(id, kgGen, bonificacion);
        System.out.println("hola buenos dias");
        System.out.println(id+"- "+base);
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
    int exceso = kgGenerados - kgMinimoIncluido;

    if (acumulable) {
        return calcularTramoUnico(tramos, kgGenerados, exceso);
    } else {
        return calcularTramosProgresivos(tramos, exceso);
    }
}

    private BigDecimal calcularTramosProgresivos(List<Ordenanza> tramos, int exceso) {
        int restante = exceso;
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
        }

        return redondear(total);
    }

    private BigDecimal calcularTramoUnico(List<Ordenanza> tramos, int kgGenerados, int exceso) {
        int pendiente = exceso;
        BigDecimal precioSeleccionado = bd(tramos.get(tramos.size() - 1).getPreciokg());

        for (Ordenanza tramo : tramos) {
            int ancho = normalizarKgTramo(tramo.getKgincluidos());
            precioSeleccionado = bd(tramo.getPreciokg());

            if (ancho == Integer.MAX_VALUE || pendiente <= ancho) {
                break;
            }

            pendiente -= ancho;
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
            lista.sort(
                Comparator
                    .comparing((Ordenanza o) -> o.getPrecioFijo() > 0 ? 0 : 1)
                    .thenComparing(o -> normalizarKgOrdenacion(o.getKgincluidos()))
            );
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

        tramos.sort(Comparator.comparing(o -> normalizarKgOrdenacion(o.getKgincluidos())));
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
}