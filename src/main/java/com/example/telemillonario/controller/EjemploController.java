package com.example.telemillonario.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin")
public class EjemploController {

    @PostMapping("/subir")
    public String recibirImagen(@RequestParam("imagenes")MultipartFile[] imagenes){

        System.out.println("En total se recibieron: " + imagenes.length);
        for(MultipartFile img : imagenes){
            System.out.println("Nombre: " + img.getOriginalFilename());
            System.out.println("Tipo: " + img.getContentType());
        }
        return "index";
    }

    @GetMapping("/agregarActor")
    public String formActor(){
        return "Administrador/Actor/crearActor";
    }
}
