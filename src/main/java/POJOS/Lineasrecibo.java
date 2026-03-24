/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package POJOS;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author sca
 */
@Entity
@Table(name = "lineasrecibo")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Lineasrecibo.findAll", query = "SELECT l FROM Lineasrecibo l"),
    @NamedQuery(name = "Lineasrecibo.findById", query = "SELECT l FROM Lineasrecibo l WHERE l.id = :id"),
    @NamedQuery(name = "Lineasrecibo.findByConcepto", query = "SELECT l FROM Lineasrecibo l WHERE l.concepto = :concepto"),
    @NamedQuery(name = "Lineasrecibo.findBySubconcepto", query = "SELECT l FROM Lineasrecibo l WHERE l.subconcepto = :subconcepto"),
    @NamedQuery(name = "Lineasrecibo.findByBaseImponible", query = "SELECT l FROM Lineasrecibo l WHERE l.baseImponible = :baseImponible"),
    @NamedQuery(name = "Lineasrecibo.findByPorcentajeIVA", query = "SELECT l FROM Lineasrecibo l WHERE l.porcentajeIVA = :porcentajeIVA"),
    @NamedQuery(name = "Lineasrecibo.findByImporteiva", query = "SELECT l FROM Lineasrecibo l WHERE l.importeiva = :importeiva"),
    @NamedQuery(name = "Lineasrecibo.findByKgincluidos", query = "SELECT l FROM Lineasrecibo l WHERE l.kgincluidos = :kgincluidos"),
    @NamedQuery(name = "Lineasrecibo.findByBonificacion", query = "SELECT l FROM Lineasrecibo l WHERE l.bonificacion = :bonificacion"),
    @NamedQuery(name = "Lineasrecibo.findByImporteBonificacion", query = "SELECT l FROM Lineasrecibo l WHERE l.importeBonificacion = :importeBonificacion"),
    @NamedQuery(name = "Lineasrecibo.findByIdRecibo", query = "SELECT l FROM Lineasrecibo l WHERE l.idRecibo = :idRecibo")})
public class Lineasrecibo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "concepto")
    private String concepto;
    @Basic(optional = false)
    @Column(name = "subconcepto")
    private String subconcepto;
    @Basic(optional = false)
    @Column(name = "baseImponible")
    private double baseImponible;
    @Basic(optional = false)
    @Column(name = "porcentajeIVA")
    private double porcentajeIVA;
    @Basic(optional = false)
    @Column(name = "importeiva")
    private double importeiva;
    @Basic(optional = false)
    @Column(name = "kgincluidos")
    private double kgincluidos;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "bonificacion")
    private Double bonificacion;
    @Column(name = "importeBonificacion")
    private Double importeBonificacion;
    @Column(name = "idRecibo")
    private Integer idRecibo;
    @JoinColumn(name = "id", referencedColumnName = "numeroRecibo", insertable = false, updatable = false)
    @OneToOne(optional = false)
    private Recibos recibos;

    public Lineasrecibo() {
    }

    public Lineasrecibo(Integer id) {
        this.id = id;
    }

    public Lineasrecibo(Integer id, String concepto, String subconcepto, double baseImponible, double porcentajeIVA, double importeiva, double kgincluidos) {
        this.id = id;
        this.concepto = concepto;
        this.subconcepto = subconcepto;
        this.baseImponible = baseImponible;
        this.porcentajeIVA = porcentajeIVA;
        this.importeiva = importeiva;
        this.kgincluidos = kgincluidos;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public String getSubconcepto() {
        return subconcepto;
    }

    public void setSubconcepto(String subconcepto) {
        this.subconcepto = subconcepto;
    }

    public double getBaseImponible() {
        return baseImponible;
    }

    public void setBaseImponible(double baseImponible) {
        this.baseImponible = baseImponible;
    }

    public double getPorcentajeIVA() {
        return porcentajeIVA;
    }

    public void setPorcentajeIVA(double porcentajeIVA) {
        this.porcentajeIVA = porcentajeIVA;
    }

    public double getImporteiva() {
        return importeiva;
    }

    public void setImporteiva(double importeiva) {
        this.importeiva = importeiva;
    }

    public double getKgincluidos() {
        return kgincluidos;
    }

    public void setKgincluidos(double kgincluidos) {
        this.kgincluidos = kgincluidos;
    }

    public Double getBonificacion() {
        return bonificacion;
    }

    public void setBonificacion(Double bonificacion) {
        this.bonificacion = bonificacion;
    }

    public Double getImporteBonificacion() {
        return importeBonificacion;
    }

    public void setImporteBonificacion(Double importeBonificacion) {
        this.importeBonificacion = importeBonificacion;
    }

    public Integer getIdRecibo() {
        return idRecibo;
    }

    public void setIdRecibo(Integer idRecibo) {
        this.idRecibo = idRecibo;
    }

    public Recibos getRecibos() {
        return recibos;
    }

    public void setRecibos(Recibos recibos) {
        this.recibos = recibos;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Lineasrecibo)) {
            return false;
        }
        Lineasrecibo other = (Lineasrecibo) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "POJOS.Lineasrecibo[ id=" + id + " ]";
    }
    
}
