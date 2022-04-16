package com.example.telemillonario.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @GetMapping("")
    public String inicio(){
        return "Administrador/Sede/listaSedes";
    }

    /*Pagina para sacar los iconos
    https://iconos8.es/icons/set/edit*/
}
