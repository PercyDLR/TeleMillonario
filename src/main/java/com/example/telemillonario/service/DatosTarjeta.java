package com.example.telemillonario.service;

import com.example.telemillonario.entity.Distrito;
import com.example.telemillonario.entity.Rol;
import com.example.telemillonario.entity.Sede;
import com.example.telemillonario.validation.Elenco;
import com.example.telemillonario.validation.Operador;
import com.example.telemillonario.validation.Perfil;
import com.example.telemillonario.validation.Usuario;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Getter
@Setter
public class DatosTarjeta {

    @Size(max = 16, message = "La tarjeta no puede exceder los 16 caracteres")
    @NotEmpty(message = "El numero de tarjeta no puede estar vacio")
    private String numeroTarjeta;


    @NotEmpty(message = "Los nombres no pueden estar vacíos")
    private String nombresTitular;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotEmpty(message = "La fecha no puede estar vacio")
    private LocalDate fechaVencimiento;

    @Size(max = 3, message = "El codigo de seguridad no puede exceder los 3 caracteres")
    @NotEmpty(message = "El codigo de seguridad no puede estar vacio")
    private String codigoSeguridad;

}
