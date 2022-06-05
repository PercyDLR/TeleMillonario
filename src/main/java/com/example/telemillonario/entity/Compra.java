package com.example.telemillonario.entity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "compra")
@Getter
@Setter
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "estado")
    private Integer estado;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "montototal")
    private Double montoTotal;

    @ManyToOne
    @JoinColumn(name = "idfuncion")
    private Funcion funcion;

    @ManyToOne
    @JoinColumn(name = "idpersona")
    private Persona persona;

}
