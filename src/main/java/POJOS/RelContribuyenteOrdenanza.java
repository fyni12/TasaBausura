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
@Table(name = "rel_contribuyente_ordenanza")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RelContribuyenteOrdenanza.findAll", query = "SELECT r FROM RelContribuyenteOrdenanza r"),
    @NamedQuery(name = "RelContribuyenteOrdenanza.findById", query = "SELECT r FROM RelContribuyenteOrdenanza r WHERE r.id = :id"),
    @NamedQuery(name = "RelContribuyenteOrdenanza.findByIdContribuyente", query = "SELECT r FROM RelContribuyenteOrdenanza r WHERE r.idContribuyente = :idContribuyente"),
    @NamedQuery(name = "RelContribuyenteOrdenanza.findByIdOrdenanza", query = "SELECT r FROM RelContribuyenteOrdenanza r WHERE r.idOrdenanza = :idOrdenanza")})
public class RelContribuyenteOrdenanza implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "idContribuyente")
    private Integer idContribuyente;
    @Column(name = "idOrdenanza")
    private String idOrdenanza;
    @JoinColumn(name = "id", referencedColumnName = "idContribuyente", insertable = false, updatable = false)
    @OneToOne(optional = false)
    private Contribuyente contribuyente;
    @JoinColumn(name = "id", referencedColumnName = "id", insertable = false, updatable = false)
    @OneToOne(optional = false)
    private Ordenanza ordenanza;

    public RelContribuyenteOrdenanza() {
    }

    public RelContribuyenteOrdenanza(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdContribuyente() {
        return idContribuyente;
    }

    public void setIdContribuyente(Integer idContribuyente) {
        this.idContribuyente = idContribuyente;
    }

    public String getIdOrdenanza() {
        return idOrdenanza;
    }

    public void setIdOrdenanza(String idOrdenanza) {
        this.idOrdenanza = idOrdenanza;
    }

    public Contribuyente getContribuyente() {
        return contribuyente;
    }

    public void setContribuyente(Contribuyente contribuyente) {
        this.contribuyente = contribuyente;
    }

    public Ordenanza getOrdenanza() {
        return ordenanza;
    }

    public void setOrdenanza(Ordenanza ordenanza) {
        this.ordenanza = ordenanza;
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
        if (!(object instanceof RelContribuyenteOrdenanza)) {
            return false;
        }
        RelContribuyenteOrdenanza other = (RelContribuyenteOrdenanza) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "POJOS.RelContribuyenteOrdenanza[ id=" + id + " ]";
    }
    
}
