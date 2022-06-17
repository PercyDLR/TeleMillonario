package com.example.telemillonario.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "obragenero")
public class Obragenero implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "estado")
    private Integer estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idgenero")
    private Genero idgenero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idobra", referencedColumnName = "id")
    private Obra idobra;

    public Obra getIdobra() {
        return idobra;
    }

    public void setIdobra(Obra idobra) {
        this.idobra = idobra;
    }

    public Genero getIdgenero() {
        return idgenero;
    }

    public void setIdgenero(Genero idgenero) {
        this.idgenero = idgenero;
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