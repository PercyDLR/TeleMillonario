package com.example.telemillonario.entity;

import com.example.telemillonario.validation.Elenco;
import com.example.telemillonario.validation.Operador;
import com.example.telemillonario.validation.Perfil;
import com.example.telemillonario.validation.Usuario;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.*;
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

    @NotEmpty(message = "Los nombres no pueden estar vacíos",groups = {Usuario.class ,Operador.class, Elenco.class})
    @Column(name = "nombres", length = 100)
    private String nombres;

    @NotEmpty(message = "Los apellidos no pueden estar vacíos", groups = {Usuario.class ,Operador.class, Elenco.class})
    @Column(name = "apellidos", length = 100)
    private String apellidos;

    //https://www.geeksforgeeks.org/spring-mvc-custom-validation/
    @NotEmpty(message = "El dni no puede estar vacío", groups = {Usuario.class ,Operador.class})
    @Pattern(regexp = "[0-9]{8}",message = "DNI no valido", groups = {Usuario.class ,Operador.class})//Momentaneamente
    @Column(name = "dni", length = 8)
    private String dni;

    @Email(message = "Ingrese una dirección de correo válida",groups = {Usuario.class ,Operador.class})
    @NotEmpty(message = "El correo no puede estar vacio",groups = {Usuario.class ,Operador.class})
    @Column(name = "correo", length = 100)
    private String correo;

    @Column(name = "telefono", length = 20)
    @Size(min=7 ,max = 20, groups = Perfil.class)
    private String telefono;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Past(groups = Perfil.class)
    @Column(name = "nacimiento")
    private LocalDate nacimiento;

    @Column(name = "direccion", length = 100)
    @NotEmpty(groups = Perfil.class)
    private String direccion;

    @Column(name = "estado")
    private Integer estado;

    @NotNull(message = "La contraseña no puede estar vacia", groups = Usuario.class)
    @Column(name = "contrasenia", length = 200)
    private String contrasenia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idsede")
    @NotNull(message = "Tiene que escoger una Sede valida", groups = Operador.class)
    private Sede idsede;//No se puede restringir puesto que implicaría malograr a Usuario,Admin,Actor y director

    @ManyToOne
    @JoinColumn(name = "idrol")
    private Rol idrol;

    @ManyToOne
    @NotNull(groups = {Usuario.class})
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

    public String getTelefono() {return telefono;}

    public void setTelefono(String telefono) {this.telefono = telefono;}
}