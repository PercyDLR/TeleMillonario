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
        model.addAttribute("listSalas", salaRepository.buscarSalaPorSede(idsede));
        model.addAttribute("idsede",idsede);
        return "Administrador/Sala/listaSalas";
    }

    @PostMapping("/buscar")
    public String busqueda(Model model, @RequestParam("parametro") String parametro, RedirectAttributes attr){
        int idsede=7;
        model.addAttribute("idsede",idsede);
        try {
            if (parametro.equals("")) { // verifica que no esté vacío
                attr.addFlashAttribute("msg", "La búsqueda no debe estar vacía.");
                return "redirect:/sala/lista";
            } else {
                int param = Integer.parseInt(parametro);
                model.addAttribute("parametro", param);

                List<Sala> listaNumero = salaRepository.buscarPorNumero(param,idsede);
                model.addAttribute("listSalas", listaNumero);


                return "Administrador/Sala/listaSalas";
            }
        } catch (Exception e) {
            attr.addFlashAttribute("msg", "La búsqueda no debe contener caracteres extraños.");
            return "redirect:/sala/lista";
        }

    }

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

    @GetMapping("/listaord")
    public String sortAforo(Model model,@RequestParam("ord") String ord) {
        int idsede=7;
        if (ord.equals("mayor")){
            model.addAttribute("listSalas", salaRepository.sortMayor(idsede));
        }else{
            model.addAttribute("listSalas", salaRepository.sortMenor(idsede));
        }

        return "Administrador/Sala/listaSalas";
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
                List<Sala> listaSalas = salaRepository.buscarSalaPorSede(sala.getIdsede().getId());
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
