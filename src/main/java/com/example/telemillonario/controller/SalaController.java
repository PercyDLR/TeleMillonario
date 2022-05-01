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
    public String filtroBuscar (Model model, @RequestParam("parametro") String parametro, @RequestParam("buscador") String buscador, RedirectAttributes attr){

        try {
            if (parametro.equals("")) { // verifica que no esté vacío
                attr.addFlashAttribute("msg", "La búsqueda no debe estar vacía.");
                return "redirect:/sala/lista";
            } else {
                model.addAttribute("parametro", parametro);
                model.addAttribute("buscador", buscador);
                parametro = parametro.toLowerCase();

                switch (buscador){
                    case "identificador":
                        List<Sala> listaIden = salaRepository.buscarPorIden(parametro);
                        model.addAttribute("listSalas", listaIden);
                        break;
                    case "numero":
                        List<Sala> listaNumero = salaRepository.buscarPorNumero(parametro);
                        model.addAttribute("listSalas", listaNumero);
                        break;
                    case "estado":
                        if(parametro == "disponible"){
                            int param=1;
                            List<Sala> listaSalasestado = salaRepository.buscarPorEstado(param);
                            model.addAttribute("listSalas", listaSalasestado);

                        }else{
                            int param=0;
                            List<Sala> listaSalasestado = salaRepository.buscarPorEstado(param);
                            model.addAttribute("listSalas", listaSalasestado);
                        }

                        break;
                    default:
                        int idsede=7;
                        model.addAttribute("listSalas", salaRepository.buscarSalaPorSede(idsede));
                        break;
                }

                return "Administrador/Sala/listaSalas";
            }
        } catch (Exception e) {
            attr.addFlashAttribute("msg", "La búsqueda no debe contener caracteres extraños.");
            return "redirect:/sala/lista";
        }




    }
}
