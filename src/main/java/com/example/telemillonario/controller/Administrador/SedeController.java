package com.example.telemillonario.controller.Administrador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping( value = {"/admin","/admin/sedes"})
public class SedeController {

    @GetMapping(value = {"", "/","/lista"})
    public String paginaPrincipal(){
        return "Administrador/Sede/listaSedes";
    }


}
