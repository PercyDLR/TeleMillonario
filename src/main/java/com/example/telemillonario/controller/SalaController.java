package com.example.telemillonario.controller;


import com.example.telemillonario.entity.Sala;
import com.example.telemillonario.repository.DistritoRepository;
import com.example.telemillonario.repository.SalaRepository;
import com.example.telemillonario.repository.SedeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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
        return "Administrador/Sala/listaSalas";
    }

    @PostMapping("/buscar")
    public String busqueda(Model model, @RequestParam("parametro") String parametro, RedirectAttributes attr){
        int idsede=7;
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

}
