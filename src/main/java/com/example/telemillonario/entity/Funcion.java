package com.example.telemillonario.entity;

import com.example.telemillonario.validation.Elenco;
import com.example.telemillonario.validation.Operador;
import com.example.telemillonario.validation.Usuario;
import org.springframework.format.annotation.DateTimeFormat;

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
    private int id;

    @Column(name = "estado")
    private int estado;

    @Column(name = "fecha")
    @FutureOrPresent(message = "La fecha que ingresaste ya pasó")
    private LocalDate fecha;

    @Column(name = "inicio")
    private LocalTime inicio;

    @Column(name = "fin")
    private LocalTime fin;

    @Column(name = "calificacion")
    private Double calificacion;

    @Column(name = "restriccionedad")
    @NotNull(message = "Defina la Restricción de Edad")
    private Integer restriccionedad;

    @Column(name = "precioentrada")
    @Positive
    @Digits(integer = 4, fraction = 2, message = "Debe contener solo 2 decimales")
    @Min(value = 5, message = "El precio mínimo es 5")
    @NotNull(message = "El precio no debe estar vacio")
    private Double precioentrada;

    @Column(name = "stockentradas")
    @Positive
    @Digits(integer = 3, fraction = 0, message = "Debe ser un número entero")
    @NotNull(message = "El stock no debe estar vacio")
    private Integer stockentradas;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max=40,message = "Nombre no mayor a 40 caracteres")
    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 5000)
    @Size(max=40,message = "La descripcion no puede ser mayor a 5000 caracteres")
    @NotBlank(message = "La descripcion no puede estar vacia")
    private String descripcion;

    @Column(name = "cantidadasistentes")
    private Integer cantidadasistentes;

    @ManyToOne
    @JoinColumn(name = "idsala")
    private Sala sala = new Sala();

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