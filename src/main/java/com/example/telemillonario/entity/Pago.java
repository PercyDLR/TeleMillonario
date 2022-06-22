package com.example.telemillonario.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago")
public class Pago implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "estado")
    private Integer estado;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "idtarjeta")
    private Integer idtarjeta;

    @Column(name = "numerotarjeta", length = 100)
    private String numerotarjeta;

    @Column(name = "fechapago")
    private LocalDateTime fechapago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompra")
    private Compra idcompra;

    @Column(name = "qrlink", length = 400)
    private String qrlink;

    @Column(name = "codigo", length = 400)
    private String codigo;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getQrlink() {
        return qrlink;
    }

    public void setQrlink(String qrlink) {
        this.qrlink = qrlink;
    }

    public Compra getIdcompra() {
        return idcompra;
    }

    public void setIdcompra(Compra idcompra) {
        this.idcompra = idcompra;
    }

    public LocalDateTime getFechapago() {
        return fechapago;
    }

    public void setFechapago(LocalDateTime fechapago) {
        this.fechapago = fechapago;
    }

    public String getNumerotarjeta() {
        return numerotarjeta;
    }

    public void setNumerotarjeta(String numerotarjeta) {
        this.numerotarjeta = numerotarjeta;
    }

    public Integer getIdtarjeta() {
        return idtarjeta;
    }

    public void setIdtarjeta(Integer idtarjeta) {
        this.idtarjeta = idtarjeta;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}