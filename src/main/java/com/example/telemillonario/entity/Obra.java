package com.example.telemillonario.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "obra")
public class Obra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "estado")
    private Integer estado;

    @Column(name = "calificacion")
    private Double calificacion;


    @NotNull(message = "Defina la Restricción de Edad")
    @Column(name = "restriccionedad")
    private Integer restriccionedad;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max=40,message = "Nombre no mayor a 40 caracteres")
    @Column(name = "nombre", length = 100)
    private String nombre;

    @Size(max=40,message = "La descripcion no puede ser mayor a 500 caracteres")
    @NotBlank(message = "La descripcion no puede estar vacia")
    @Column(name = "descripcion", length = 500)
    private String descripcion;

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