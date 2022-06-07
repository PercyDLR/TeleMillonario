package com.example.telemillonario.entity;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "funcion")
public class Funcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "estado")
    private Integer estado;

    @Column(name = "fecha")
    @FutureOrPresent(message = "La fecha que ingresaste ya pasó")
    private LocalDate fecha;

    @Column(name = "inicio")
    private LocalTime inicio;

    @Column(name = "fin")
    private LocalTime fin;


    @Positive
    @Digits(integer = 4, fraction = 2, message = "Debe contener solo 2 decimales")
    @Min(value = 5, message = "El precio mínimo es 5")
    @NotNull(message = "El precio no debe estar vacio")
    @Column(name = "precioentrada")
    private Double precioentrada;

    @Positive
    @Digits(integer = 3, fraction = 0, message = "Debe ser un número entero")
    @NotNull(message = "El stock no debe estar vacio")
    @Column(name = "stockentradas")
    private Integer stockentradas;

    @Column(name = "cantidadasistentes")
    private Integer cantidadasistentes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idsala")
    private Sala idsala;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idobra", referencedColumnName = "id")
    private Obra idobra;

    public Obra getIdobra() {
        return idobra;
    }

    public void setIdobra(Obra idobra) {
        this.idobra = idobra;
    }

    public Sala getIdsala() {
        return idsala;
    }

    public void setIdsala(Sala idsala) {
        this.idsala = idsala;
    }

    public Integer getCantidadasistentes() {
        return cantidadasistentes;
    }

    public void setCantidadasistentes(Integer cantidadasistentes) {
        this.cantidadasistentes = cantidadasistentes;
    }

    public Integer getStockentradas() {
        return stockentradas;
    }

    public void setStockentradas(Integer stockentradas) {
        this.stockentradas = stockentradas;
    }

    public Double getPrecioentrada() {
        return precioentrada;
    }

    public void setPrecioentrada(Double precioentrada) {
        this.precioentrada = precioentrada;
    }

    public LocalTime getFin() {
        return fin;
    }

    public void setFin(LocalTime fin) {
        this.fin = fin;
    }

    public LocalTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalTime inicio) {
        this.inicio = inicio;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
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