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
@Table(name = "lecturas")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Lecturas.findAll", query = "SELECT l FROM Lecturas l"),
    @NamedQuery(name = "Lecturas.findById", query = "SELECT l FROM Lecturas l WHERE l.id = :id"),
    @NamedQuery(name = "Lecturas.findByEjercicio", query = "SELECT l FROM Lecturas l WHERE l.ejercicio = :ejercicio"),
    @NamedQuery(name = "Lecturas.findByPeriodo", query = "SELECT l FROM Lecturas l WHERE l.periodo = :periodo"),
    @NamedQuery(name = "Lecturas.findByKgGenerados", query = "SELECT l FROM Lecturas l WHERE l.kgGenerados = :kgGenerados"),
    @NamedQuery(name = "Lecturas.findByIdContribuyente", query = "SELECT l FROM Lecturas l WHERE l.idContribuyente = :idContribuyente")})
public class Lecturas implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "ejercicio")
    private String ejercicio;
    @Basic(optional = false)
    @Column(name = "periodo")
    private String periodo;
    @Basic(optional = false)
    @Column(name = "kgGenerados")
    private int kgGenerados;
    @Column(name = "idContribuyente")
    private String idContribuyente;
    @JoinColumn(name = "Id", referencedColumnName = "idContribuyente", insertable = false, updatable = false)
    @OneToOne(optional = false)
    private Contribuyente contribuyente;

    public Lecturas() {
    }

    public Lecturas(Integer id) {
        this.id = id;
    }

    public Lecturas(Integer id, String ejercicio, String periodo, int kgGenerados) {
        this.id = id;
        this.ejercicio = ejercicio;
        this.periodo = periodo;
        this.kgGenerados = kgGenerados;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEjercicio() {
        return ejercicio;
    }

    public void setEjercicio(String ejercicio) {
        this.ejercicio = ejercicio;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public int getKgGenerados() {
        return kgGenerados;
    }

    public void setKgGenerados(int kgGenerados) {
        this.kgGenerados = kgGenerados;
    }

    public String getIdContribuyente() {
        return idContribuyente;
    }

    public void setIdContribuyente(String idContribuyente) {
        this.idContribuyente = idContribuyente;
    }

    public Contribuyente getContribuyente() {
        return contribuyente;
    }

    public void setContribuyente(Contribuyente contribuyente) {
        this.contribuyente = contribuyente;
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
        if (!(object instanceof Lecturas)) {
            return false;
        }
        Lecturas other = (Lecturas) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "POJOS.Lecturas[ id=" + id + " ]";
    }
    
}
