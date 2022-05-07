package com.example.telemillonario.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsuarioController {

    @GetMapping("")
    public String paginaPrincipal(){
        return "vistaPrincipal";
    }






}
