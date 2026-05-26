package com.mycompany.proyecto_si2.domain.service;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;

public class InformeRecibosQueriesService {

    private final EntityManager em;

    private static final String HQL_RESUMEN_AYUNTAMIENTOS =
            "select o.pueblo, count(o), avg(o.precioFijo) " +
            "from Ordenanza o " +
            "group by o.pueblo " +
            "order by o.pueblo";

    // HQL obligatoria para localizar el recibo objetivo
    private static final String HQL_RECIBO_MAXIMO_INFERIOR_MEDIA =
            "select r.numeroRecibo, r.totalRecibo, r.nombre, r.apellidos, " +
            "       r.nifContribuyente, r.direccionCompleta " +
            "from Recibos r " +
            "where r.totalRecibo = ( " +
            "    select max(r2.totalRecibo) " +
            "    from Recibos r2 " +
            "    where r2.totalRecibo < ( " +
            "        select avg(r3.totalRecibo) " +
            "        from Recibos r3 " +
            "    ) " +
            ") " +
            "order by r.numeroRecibo asc";

    // SQL nativa para obtener el pueblo/ayuntamiento correcto desde la ordenanza
    private static final String SQL_PUEBLO_POR_NUMERO_RECIBO =
            "select o.pueblo " +
            "from recibos r " +
            "join rel_contribuyente_ordenanza rel on rel.idContribuyente = r.idContribuyente " +
            "join ordenanza o on o.id = rel.idOrdenanza " +
            "where r.numeroRecibo = ? " +
            "order by o.pueblo asc " +
            "limit 1";

    public InformeRecibosQueriesService(EntityManager em) {
        this.em = em;
    }

    public List<ResumenAyuntamientoDTO> obtenerResumenAyuntamientos() {
        List<ResumenAyuntamientoDTO> resultado = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Object[]> filas = em.createQuery(HQL_RESUMEN_AYUNTAMIENTOS).getResultList();

        for (Object[] fila : filas) {
            String ayuntamiento = fila[0] != null ? fila[0].toString() : "";
            long numeroFilas = fila[1] != null ? ((Number) fila[1]).longValue() : 0L;
            double importeMedio = fila[2] != null ? ((Number) fila[2]).doubleValue() : 0.0;

            resultado.add(new ResumenAyuntamientoDTO(ayuntamiento, numeroFilas, importeMedio));
        }

        return resultado;
    }

    public ReciboObjetivoDTO obtenerReciboMaximoInferiorMedia() {
        @SuppressWarnings("unchecked")
        List<Object[]> filas = em.createQuery(HQL_RECIBO_MAXIMO_INFERIOR_MEDIA)
                                 .setMaxResults(1)
                                 .getResultList();

        if (filas == null || filas.isEmpty()) {
            return null;
        }

        Object[] fila = filas.get(0);

        Integer numeroRecibo = fila[0] != null ? ((Number) fila[0]).intValue() : null;
        double totalRecibo = fila[1] != null ? ((Number) fila[1]).doubleValue() : 0.0;
        String nombre = fila[2] != null ? fila[2].toString() : "";
        String apellidos = fila[3] != null ? fila[3].toString() : "";
        String nif = fila[4] != null ? fila[4].toString() : "";
        String direccion = fila[5] != null ? fila[5].toString() : "";

        String ayuntamiento = "NO LOCALIZADO";

        if (numeroRecibo != null) {
            @SuppressWarnings("unchecked")
            List<Object> pueblos = em.createNativeQuery(SQL_PUEBLO_POR_NUMERO_RECIBO)
                                     .setParameter(1, numeroRecibo)
                                     .getResultList();

            if (pueblos != null && !pueblos.isEmpty() && pueblos.get(0) != null) {
                ayuntamiento = pueblos.get(0).toString();
            }
        }

        return new ReciboObjetivoDTO(totalRecibo, nombre, apellidos, nif, direccion, ayuntamiento);
    }

    public String getHqlReciboMaximoInferiorMedia() {
        return HQL_RECIBO_MAXIMO_INFERIOR_MEDIA;
    }

    public static class ResumenAyuntamientoDTO {
        private final String ayuntamiento;
        private final long numeroFilasOrdenanza;
        private final double importeMedio;

        public ResumenAyuntamientoDTO(String ayuntamiento, long numeroFilasOrdenanza, double importeMedio) {
            this.ayuntamiento = ayuntamiento;
            this.numeroFilasOrdenanza = numeroFilasOrdenanza;
            this.importeMedio = importeMedio;
        }

        public String getAyuntamiento() {
            return ayuntamiento;
        }

        public long getNumeroFilasOrdenanza() {
            return numeroFilasOrdenanza;
        }

        public double getImporteMedio() {
            return importeMedio;
        }
    }

    public static class ReciboObjetivoDTO {
        private final double totalRecibo;
        private final String nombre;
        private final String apellidos;
        private final String nif;
        private final String direccion;
        private final String ayuntamiento;

        public ReciboObjetivoDTO(double totalRecibo, String nombre, String apellidos,
                                 String nif, String direccion, String ayuntamiento) {
            this.totalRecibo = totalRecibo;
            this.nombre = nombre;
            this.apellidos = apellidos;
            this.nif = nif;
            this.direccion = direccion;
            this.ayuntamiento = ayuntamiento;
        }

        public double getTotalRecibo() {
            return totalRecibo;
        }

        public String getNombre() {
            return nombre;
        }

        public String getApellidos() {
            return apellidos;
        }

        public String getNif() {
            return nif;
        }

        public String getDireccion() {
            return direccion;
        }

        public String getAyuntamiento() {
            return ayuntamiento;
        }
    }
}