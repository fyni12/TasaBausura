/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package POJOS;

import java.io.Serializable;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author sca
 */
@Entity
@Table(name = "contribuyente")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Contribuyente.findAll", query = "SELECT c FROM Contribuyente c"),
    @NamedQuery(name = "Contribuyente.findByIdContribuyente", query = "SELECT c FROM Contribuyente c WHERE c.idContribuyente = :idContribuyente"),
    @NamedQuery(name = "Contribuyente.findByNombre", query = "SELECT c FROM Contribuyente c WHERE c.nombre = :nombre"),
    @NamedQuery(name = "Contribuyente.findByApellido1", query = "SELECT c FROM Contribuyente c WHERE c.apellido1 = :apellido1"),
    @NamedQuery(name = "Contribuyente.findByApellido2", query = "SELECT c FROM Contribuyente c WHERE c.apellido2 = :apellido2"),
    @NamedQuery(name = "Contribuyente.findByNifnie", query = "SELECT c FROM Contribuyente c WHERE c.nifnie = :nifnie"),
    @NamedQuery(name = "Contribuyente.findByDireccion", query = "SELECT c FROM Contribuyente c WHERE c.direccion = :direccion"),
    @NamedQuery(name = "Contribuyente.findByNumero", query = "SELECT c FROM Contribuyente c WHERE c.numero = :numero"),
    @NamedQuery(name = "Contribuyente.findByPaisCCC", query = "SELECT c FROM Contribuyente c WHERE c.paisCCC = :paisCCC"),
    @NamedQuery(name = "Contribuyente.findByCcc", query = "SELECT c FROM Contribuyente c WHERE c.ccc = :ccc"),
    @NamedQuery(name = "Contribuyente.findByIban", query = "SELECT c FROM Contribuyente c WHERE c.iban = :iban"),
    @NamedQuery(name = "Contribuyente.findByEEmail", query = "SELECT c FROM Contribuyente c WHERE c.eEmail = :eEmail"),
    @NamedQuery(name = "Contribuyente.findByExencion", query = "SELECT c FROM Contribuyente c WHERE c.exencion = :exencion"),
    @NamedQuery(name = "Contribuyente.findByBonificacion", query = "SELECT c FROM Contribuyente c WHERE c.bonificacion = :bonificacion"),
    @NamedQuery(name = "Contribuyente.findByFechaAlta", query = "SELECT c FROM Contribuyente c WHERE c.fechaAlta = :fechaAlta"),
    @NamedQuery(name = "Contribuyente.findByFechaBaja", query = "SELECT c FROM Contribuyente c WHERE c.fechaBaja = :fechaBaja")})
public class Contribuyente implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "idContribuyente")
    private Integer idContribuyente;
    @Basic(optional = false)
    @Column(name = "nombre")
    private String nombre;
    @Basic(optional = false)
    @Column(name = "apellido1")
    private String apellido1;
    @Column(name = "apellido2")
    private String apellido2;
    @Basic(optional = false)
    @Column(name = "NIFNIE")
    private String nifnie;
    @Basic(optional = false)
    @Column(name = "direccion")
    private String direccion;
    @Column(name = "numero")
    private String numero;
    @Column(name = "paisCCC")
    private String paisCCC;
    @Column(name = "CCC")
    private String ccc;
    @Column(name = "IBAN")
    private String iban;
    @Column(name = "eEmail")
    private String eEmail;
    @Column(name = "exencion")
    private Character exencion;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "bonificacion")
    private Double bonificacion;
    @Basic(optional = false)
    @Column(name = "fechaAlta")
    @Temporal(TemporalType.DATE)
    private Date fechaAlta;
    @Column(name = "fechaBaja")
    private Date fechaBaja;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "contribuyente")
    private Lecturas lecturas;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "contribuyente")
    private Recibos recibos;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "contribuyente")
    private RelContribuyenteOrdenanza relContribuyenteOrdenanza;

    public Contribuyente() {
    }

    public Contribuyente(Integer idContribuyente) {
        this.idContribuyente = idContribuyente;
    }

    public Contribuyente(Integer idContribuyente, String nombre, String apellido1,String apellido2, String nifnie, String direccion, Date fechaAlta) {
        this.idContribuyente = idContribuyente;
        this.nombre = nombre;
        this.apellido1 = apellido1;
        this.apellido2 = apellido2;
        this.nifnie = nifnie;
        this.direccion = direccion;
        this.fechaAlta = fechaAlta;
    }

    public Integer getIdContribuyente() {
        return idContribuyente;
    }

    public void setIdContribuyente(Integer idContribuyente) {
        this.idContribuyente = idContribuyente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido1() {
        return apellido1;
    }

    public void setApellido1(String apellido1) {
        this.apellido1 = apellido1;
    }

    public String getApellido2() {
        return apellido2;
    }

    public void setApellido2(String apellido2) {
        this.apellido2 = apellido2;
    }

    public String getNifnie() {
        return nifnie;
    }

    public void setNifnie(String nifnie) {
        this.nifnie = nifnie;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getPaisCCC() {
        return paisCCC;
    }

    public void setPaisCCC(String paisCCC) {
        this.paisCCC = paisCCC;
    }

    public String getCcc() {
        return ccc;
    }

    public void setCcc(String ccc) {
        this.ccc = ccc;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getEEmail() {
        return eEmail;
    }

    public void setEEmail(String eEmail) {
        this.eEmail = eEmail;
    }

    public Character getExencion() {
        return exencion;
    }

    public void setExencion(Character exencion) {
        this.exencion = exencion;
    }

    public Double getBonificacion() {
        return bonificacion;
    }

    public void setBonificacion(Double bonificacion) {
        this.bonificacion = bonificacion;
    }

    public Date getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(Date fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public Date getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(Date fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public Lecturas getLecturas() {
        return lecturas;
    }

    public void setLecturas(Lecturas lecturas) {
        this.lecturas = lecturas;
    }

    public Recibos getRecibos() {
        return recibos;
    }

    public void setRecibos(Recibos recibos) {
        this.recibos = recibos;
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
        hash += (idContribuyente != null ? idContribuyente.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Contribuyente)) {
            return false;
        }
        Contribuyente other = (Contribuyente) object;
        if ((this.idContribuyente == null && other.idContribuyente != null) || (this.idContribuyente != null && !this.idContribuyente.equals(other.idContribuyente))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "POJOS.Contribuyente[ idContribuyente=" + idContribuyente + " ]";
    }
    
}
