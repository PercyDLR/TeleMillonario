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
        model.addAttribute("idsede", idsede);
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

    @GetMapping("/nuevaSala")
    public String crearSala(@ModelAttribute("sala") Sala sala, Model model, @RequestParam("sede") int idsede){
        model.addAttribute("sede", sedeRepository.findById(idsede));
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
            return "redirect:/juegos/lista";
        }
    }

    @GetMapping("/disponibilidad")
    public String disponibilidadSala(@RequestParam("id") int id, RedirectAttributes a){
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
        return "redirect:/juegos/lista";
    }
}
