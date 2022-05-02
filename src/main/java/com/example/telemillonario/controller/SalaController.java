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
        int idsede=7;
        model.addAttribute("listSalas", salaRepository.buscarSalas(idsede,0,2));
        model.addAttribute("idsede",idsede);
        return "Administrador/Sala/listaSalas";
    }


    @PostMapping("/filtrar")
    public String busqueda(@RequestParam("parametro") String parametro,
                           @RequestParam("buscador") String buscador,
                           @RequestParam("ord") String ord,
                           RedirectAttributes attr, Model model){
        int idsede=7;
        model.addAttribute("idsede",idsede);

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

    @GetMapping("/nuevaSala")
    public String crearSala(@ModelAttribute("sala") Sala sala, Model model, @RequestParam("idsede") int idsede){
        model.addAttribute("sede", sedeRepository.findById(idsede).get());
        return "Administrador/Sala/editarSalas";
    }

    @GetMapping("/editarSalas")
    public String editarSala(@RequestParam("id") int id, Model model, @ModelAttribute("sala") Sala sala) {

        Optional<Sala> optionalSala = salaRepository.findById(id);
        if(optionalSala.isEmpty()) {
            return "redirect:/sala";
        } else {
            model.addAttribute("sala",optionalSala.get());
            model.addAttribute("sede", optionalSala.get().getIdsede());
            return "Administrador/Sala/editarSalas";
        }
    }

    @PostMapping("/guardar")
    public String guardarSala(@ModelAttribute("sala") @Valid Sala sala, BindingResult bindingResult, RedirectAttributes a, Model model) {
        String msg;
        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult.getFieldError().getDefaultMessage());
            model.addAttribute("sede", sala.getIdsede());
            return "Administrador/Sala/editarSalas";
        } else {
            if(salaRepository.findById(sala.getId()).isPresent()){
                msg="1";
            } else {
                List<Sala> listaSalas = salaRepository.buscarSalas(sala.getIdsede().getId(),0,2);
                int b = 0;
                for (Sala s : listaSalas) {
                    if(s.getNumero() > b) {
                        b = s.getNumero();
                    }
                }
                sala.setNumero(b+1);
                msg="0";
            }
            salaRepository.save(sala);
            a.addFlashAttribute("msg",msg);
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
        }
        return "redirect:/sala";
    }
}
