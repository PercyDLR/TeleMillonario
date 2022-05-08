package com.example.telemillonario.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "sede")
public class Sede implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "direccion", length = 100)
    private String direccion;

    @Column(name = "coordenadas", length = 100)
    private String coordenadas;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "numerosalas")
    private int numerosalas;

    @Column(name = "calificacion")
    private Double calificacion;

    @Column(name = "estado")
    private int estado;

    @ManyToOne
    @JoinColumn(name = "iddistrito")
    private Distrito iddistrito;

    public Distrito getIddistrito() {
        return iddistrito;
    }

    public void setIddistrito(Distrito iddistrito) {
        this.iddistrito = iddistrito;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public Double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Double calificacion) {
        this.calificacion = calificacion;
    }

    public int getNumerosalas() {
        return numerosalas;
    }

    public void setNumerosalas(int numerosalas) {
        this.numerosalas = numerosalas;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(String coordenadas) {
        this.coordenadas = coordenadas;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}