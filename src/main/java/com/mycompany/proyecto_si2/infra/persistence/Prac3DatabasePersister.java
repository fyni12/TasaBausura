package com.mycompany.proyecto_si2.infra.persistence;

import com.mycompany.proyecto_si2.infra.pdf.PDFGenerator;
import com.mycompany.proyecto_si2.domain.model.RegistroBD;
import POJOS.Ordenanza;
import com.mycompany.proyecto_si2.support.Prac3Support;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Clase encargada de persistir en la base de datos toda la información generada durante el procesamiento
 * de la práctica 3. Su función principal es recorrer los registros calculados, insertar o actualizar
 * contribuyentes, lecturas, ordenanzas, recibos, líneas de recibo y relaciones entre contribuyentes
 * y ordenanzas, todo ello dentro de una transacción controlada.
 *
 * Funciones de la clase:
 *
 * - Prac3DatabasePersister(EntityManager em):
 *   Constructor que recibe y almacena el EntityManager que se utilizará para ejecutar todas las
 *   operaciones de persistencia contra la base de datos.
 *
 * - persistirRegistrosBD(List<RegistroBD> registros, Map<Integer, List<Ordenanza>> ordenanzasPorId):
 *   Método principal que persiste en base de datos la colección completa de registros procesados.
 *   Inicia una transacción, evita repetir inserciones innecesarias de contribuyentes y ordenanzas,
 *   realiza las operaciones de inserción o actualización necesarias para cada registro y confirma la
 *   transacción al finalizar correctamente. Si ocurre un error, revierte todos los cambios.
 *
 * - upsertContribuyente(RegistroBD r):
 *   Método auxiliar privado que inserta un nuevo contribuyente o actualiza uno existente en función
 *   de su nombre, NIF y fecha de alta. También actualiza el resto de datos personales y administrativos
 *   cuando el contribuyente ya existe en la base de datos.
 *
 * - upsertLectura(RegistroBD r):
 *   Método auxiliar privado que inserta o actualiza la lectura de kilogramos generados de un contribuyente
 *   para un ejercicio y trimestre concretos.
 *
 * - upsertOrdenanza(Ordenanza ord):
 *   Método auxiliar privado que inserta o actualiza una ordenanza en la base de datos, identificándola
 *   por su id de ordenanza, concepto y subconcepto.
 *
 * - upsertRecibo(RegistroBD r):
 *   Método auxiliar privado que inserta o actualiza un recibo en función del NIF del contribuyente y
 *   del período del padrón. Devuelve el número real del recibo almacenado en la base de datos.
 *
 * - upsertLineasRecibo(int numeroRecibo, List<PDFGenerator.LineaConcepto> lineas, int bonificacion):
 *   Método auxiliar privado que inserta o actualiza todas las líneas de detalle asociadas a un recibo,
 *   incluyendo base imponible, IVA, kilos incluidos y bonificación aplicada.
 *
 * - obtenerIdContribuyenteBD(String nombre, String nif, LocalDate fechaAlta):
 *   Busca en la base de datos el identificador de un contribuyente a partir de su nombre, NIF y fecha
 *   de alta, devolviendo dicho identificador en formato texto o null si no existe.
 *
 * - upsertRelContribuyenteOrdenanza(Integer idContribuyente, Integer idOrdenanzaBd):
 *   Método auxiliar privado que crea la relación entre un contribuyente y una ordenanza si dicha relación
 *   todavía no existe en la tabla intermedia correspondiente.
 *
 * - obtenerIdOrdenanzaBD(Ordenanza ord):
 *   Método auxiliar privado que recupera el identificador interno en base de datos de una ordenanza
 *   concreta a partir de su id lógico, concepto y subconcepto.
 */
public class Prac3DatabasePersister {

    private final EntityManager em;

    public Prac3DatabasePersister(EntityManager em) {
        this.em = em;
    }

    public void persistirRegistrosBD(List<RegistroBD> registros, Map<Integer, List<Ordenanza>> ordenanzasPorId) throws Exception {
        if (registros.isEmpty()) {
            return;
        }

        em.getTransaction().begin();
        try {
            Set<String> ordenanzasPersistidas = new HashSet<>();
            Set<String> contribuyentesProcesados = new HashSet<>();

            for (RegistroBD r : registros) {
                String keyContribuyente = Prac3Support.safe(r.nombre) + "|" + Prac3Support.safe(r.nif) + "|" + r.fechaAlta;

                if (contribuyentesProcesados.add(keyContribuyente)) {
                    upsertContribuyente(r);
                }

                r.idContribuyente = obtenerIdContribuyenteBD(r.nombre, r.nif, r.fechaAlta);
                upsertLectura(r);

                for (Integer idConcepto : r.idsConcepto) {
                    List<Ordenanza> lista = ordenanzasPorId.getOrDefault(idConcepto, List.of());
                    for (Ordenanza ord : lista) {
                        String key = ord.getIdOrdenanza() + "|" + Prac3Support.safe(ord.getConcepto()) + "|" + Prac3Support.safe(ord.getSubconcepto());
                        if (ordenanzasPersistidas.add(key)) {
                            upsertOrdenanza(ord);
                        }
                    }
                }

                int numeroReciboReal = upsertRecibo(r);
                upsertLineasRecibo(numeroReciboReal, r.lineas, r.bonificacion);

                if (r.idContribuyente != null && !r.idContribuyente.isBlank()) {
                    Integer idContribuyente = Integer.valueOf(r.idContribuyente);

                    for (Integer idConcepto : r.idsConcepto) {
                        List<Ordenanza> lista = ordenanzasPorId.getOrDefault(idConcepto, List.of());
                        for (Ordenanza ord : lista) {
                            Integer idOrdenanzaBd = obtenerIdOrdenanzaBD(ord);
                            upsertRelContribuyenteOrdenanza(idContribuyente, idOrdenanzaBd);
                        }
                    }
                }
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    private void upsertContribuyente(RegistroBD r) {
        Query q = em.createNativeQuery(
                "SELECT idContribuyente "
                + "FROM contribuyente "
                + "WHERE nombre = ? AND NIFNIE = ? AND fechaAlta = ?"
        );
        q.setParameter(1, Prac3Support.safe(r.nombre));
        q.setParameter(2, Prac3Support.safe(r.nif));
        q.setParameter(3, Date.valueOf(r.fechaAlta));

        List<?> res = q.getResultList();

        if (!res.isEmpty()) {
            Object id = res.get(0);

            Query update = em.createNativeQuery(
                    "UPDATE contribuyente "
                    + "SET apellido1 = ?, "
                    + "apellido2 = ?, "
                    + "direccion = ?, "
                    + "numero = ?, "
                    + "paisCCC = ?, "
                    + "CCC = ?, "
                    + "IBAN = ?, "
                    + "eEmail = ?, "
                    + "exencion = ?, "
                    + "bonificacion = ?, "
                    + "fechaBaja = ? "
                    + "WHERE idContribuyente = ?"
            );
            update.setParameter(1, Prac3Support.emptyToNull(r.apellido1));
            update.setParameter(2, Prac3Support.emptyToNull(r.apellido2));
            update.setParameter(3, Prac3Support.emptyToNull(r.direccion));
            update.setParameter(4, Prac3Support.emptyToNull(r.numero));
            update.setParameter(5, Prac3Support.emptyToNull(r.paisCCC));
            update.setParameter(6, Prac3Support.emptyToNull(r.ccc));
            update.setParameter(7, Prac3Support.emptyToNull(r.iban));
            update.setParameter(8, Prac3Support.emptyToNull(r.email));
            update.setParameter(9, Prac3Support.charOrNull(r.exencion));
            update.setParameter(10, Prac3Support.bonificacionOrNull(r.bonificacion));
            update.setParameter(11, r.fechaBaja != null ? Date.valueOf(r.fechaBaja) : null);
            update.setParameter(12, id);

            update.executeUpdate();
        } else {
            Query insert = em.createNativeQuery(
                    "INSERT INTO contribuyente "
                    + "(nombre, apellido1, apellido2, NIFNIE, direccion, numero, paisCCC, CCC, IBAN, eEmail, fechaAlta, fechaBaja, exencion, bonificacion) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            insert.setParameter(1, Prac3Support.safe(r.nombre));
            insert.setParameter(2, Prac3Support.emptyToNull(r.apellido1));
            insert.setParameter(3, Prac3Support.emptyToNull(r.apellido2));
            insert.setParameter(4, Prac3Support.safe(r.nif));
            insert.setParameter(5, Prac3Support.emptyToNull(r.direccion));
            insert.setParameter(6, Prac3Support.emptyToNull(r.numero));
            insert.setParameter(7, Prac3Support.emptyToNull(r.paisCCC));
            insert.setParameter(8, Prac3Support.emptyToNull(r.ccc));
            insert.setParameter(9, Prac3Support.emptyToNull(r.iban));
            insert.setParameter(10, Prac3Support.emptyToNull(r.email));
            insert.setParameter(11, r.fechaAlta != null ? Date.valueOf(r.fechaAlta) : null);
            insert.setParameter(12, r.fechaBaja != null ? Date.valueOf(r.fechaBaja) : null);
            insert.setParameter(13, Prac3Support.charOrNull(r.exencion));
            insert.setParameter(14, Prac3Support.bonificacionOrNull(r.bonificacion));

            insert.executeUpdate();
        }
    }

    private void upsertLectura(RegistroBD r) {
        if (r.idContribuyente == null || r.idContribuyente.isBlank()) {
            return;
        }

        String ejercicio = String.valueOf(r.anio);
        String periodo = String.valueOf(r.trimestre);

        Query q = em.createNativeQuery(
                "SELECT COUNT(*) FROM lecturas WHERE idContribuyente = ? AND ejercicio = ? AND periodo = ?"
        );
        q.setParameter(1, r.idContribuyente);
        q.setParameter(2, ejercicio);
        q.setParameter(3, periodo);

        Number n = (Number) q.getSingleResult();

        if (n.intValue() > 0) {
            Query update = em.createNativeQuery(
                    "UPDATE lecturas SET kgGenerados = ? "
                    + "WHERE idContribuyente = ? AND ejercicio = ? AND periodo = ?"
            );
            update.setParameter(1, r.kgGenerados);
            update.setParameter(2, r.idContribuyente);
            update.setParameter(3, ejercicio);
            update.setParameter(4, periodo);
            update.executeUpdate();
        } else {
            Query insert = em.createNativeQuery(
                    "INSERT INTO lecturas (ejercicio, periodo, kgGenerados, idContribuyente) "
                    + "VALUES (?, ?, ?, ?)"
            );
            insert.setParameter(1, ejercicio);
            insert.setParameter(2, periodo);
            insert.setParameter(3, r.kgGenerados);
            insert.setParameter(4, r.idContribuyente);
            insert.executeUpdate();
        }
    }

    private void upsertOrdenanza(Ordenanza ord) {
        Query q = em.createNativeQuery(
                "SELECT COUNT(*) FROM ordenanza WHERE idOrdenanza = ? AND concepto = ? AND subconcepto = ?"
        );
        q.setParameter(1, ord.getIdOrdenanza());
        q.setParameter(2, ord.getConcepto());
        q.setParameter(3, ord.getSubconcepto());

        Number n = (Number) q.getSingleResult();

        if (n.intValue() > 0) {
            Query update = em.createNativeQuery(
                    "UPDATE ordenanza "
                    + "SET descripcion = ?, pueblo = ?, tipoCalculo = ?, acumulable = ?, precioFijo = ?, "
                    + "kgIncluidos = ?, precioKg = ?, porcentaje = ?, conceptoRelacionado = ?, iva = ? "
                    + "WHERE idOrdenanza = ? AND concepto = ? AND subconcepto = ?"
            );
            update.setParameter(1, ord.getDescripcion());
            update.setParameter(2, ord.getPueblo());
            update.setParameter(3, ord.getTipoCalculo());
            update.setParameter(4, ord.getAcumulable());
            update.setParameter(5, ord.getPrecioFijo());
            update.setParameter(6, ord.getKgincluidos());
            update.setParameter(7, ord.getPreciokg());
            update.setParameter(8, ord.getPorcentaje());
            update.setParameter(9, ord.getConceptoRelacionado());
            update.setParameter(10, ord.getIva());
            update.setParameter(11, ord.getIdOrdenanza());
            update.setParameter(12, ord.getConcepto());
            update.setParameter(13, ord.getSubconcepto());
            update.executeUpdate();
        } else {
            Query insert = em.createNativeQuery(
                    "INSERT INTO ordenanza "
                    + "(idOrdenanza, concepto, subconcepto, descripcion, pueblo, tipoCalculo, acumulable, "
                    + "precioFijo, kgIncluidos, precioKg, porcentaje, conceptoRelacionado, iva) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            insert.setParameter(1, ord.getIdOrdenanza());
            insert.setParameter(2, ord.getConcepto());
            insert.setParameter(3, ord.getSubconcepto());
            insert.setParameter(4, ord.getDescripcion());
            insert.setParameter(5, ord.getPueblo());
            insert.setParameter(6, ord.getTipoCalculo());
            insert.setParameter(7, ord.getAcumulable());
            insert.setParameter(8, ord.getPrecioFijo());
            insert.setParameter(9, ord.getKgincluidos());
            insert.setParameter(10, ord.getPreciokg());
            insert.setParameter(11, ord.getPorcentaje());
            insert.setParameter(12, ord.getConceptoRelacionado());
            insert.setParameter(13, ord.getIva());
            insert.executeUpdate();
        }
    }

    private int upsertRecibo(RegistroBD r) {
        Query q = em.createNativeQuery(
                "SELECT numeroRecibo FROM recibos "
                + "WHERE nifContribuyente = ? AND QUARTER(fechaPadron) = ? AND YEAR(fechaPadron) = ?"
        );
        q.setParameter(1, r.nif);
        q.setParameter(2, r.trimestre);
        q.setParameter(3, r.anio);

        List<?> resultados = q.getResultList();

        String apellidos = Prac3Support.unirApellidos(r.apellido1, r.apellido2);
        String direccionCompleta = Prac3Support.safe(r.direccion);

        if (!resultados.isEmpty()) {
            Number num = (Number) resultados.get(0);
            int numeroRecibo = num.intValue();

            Query update = em.createNativeQuery(
                    "UPDATE recibos SET "
                    + "direccionCompleta = ?, nombre = ?, apellidos = ?, fechaRecibo = ?, kgGenerados = ?, "
                    + "fechaPadron = ?, totalBaseImponible = ?, totalIVA = ?, totalRecibo = ?, "
                    + "IBAN = ?, email = ?, exencion = ?, idContribuyente = ? "
                    + "WHERE numeroRecibo = ?"
            );
            update.setParameter(1, direccionCompleta);
            update.setParameter(2, r.nombre);
            update.setParameter(3, apellidos);
            update.setParameter(4, Date.valueOf(r.fechaRecibo));
            update.setParameter(5, r.kgGenerados);
            update.setParameter(6, Date.valueOf(r.fechaPadron));
            update.setParameter(7, r.baseImponible.doubleValue());
            update.setParameter(8, r.iva.doubleValue());
            update.setParameter(9, r.totalRecibo.doubleValue());
            update.setParameter(10, Prac3Support.emptyToNull(r.iban));
            update.setParameter(11, Prac3Support.emptyToNull(r.email));
            update.setParameter(12, Prac3Support.emptyToNull(r.exencion));
            update.setParameter(13, Prac3Support.emptyToNull(r.idContribuyente));
            update.setParameter(14, numeroRecibo);
            update.executeUpdate();

            return numeroRecibo;
        } else {
            Query insert = em.createNativeQuery(
                    "INSERT INTO recibos ("
                    + "nifContribuyente, direccionCompleta, nombre, apellidos, fechaRecibo, kgGenerados, "
                    + "fechaPadron, totalBaseImponible, totalIVA, totalRecibo, IBAN, email, exencion, idContribuyente"
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            insert.setParameter(1, r.nif);
            insert.setParameter(2, direccionCompleta);
            insert.setParameter(3, r.nombre);
            insert.setParameter(4, apellidos);
            insert.setParameter(5, Date.valueOf(r.fechaRecibo));
            insert.setParameter(6, r.kgGenerados);
            insert.setParameter(7, Date.valueOf(r.fechaPadron));
            insert.setParameter(8, r.baseImponible.doubleValue());
            insert.setParameter(9, r.iva.doubleValue());
            insert.setParameter(10, r.totalRecibo.doubleValue());
            insert.setParameter(11, Prac3Support.emptyToNull(r.iban));
            insert.setParameter(12, Prac3Support.emptyToNull(r.email));
            insert.setParameter(13, Prac3Support.emptyToNull(r.exencion));
            insert.setParameter(14, Prac3Support.emptyToNull(r.idContribuyente));
            insert.executeUpdate();

            Query lastId = em.createNativeQuery("SELECT LAST_INSERT_ID()");
            Number id = (Number) lastId.getSingleResult();
            return id.intValue();
        }
    }

    private void upsertLineasRecibo(int numeroRecibo, List<PDFGenerator.LineaConcepto> lineas, int bonificacion) {
        for (PDFGenerator.LineaConcepto linea : lineas) {
            Query q = em.createNativeQuery(
                    "SELECT id FROM lineasrecibo WHERE idRecibo = ? AND concepto = ? AND subconcepto = ?"
            );
            q.setParameter(1, numeroRecibo);
            q.setParameter(2, linea.concepto);
            q.setParameter(3, linea.subconcepto);

            List<?> existentes = q.getResultList();
            Double importeBonificacion = Prac3Support.calcularImporteBonificacion(linea.baseImponible, bonificacion);

            if (!existentes.isEmpty()) {
                int id = ((Number) existentes.get(0)).intValue();

                Query update = em.createNativeQuery(
                        "UPDATE lineasrecibo SET baseImponible = ?, porcentajeIVA = ?, importeiva = ?, "
                        + "kgincluidos = ?, bonificacion = ?, importeBonificacion = ? WHERE id = ?"
                );
                update.setParameter(1, linea.baseImponible.doubleValue());
                update.setParameter(2, linea.porcentajeIva.doubleValue());
                update.setParameter(3, linea.importeIva.doubleValue());
                update.setParameter(4, linea.kgIncluidos.doubleValue());
                update.setParameter(5, bonificacion > 0 ? Double.valueOf(bonificacion) : null);
                update.setParameter(6, importeBonificacion);
                update.setParameter(7, id);
                update.executeUpdate();
            } else {
                Query insert = em.createNativeQuery(
                        "INSERT INTO lineasrecibo "
                        + "(concepto, subconcepto, baseImponible, porcentajeIVA, importeiva, kgincluidos, "
                        + "bonificacion, importeBonificacion, idRecibo) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                );
                insert.setParameter(1, linea.concepto);
                insert.setParameter(2, linea.subconcepto);
                insert.setParameter(3, linea.baseImponible.doubleValue());
                insert.setParameter(4, linea.porcentajeIva.doubleValue());
                insert.setParameter(5, linea.importeIva.doubleValue());
                insert.setParameter(6, linea.kgIncluidos.doubleValue());
                insert.setParameter(7, bonificacion > 0 ? Double.valueOf(bonificacion) : null);
                insert.setParameter(8, importeBonificacion);
                insert.setParameter(9, numeroRecibo);
                insert.executeUpdate();
            }
        }
    }

    public String obtenerIdContribuyenteBD(String nombre, String nif, LocalDate fechaAlta) {
        Query q = em.createNativeQuery(
                "SELECT idContribuyente FROM contribuyente WHERE nombre = ? AND nifnie = ? AND fechaAlta = ?"
        );
        q.setParameter(1, Prac3Support.safe(nombre));
        q.setParameter(2, Prac3Support.safe(nif));
        q.setParameter(3, Date.valueOf(fechaAlta));

        List<?> res = q.getResultList();
        if (res.isEmpty()) {
            return null;
        }
        return String.valueOf(res.get(0));
    }

    private void upsertRelContribuyenteOrdenanza(Integer idContribuyente, Integer idOrdenanzaBd) {
        if (idContribuyente == null || idOrdenanzaBd == null) {
            return;
        }

        Query q = em.createNativeQuery(
                "SELECT COUNT(*) FROM rel_contribuyente_ordenanza "
                + "WHERE idContribuyente = ? AND idOrdenanza = ?"
        );
        q.setParameter(1, idContribuyente);
        q.setParameter(2, idOrdenanzaBd);

        Number n = (Number) q.getSingleResult();

        if (n.intValue() == 0) {
            Query insert = em.createNativeQuery(
                    "INSERT INTO rel_contribuyente_ordenanza (idContribuyente, idOrdenanza) "
                    + "VALUES (?, ?)"
            );
            insert.setParameter(1, idContribuyente);
            insert.setParameter(2, idOrdenanzaBd);
            insert.executeUpdate();
        }
    }

    private Integer obtenerIdOrdenanzaBD(Ordenanza ord) {
        Query q = em.createNativeQuery(
                "SELECT id FROM ordenanza "
                + "WHERE idOrdenanza = ? AND concepto = ? AND subconcepto = ?"
        );
        q.setParameter(1, ord.getIdOrdenanza());
        q.setParameter(2, ord.getConcepto());
        q.setParameter(3, ord.getSubconcepto());

        List<?> res = q.getResultList();
        if (res.isEmpty()) {
            return null;
        }
        return ((Number) res.get(0)).intValue();
    }
}