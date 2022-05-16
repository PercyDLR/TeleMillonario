package com.example.telemillonario.entity;

import javax.persistence.*;

@Entity
@Table(name = "funciongenero")
public class Funciongenero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idfuncion")
    private Funcion idfuncion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idgenero")
    private Genero idgenero;

    @Column(name = "estado")
    private Integer estado;

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }

    public Genero getIdgenero() {
        return idgenero;
    }

    public void setIdgenero(Genero idgenero) {
        this.idgenero = idgenero;
    }

    public Funcion getIdfuncion() {
        return idfuncion;
    }

    public void setIdfuncion(Funcion idfuncion) {
        this.idfuncion = idfuncion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}