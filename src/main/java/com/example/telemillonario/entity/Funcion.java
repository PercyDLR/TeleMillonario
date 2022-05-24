package com.example.telemillonario.entity;

import com.example.telemillonario.validation.Elenco;
import com.example.telemillonario.validation.Operador;
import com.example.telemillonario.validation.Usuario;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "funcion")
public class Funcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "estado")
    private int estado;

    @Column(name = "fecha")
    @Future(message = "La fecha que ingresaste ya pasó")
    private LocalDate fecha;

    @Column(name = "inicio")
    private LocalTime inicio;

    @Column(name = "fin")
    private LocalTime fin;

    @Column(name = "calificacion")
    private Double calificacion;

    @Column(name = "restriccionedad")
    @NotNull(message = "La restriccion de edad no debe estar vacia")
    private Integer restriccionedad;

    @Column(name = "precioentrada")
    private Double precioentrada;

    @Column(name = "stockentradas")
    private Integer stockentradas;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max=40,message = "El nombre no puede ser mayor a 40 caracteres")
    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 5000)
    private String descripcion;

    @Column(name = "cantidadasistentes")
    private Integer cantidadasistentes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idsala")
    private Sala sala=new Sala();

    public Sala getSala() {
        return sala;
    }

    public void setSala(Sala sala) {
        this.sala = sala;
    }

    public Integer getCantidadasistentes() {
        return cantidadasistentes;
    }

    public void setCantidadasistentes(Integer cantidadasistentes) {
        this.cantidadasistentes = cantidadasistentes;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public Integer getRestriccionedad() {
        return restriccionedad;
    }

    public void setRestriccionedad(Integer restriccionedad) {
        this.restriccionedad = restriccionedad;
    }

    public Double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Double calificacion) {
        this.calificacion = calificacion;
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

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}