package com.example.telemillonario.controller;

import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.repository.FotoRepository;
import com.example.telemillonario.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @Autowired
    FotoRepository fotoRepository;
    @Autowired
    PersonaRepository personaRepository;

    @GetMapping("/editarActor")
    public String formEditarActor(Model model){

        int idactor = 1;

        Persona actor = personaRepository.findById(idactor).get();
        List<Foto> fotos = fotoRepository.findByIdpersonaOrderByNumero(idactor);

        model.addAttribute("imagenes", fotos);
        model.addAttribute("actor", actor);

        return "Administrador/Actor/editarActor";
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

    @GetMapping("/error")
    public String pantallaError(Model model){

        model.addAttribute("status",404);
        model.addAttribute("error","");
        return "error";
    }

}
