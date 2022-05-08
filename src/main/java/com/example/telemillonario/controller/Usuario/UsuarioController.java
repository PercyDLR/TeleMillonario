package com.example.telemillonario.controller.Usuario;

import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class UsuarioController {

    @Autowired
    PersonaRepository personaRepository;

    @GetMapping("")
    public String paginaPrincipal(){
        return "vistaPrincipal";
    }

    @GetMapping("/verPerfil")
    public String verPerfilUsuario(Model model, HttpSession session) {
        model.addAttribute("usuario", personaRepository.findById((int) session.getAttribute("id")));
        return "usuario/verPerfil";
    }

    @GetMapping("/editarPerfil")
    public String editarPerfilUsuario(@ModelAttribute("usuario") Persona usuario ,Model model, HttpSession session) {
        model.addAttribute("usuario", personaRepository.findById((int) session.getAttribute("id")));
        return "usuario/editarPerfil";
    }

    @PostMapping("/guardarPerfil")
    public String guardarPerfilUsuario(@ModelAttribute("usuario") @Valid Persona usuario, BindingResult bindingResult, Model model, RedirectAttributes a, HttpSession session) {

        if (bindingResult.hasErrors()) {
            return "usuario/editarPerfil";
        } else {
            if (usuario.getId() == (int) session.getAttribute("id")) { //Correctamente editado
                a.addFlashAttribute("msg", "0");
                personaRepository.save(usuario);
            } else { //No debe dejar guardar el usuario editado
                a.addFlashAttribute("msg", "-1");
            }
            return "redirect:/usuario/verPerfil";
        }

    }

}
