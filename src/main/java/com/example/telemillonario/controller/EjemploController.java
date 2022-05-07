package com.example.telemillonario.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/ejemplo")
public class EjemploController {

    @GetMapping("")
    public String modelo(){
        return "index";
    }

    @GetMapping("/agregarActor")
    public String formNuevoActor(){
        return "Administrador/Actor/crearActor";
    }

    @GetMapping("/agregarActor")
    public String formEditarActor(){
        return "Administrador/Actor/crearActor";
    }

    @PostMapping("/subir")
    public String recibirImagen(@RequestParam("imagenes")MultipartFile[] imagenes){

        System.out.println("En total se recibieron: " + imagenes.length);
        for(MultipartFile img : imagenes){
            System.out.println("Nombre: " + img.getOriginalFilename());
            System.out.println("Tipo: " + img.getContentType());
        }
        return "redirect:/ejemplo";
    }

    @PostMapping("/editar")
    public String editarImagen(@RequestParam("eliminar") String[] ids,
                               @RequestParam("imagenes")MultipartFile[] imagenes){

        System.out.println("Imágenes a Eliminar: " + ids.length);
        for(String id : ids){
            System.out.println("ID: " + id);
        }


        System.out.println("\nImágenes a Agregar: " + imagenes.length);
        for(MultipartFile img : imagenes){
            System.out.println("Nombre: " + img.getOriginalFilename());
            System.out.println("Tipo: " + img.getContentType());
        }
        return "redirect:/ejemplo";
    }

}
