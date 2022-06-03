package com.example.telemillonario.entity;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;

@Entity
@Table(name = "sede")
public class Sede implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "nombre", length = 100)
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max=100,message = "Nombre no mayor a 40 caracteres")
    private String nombre;

//    @NotBlank(message = "La descripción no puede estar vacío")
//    @Size(max=500,message = "Descripción no mayor a 500 caracteres")
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "direccion", length = 100)
    @NotBlank(message = "La dirección no puede estar vacío")
    @Size(max=100,message = "Dirección no mayor a 100 caracteres")
    private String direccion;

    @Column(name = "coordenadas", length = 100)
    @NotBlank(message = "Las coordenadas no pueden estar vacías")
    @Size(max=100,message = "Coordenadas no mayor a 100 caracteres")
    private String coordenadas;

    @Column(name = "telefono", length = 20)
    @NotBlank(message = "El telefono no pueden estar vacíos")
    @Size(max=20,message = "Teléfono no mayor a 20 caracteres")
    private String telefono;

    @Column(name = "numerosalas")
    @Positive
    @Min(value = 1, message = "El valor mínimo de salas es 1")
    @Digits(integer = 2, fraction = 0, message = "Debe ser un número entero y menor a 3 cifras")
    @NotNull(message = "La cantidad de salas no debe estar vacio")
    private Integer numerosalas;

//    @Positive
//    @Digits(integer = 2, fraction = 2, message = "Debe contener solo 2 decimales")
//    @NotNull(message = "El precio no debe estar vacio")
//    @Column(name = "calificacion")
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

    public Integer getNumerosalas() {
        return numerosalas;
    }

    public void setNumerosalas(Integer numerosalas) {
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