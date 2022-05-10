package com.example.telemillonario.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties
@Getter
@Setter
public class DatosAPI {
    private int numero;
    private String nombres;
    private String apellido_materno;
    private String apellido_paterno;
    private String sexo;
    private String fecha_nacimiento;

}
