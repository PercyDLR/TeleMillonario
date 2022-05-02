package com.example.telemillonario.controller;


import com.example.telemillonario.entity.Sala;
import com.example.telemillonario.entity.Sede;
import com.example.telemillonario.repository.DistritoRepository;
import com.example.telemillonario.repository.SalaRepository;
import com.example.telemillonario.repository.SedeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.spring5.processor.SpringTextareaFieldTagProcessor;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/sala")
public class SalaController {

    @Autowired
    SalaRepository salaRepository;
    @Autowired
    SedeRepository sedeRepository;
    @Autowired
    DistritoRepository distritoRepository;


    @GetMapping(value = {"", "/","/lista"})
    public String listSalas(Model model) {
        int idsede=1;
        model.addAttribute("listSalas", salaRepository.buscarSalas(idsede,0,2));
        model.addAttribute("sede", sedeRepository.findById(idsede).get());
        return "Administrador/Sala/listaSalas";
    }


    @PostMapping("/filtrar")
    public String busqueda(@RequestParam("parametro") String parametro,
                           @RequestParam("buscador") String buscador,
                           @RequestParam("ord") String ord,
                           RedirectAttributes attr, Model model){
        int idsede=1;
        model.addAttribute("sede", sedeRepository.findById(idsede).get());

        try {
            int numero = !parametro.isBlank() ? Integer.parseInt(parametro) : 0;
            model.addAttribute("parametro", numero);

            int estado = buscador.equals("disponible") ? 1 : buscador.equals("nodisponible") ? 0 : 2;
            model.addAttribute("buscador", buscador);

            List<Sala> listaSalas = switch (ord) {
                case "mayor" -> salaRepository.buscarSalasDesc(idsede, numero, estado);
                case "menor" -> salaRepository.buscarSalasAsc(idsede, numero, estado);
                default -> salaRepository.buscarSalas(idsede, numero, estado);
            };

            model.addAttribute("ord",ord);
            model.addAttribute("listSalas", listaSalas);
            return "Administrador/Sala/listaSalas";

        } catch (Exception e) {
            attr.addFlashAttribute("msg", "La búsqueda no debe contener caracteres extraños.");
            return "redirect:/sala/lista";
        }

    }

    /*
    @PostMapping("/filtrar")
    public String filtrarPorEstado (Model model,@RequestParam("buscador") String buscador, RedirectAttributes attr){
        int idsede=7;
        try {
                model.addAttribute("buscador", buscador);
                switch (buscador){
                    case "disponible":
                        List<Sala> listaEstado = salaRepository.buscarPorEstado(1,idsede);
                        model.addAttribute("listSalas", listaEstado);
                        break;
                    case "nodisponible":
                        List<Sala> listaEstado1 = salaRepository.buscarPorEstado(0,idsede);
                        model.addAttribute("listSalas", listaEstado1);
                        break;
                    default:
                        model.addAttribute("listSalas", salaRepository.buscarSalaPorSede(idsede));
                        break;
                }

                return "Administrador/Sala/listaSalas";

        } catch (Exception e) {
            attr.addFlashAttribute("msg", "La búsqueda no debe contener caracteres extraños.");
            return "redirect:/sala/lista";
        }

    }
    */

    /*
    @GetMapping("/listaord")
    public String sortAforo(Model model,@RequestParam("ord") String ord) {
        int idsede=1;
        if (ord.equals("mayor")){
            model.addAttribute("listSalas", salaRepository.sortMayor(idsede));
        }else{
            model.addAttribute("listSalas", salaRepository.sortMenor(idsede));
        }

        model.addAttribute("sede", sedeRepository.findById(idsede).get());
        return "Administrador/Sala/listaSalas";
    }*/

    @GetMapping("/nuevaSala")
    public String crearSala(@ModelAttribute("sala") Sala sala, Model model, @RequestParam("idsede") int idsede){
        model.addAttribute("sede", sedeRepository.findById(idsede).get());
        return "Administrador/Sala/editarSalas";
    }

    @GetMapping("/editarSalas")
    public String editarSala(@RequestParam("id") int id, Model model, @ModelAttribute("sala") Sala sala, RedirectAttributes a) {

        Optional<Sala> optionalSala = salaRepository.findById(id);
        if(optionalSala.isEmpty()) {
            a.addFlashAttribute("msg","-1");
            return "redirect:/sala";
        } else {
            model.addAttribute("sala",optionalSala.get());
            model.addAttribute("sede", optionalSala.get().getIdsede());
            return "Administrador/Sala/editarSalas";
        }
    }

    @PostMapping("/guardar")
    public String guardarSala(@ModelAttribute("sala") @Valid Sala sala, BindingResult bindingResult, RedirectAttributes a, Model model) {
        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult.getFieldError().getDefaultMessage());
            model.addAttribute("sede", sala.getIdsede());
            return "Administrador/Sala/editarSalas";
        } else {
            if(!salaRepository.findById(sala.getId()).isPresent()){
                //Crea automaticamente el numero de la sala
                List<Sala> listaSalas = salaRepository.buscarSalas(sala.getIdsede().getId(),0,2);
                int b = 0;
                for (Sala s : listaSalas) {
                    if(s.getNumero() > b) {
                        b = s.getNumero();
                    }
                }
                sala.setNumero(b+1);
                //Crea automaticamente el identificador de la sala
                String identificador;
                String[] palabras = sala.getIdsede().getNombre().split(" ");
                System.out.println("Longitud de la lista: " + palabras.length);
                System.out.println("Primera palabra: " + palabras[0]);
                System.out.println("Segunda palabra: " + palabras[1]);
                System.out.println("Primera letra primera palabra: " + palabras[0].substring(0,1));
                System.out.println("Primera letra segunda palabra: " + palabras[1].substring(0,1));
                if (palabras.length >= 2) {
                    identificador = palabras[0].substring(0,1).toUpperCase() + palabras[1].substring(0,1).toUpperCase() + "00" + Integer.toString(b+1);
                } else {
                    identificador = palabras[0].substring(0,2).toUpperCase() + "00" + Integer.toString(b+1);
                }
                System.out.println("Identificador: " + identificador);
                sala.setIdentificador(identificador);

                a.addFlashAttribute("msg","0");
            } else {
                a.addFlashAttribute("msg","1");
            }
            a.addFlashAttribute("identificador", sala.getIdentificador());
            salaRepository.save(sala);
            return "redirect:/sala";
        }
    }

    @GetMapping("/disponibilidad")
    public String disponibilidadSala(@RequestParam("id") int id, RedirectAttributes a) {
        Optional<Sala> optionalSala = salaRepository.findById(id);
        if (optionalSala.isPresent()) {
            Sala sala = optionalSala.get();
            if (sala.getEstado() == 1) {
                sala.setEstado(0);
                salaRepository.save(sala);
            } else {
                sala.setEstado(1);
                salaRepository.save(sala);
            }
            a.addFlashAttribute("msg", "2");
            a.addFlashAttribute("identificador", sala.getIdentificador());
        }
        return "redirect:/sala";
    }
}
