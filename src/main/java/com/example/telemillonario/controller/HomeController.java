package com.example.telemillonario.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @GetMapping("")
    public String modelo(){
        return "index";
    }

    @GetMapping("/admin")
    public String admin(){
        return "Administrador/Sede/listaSedes";
    }

    @GetMapping("/user")
    public String usuario(){
        return "usuario/vistaPrincipal";
    }
}
