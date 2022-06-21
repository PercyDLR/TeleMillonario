package com.example.telemillonario.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "funcionelenco")
public class Funcionelenco implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "idpersona")
    private Persona idpersona;

    @ManyToOne
    @JoinColumn(name = "idfuncion")
    private Funcion idfuncion;

    @Column(name = "estado")
    private Integer estado;

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }

    public Funcion getIdfuncion() {
        return idfuncion;
    }

    public void setIdfuncion(Funcion idfuncion) {
        this.idfuncion = idfuncion;
    }

    public Persona getIdpersona() {
        return idpersona;
    }

    public void setIdpersona(Persona idpersona) {
        this.idpersona = idpersona;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}