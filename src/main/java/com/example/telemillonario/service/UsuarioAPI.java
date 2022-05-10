package com.example.telemillonario.service;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties
@Getter
@Setter
public class UsuarioAPI {

    private boolean success;

    private DatosAPI datos;


}
