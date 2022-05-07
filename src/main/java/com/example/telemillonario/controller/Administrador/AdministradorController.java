package com.example.telemillonario.controller.Administrador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdministradorController {

    @GetMapping("/listarSedes")
    public String paginaPrincipal(){
        return "Administrador/Sede/listaSedes";
    }


}
