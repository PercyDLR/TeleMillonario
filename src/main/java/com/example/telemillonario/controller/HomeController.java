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

    @GetMapping("/admin1")
    public String directores(){
        return "Administrador/Director/editarDirector";
    }

    @GetMapping("/admin2")
    public String directores1(){
        return "Administrador/Director/listaDirectores";
    }

    @GetMapping("/admin3")
    public String directores2(){
        return "Administrador/Sede/editarSedes";
    }

    @GetMapping("/sala")
    public String listaSlas(){
        return "Administrador/Sala/listaSalas";
    }

    @GetMapping("/agregarSede")
    public String agregarSede(){
        return "Administrador/Sede/agregarSedes";
    }

    @GetMapping("/editarSede")
    public String editarSede(){
        return "Administrador/Sede/editarSedes";
    }

    @GetMapping("/agregarSala")
    public String agregarSala(){
        return "Administrador/Sala/agregarSalas";
    }

    @GetMapping("/editarSalas")
    public String editarSala(){
        return "Administrador/Sala/editarSalas";
    }

}
