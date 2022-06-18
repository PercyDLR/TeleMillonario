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
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class DatosTarjeta implements Serializable {

    @Size(max = 19, message = "La tarjeta no puede exceder los 16 caracteres")//considerar los guiones intermedios
    @NotEmpty(message = "El numero de tarjeta no puede estar vacio")
    private String numeroTarjeta;//formato 4557-2341-5567-8890


    @NotEmpty(message = "Los nombres no pueden estar vacíos")
    private String nombresTitular;//nombre Mastercard,Visa,Diners Club

    @NotEmpty(message = "La fecha no puede estar vacio")
    private String fechaVencimiento;//Formato p.e 06/25

    @Size(max = 3, message = "El codigo de seguridad no puede exceder los 3 caracteres")
    @NotEmpty(message = "El codigo de seguridad no puede estar vacio")
    private String codigoSeguridad;//CVV p.e 986

}
