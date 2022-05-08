package com.example.telemillonario.entity;

import org.hibernate.validator.constraints.UniqueElements;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.regex.Matcher;

@Entity
@Table(name = "persona")
public class Persona implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotBlank(message = "Los nombres no pueden estar vacíos")
    @Column(name = "nombres", length = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos no pueden estar vacíos")
    @Column(name = "apellidos", length = 100)
    private String apellidos;

    //https://www.geeksforgeeks.org/spring-mvc-custom-validation/
    @NotBlank(message = "El dni no puede estar vacío")
    @Pattern(regexp = "[0-9]{8}",message = "DNI no valido")//Momentaneamente
    @Column(name = "dni", length = 8)
    private String dni;

    @Email
    @NotNull(message = "El correo no puede estar vacio")
    @Column(name = "correo", length = 100)
    private String correo;

//    @Column(name = "telefono", length = 20)
//    private String telefono;

    @Column(name = "nacimiento")
    private LocalDate nacimiento;

    @Column(name = "direccion", length = 100)
    private String direccion;

    @Column(name = "estado")
    private Integer estado;

    @NotNull(message = "La contraseña no puede estar vacia")
    @Column(name = "contrasenia", length = 200)
    private String contrasenia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idsede")
    @NotNull(message = "Tiene que escoger una Sede valida")
    private Sede idsede;//No se puede restringir puesto que implicaría malograr a Usuario,Admin,Actor y director

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idrol")//No se puede restringir puesto que implicaría malograr a Usuario,Admin,Actor y director
    private Rol idrol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iddistrito")
    private Distrito iddistrito;

    @Column(name = "calificacion")
    private Double calificacion;

    @Column(name = "passwordtoken")
    private String passwordToken;

    public String getPasswordToken() {
        return passwordToken;
    }

    public void setPasswordToken(String passwordToken) {
        this.passwordToken = passwordToken;
    }

    public Double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Double calificacion) {
        this.calificacion = calificacion;
    }

    public Distrito getIddistrito() {
        return iddistrito;
    }

    public void setIddistrito(Distrito iddistrito) {
        this.iddistrito = iddistrito;
    }

    public Rol getIdrol() {
        return idrol;
    }

    public void setIdrol(Rol idrol) {
        this.idrol = idrol;
    }

    public Sede getIdsede() {
        return idsede;
    }

    public void setIdsede(Sede idsede) {
        this.idsede = idsede;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public LocalDate getNacimiento() {
        return nacimiento;
    }

    public void setNacimiento(LocalDate nacimiento) {
        this.nacimiento = nacimiento;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}