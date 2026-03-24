/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package POJOS;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "ordenanza")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Ordenanza.findAll", query = "SELECT o FROM Ordenanza o"),
    @NamedQuery(name = "Ordenanza.findById", query = "SELECT o FROM Ordenanza o WHERE o.id = :id"),
    @NamedQuery(name = "Ordenanza.findByIdOrdenanza", query = "SELECT o FROM Ordenanza o WHERE o.idOrdenanza = :idOrdenanza"),
    @NamedQuery(name = "Ordenanza.findByConcepto", query = "SELECT o FROM Ordenanza o WHERE o.concepto = :concepto"),
    @NamedQuery(name = "Ordenanza.findBySubconcepto", query = "SELECT o FROM Ordenanza o WHERE o.subconcepto = :subconcepto"),
    @NamedQuery(name = "Ordenanza.findByDescripcion", query = "SELECT o FROM Ordenanza o WHERE o.descripcion = :descripcion"),
    @NamedQuery(name = "Ordenanza.findByAcumulable", query = "SELECT o FROM Ordenanza o WHERE o.acumulable = :acumulable"),
    @NamedQuery(name = "Ordenanza.findByPrecioFijo", query = "SELECT o FROM Ordenanza o WHERE o.precioFijo = :precioFijo"),
    @NamedQuery(name = "Ordenanza.findByKgincluidos", query = "SELECT o FROM Ordenanza o WHERE o.kgincluidos = :kgincluidos"),
    @NamedQuery(name = "Ordenanza.findByPreciokg", query = "SELECT o FROM Ordenanza o WHERE o.preciokg = :preciokg"),
    @NamedQuery(name = "Ordenanza.findByPorcentaje", query = "SELECT o FROM Ordenanza o WHERE o.porcentaje = :porcentaje"),
    @NamedQuery(name = "Ordenanza.findByConceptoRelacionado", query = "SELECT o FROM Ordenanza o WHERE o.conceptoRelacionado = :conceptoRelacionado"),
    @NamedQuery(name = "Ordenanza.findByIva", query = "SELECT o FROM Ordenanza o WHERE o.iva = :iva"),
    @NamedQuery(name = "Ordenanza.findByPueblo", query = "SELECT o FROM Ordenanza o WHERE o.pueblo = :pueblo"),
    @NamedQuery(name = "Ordenanza.findByTipoCalculo", query = "SELECT o FROM Ordenanza o WHERE o.tipoCalculo = :tipoCalculo")})
public class Ordenanza implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "idOrdenanza")
    private int idOrdenanza;
    @Basic(optional = false)
    @Column(name = "concepto")
    private String concepto;
    @Basic(optional = false)
    @Column(name = "subconcepto")
    private String subconcepto;
    @Basic(optional = false)
    @Column(name = "descripcion")
    private String descripcion;
    @Column(name = "acumulable")
    private String acumulable;
    @Column(name = "precioFijo")
    private Integer precioFijo;
    @Column(name = "kgincluidos")
    private Integer kgincluidos;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "preciokg")
    private Double preciokg;
    @Column(name = "porcentaje")
    private Double porcentaje;
    @Column(name = "conceptoRelacionado")
    private Integer conceptoRelacionado;
    @Column(name = "IVA")
    private Double iva;
    @Column(name = "pueblo")
    private String pueblo;
    @Column(name = "tipoCalculo")
    private String tipoCalculo;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "ordenanza")
    private RelContribuyenteOrdenanza relContribuyenteOrdenanza;

    public Ordenanza() {
    }

    public Ordenanza(Integer id) {
        this.id = id;
    }

    public Ordenanza(Integer id, int idOrdenanza, String concepto, String subconcepto, String descripcion) {
        this.id = id;
        this.idOrdenanza = idOrdenanza;
        this.concepto = concepto;
        this.subconcepto = subconcepto;
        this.descripcion = descripcion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getIdOrdenanza() {
        return idOrdenanza;
    }

    public void setIdOrdenanza(int idOrdenanza) {
        this.idOrdenanza = idOrdenanza;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getAcumulable() {
        return acumulable;
    }

    public void setAcumulable(String acumulable) {
        this.acumulable = acumulable;
    }

    public Integer getPrecioFijo() {
        return precioFijo;
    }

    public void setPrecioFijo(Integer precioFijo) {
        this.precioFijo = precioFijo;
    }

    public Integer getKgincluidos() {
        return kgincluidos;
    }

    public void setKgincluidos(Integer kgincluidos) {
        this.kgincluidos = kgincluidos;
    }

    public Double getPreciokg() {
        return preciokg;
    }

    public void setPreciokg(Double preciokg) {
        this.preciokg = preciokg;
    }

    public Double getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(Double porcentaje) {
        this.porcentaje = porcentaje;
    }

    public Integer getConceptoRelacionado() {
        return conceptoRelacionado;
    }

    public void setConceptoRelacionado(Integer conceptoRelacionado) {
        this.conceptoRelacionado = conceptoRelacionado;
    }

    public Double getIva() {
        return iva;
    }

    public void setIva(Double iva) {
        this.iva = iva;
    }

    public String getPueblo() {
        return pueblo;
    }

    public void setPueblo(String pueblo) {
        this.pueblo = pueblo;
    }

    public String getTipoCalculo() {
        return tipoCalculo;
    }

    public void setTipoCalculo(String tipoCalculo) {
        this.tipoCalculo = tipoCalculo;
    }

    public RelContribuyenteOrdenanza getRelContribuyenteOrdenanza() {
        return relContribuyenteOrdenanza;
    }

    public void setRelContribuyenteOrdenanza(RelContribuyenteOrdenanza relContribuyenteOrdenanza) {
        this.relContribuyenteOrdenanza = relContribuyenteOrdenanza;
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
        if (!(object instanceof Ordenanza)) {
            return false;
        }
        Ordenanza other = (Ordenanza) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "POJOS.Ordenanza[ id=" + id + " ]";
    }
    
}
