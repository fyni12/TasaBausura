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
import javax.persistence.JoinColumn;
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
@Table(name = "recibos")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Recibos.findAll", query = "SELECT r FROM Recibos r"),
    @NamedQuery(name = "Recibos.findByNumeroRecibo", query = "SELECT r FROM Recibos r WHERE r.numeroRecibo = :numeroRecibo"),
    @NamedQuery(name = "Recibos.findByNifContribuyente", query = "SELECT r FROM Recibos r WHERE r.nifContribuyente = :nifContribuyente"),
    @NamedQuery(name = "Recibos.findByDireccionCompleta", query = "SELECT r FROM Recibos r WHERE r.direccionCompleta = :direccionCompleta"),
    @NamedQuery(name = "Recibos.findByNombre", query = "SELECT r FROM Recibos r WHERE r.nombre = :nombre"),
    @NamedQuery(name = "Recibos.findByApellidos", query = "SELECT r FROM Recibos r WHERE r.apellidos = :apellidos"),
    @NamedQuery(name = "Recibos.findByFechaRecibo", query = "SELECT r FROM Recibos r WHERE r.fechaRecibo = :fechaRecibo"),
    @NamedQuery(name = "Recibos.findByKgGenerados", query = "SELECT r FROM Recibos r WHERE r.kgGenerados = :kgGenerados"),
    @NamedQuery(name = "Recibos.findByFechaPadron", query = "SELECT r FROM Recibos r WHERE r.fechaPadron = :fechaPadron"),
    @NamedQuery(name = "Recibos.findByTotalBaseImponible", query = "SELECT r FROM Recibos r WHERE r.totalBaseImponible = :totalBaseImponible"),
    @NamedQuery(name = "Recibos.findByTotalIVA", query = "SELECT r FROM Recibos r WHERE r.totalIVA = :totalIVA"),
    @NamedQuery(name = "Recibos.findByTotalRecibo", query = "SELECT r FROM Recibos r WHERE r.totalRecibo = :totalRecibo"),
    @NamedQuery(name = "Recibos.findByIban", query = "SELECT r FROM Recibos r WHERE r.iban = :iban"),
    @NamedQuery(name = "Recibos.findByEmail", query = "SELECT r FROM Recibos r WHERE r.email = :email"),
    @NamedQuery(name = "Recibos.findByExencion", query = "SELECT r FROM Recibos r WHERE r.exencion = :exencion"),
    @NamedQuery(name = "Recibos.findByIdContribuyente", query = "SELECT r FROM Recibos r WHERE r.idContribuyente = :idContribuyente")})
public class Recibos implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "numeroRecibo")
    private Integer numeroRecibo;
    @Basic(optional = false)
    @Column(name = "nifContribuyente")
    private String nifContribuyente;
    @Basic(optional = false)
    @Column(name = "direccionCompleta")
    private String direccionCompleta;
    @Basic(optional = false)
    @Column(name = "nombre")
    private String nombre;
    @Basic(optional = false)
    @Column(name = "apellidos")
    private String apellidos;
    @Basic(optional = false)
    @Column(name = "fechaRecibo")
    @Temporal(TemporalType.DATE)
    private Date fechaRecibo;
    @Basic(optional = false)
    @Column(name = "kgGenerados")
    private int kgGenerados;
    @Basic(optional = false)
    @Column(name = "fechaPadron")
    @Temporal(TemporalType.DATE)
    private Date fechaPadron;
    @Basic(optional = false)
    @Column(name = "totalBaseImponible")
    private double totalBaseImponible;
    @Basic(optional = false)
    @Column(name = "totalIVA")
    private double totalIVA;
    @Basic(optional = false)
    @Column(name = "totalRecibo")
    private double totalRecibo;
    @Column(name = "IBAN")
    private String iban;
    @Column(name = "email")
    private String email;
    @Column(name = "exencion")
    private String exencion;
    @Column(name = "idContribuyente")
    private String idContribuyente;
    @JoinColumn(name = "numeroRecibo", referencedColumnName = "idContribuyente", insertable = false, updatable = false)
    @OneToOne(optional = false)
    private Contribuyente contribuyente;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "recibos")
    private Lineasrecibo lineasrecibo;

    public Recibos() {
    }

    public Recibos(Integer numeroRecibo) {
        this.numeroRecibo = numeroRecibo;
    }

    public Recibos(Integer numeroRecibo, String nifContribuyente, String direccionCompleta, String nombre, String apellidos, Date fechaRecibo, int kgGenerados, Date fechaPadron, double totalBaseImponible, double totalIVA, double totalRecibo) {
        this.numeroRecibo = numeroRecibo;
        this.nifContribuyente = nifContribuyente;
        this.direccionCompleta = direccionCompleta;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fechaRecibo = fechaRecibo;
        this.kgGenerados = kgGenerados;
        this.fechaPadron = fechaPadron;
        this.totalBaseImponible = totalBaseImponible;
        this.totalIVA = totalIVA;
        this.totalRecibo = totalRecibo;
    }

    public Integer getNumeroRecibo() {
        return numeroRecibo;
    }

    public void setNumeroRecibo(Integer numeroRecibo) {
        this.numeroRecibo = numeroRecibo;
    }

    public String getNifContribuyente() {
        return nifContribuyente;
    }

    public void setNifContribuyente(String nifContribuyente) {
        this.nifContribuyente = nifContribuyente;
    }

    public String getDireccionCompleta() {
        return direccionCompleta;
    }

    public void setDireccionCompleta(String direccionCompleta) {
        this.direccionCompleta = direccionCompleta;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public Date getFechaRecibo() {
        return fechaRecibo;
    }

    public void setFechaRecibo(Date fechaRecibo) {
        this.fechaRecibo = fechaRecibo;
    }

    public int getKgGenerados() {
        return kgGenerados;
    }

    public void setKgGenerados(int kgGenerados) {
        this.kgGenerados = kgGenerados;
    }

    public Date getFechaPadron() {
        return fechaPadron;
    }

    public void setFechaPadron(Date fechaPadron) {
        this.fechaPadron = fechaPadron;
    }

    public double getTotalBaseImponible() {
        return totalBaseImponible;
    }

    public void setTotalBaseImponible(double totalBaseImponible) {
        this.totalBaseImponible = totalBaseImponible;
    }

    public double getTotalIVA() {
        return totalIVA;
    }

    public void setTotalIVA(double totalIVA) {
        this.totalIVA = totalIVA;
    }

    public double getTotalRecibo() {
        return totalRecibo;
    }

    public void setTotalRecibo(double totalRecibo) {
        this.totalRecibo = totalRecibo;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getExencion() {
        return exencion;
    }

    public void setExencion(String exencion) {
        this.exencion = exencion;
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

    public Lineasrecibo getLineasrecibo() {
        return lineasrecibo;
    }

    public void setLineasrecibo(Lineasrecibo lineasrecibo) {
        this.lineasrecibo = lineasrecibo;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (numeroRecibo != null ? numeroRecibo.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Recibos)) {
            return false;
        }
        Recibos other = (Recibos) object;
        if ((this.numeroRecibo == null && other.numeroRecibo != null) || (this.numeroRecibo != null && !this.numeroRecibo.equals(other.numeroRecibo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "POJOS.Recibos[ numeroRecibo=" + numeroRecibo + " ]";
    }
    
}
