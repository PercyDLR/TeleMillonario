package com.example.telemillonario.controller.Usuario;

import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.repository.PersonaRepository;
import com.example.telemillonario.validation.Usuario;
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

    @GetMapping("/perfil")
    public String verPerfilUsuario(@ModelAttribute("usuario") Persona usuario, Model model, HttpSession session) {
        usuario = (Persona) session.getAttribute("usuario");
        model.addAttribute("usuario",usuario);
        return "usuario/verPerfil";
    }

    @GetMapping("/perfil/editar")
    public String editarPerfilUsuario(@ModelAttribute("usuario") Persona usuario, Model model, HttpSession session) {
        usuario = (Persona) session.getAttribute("usuario");
        model.addAttribute("usuario",usuario);
        return "usuario/editarPerfil";
    }

    @PostMapping("/perfil/guardar")
    public String guardarPerfilUsuario(@ModelAttribute("usuario") @Valid Persona usuario, BindingResult bindingResult, Model model, RedirectAttributes a, HttpSession session) {

        if (bindingResult.hasErrors()) {
            return "usuario/editarPerfil";
        } else {
            Persona usuarioSesion = (Persona) session.getAttribute("usuario");

            if (usuario.getId() == usuarioSesion.getId()) { //Correctamente editado
                a.addFlashAttribute("msg", "0");
                personaRepository.save(usuario);
                session.setAttribute("usuario",usuario);
            } else { //No debe dejar guardar el usuario editado
                a.addFlashAttribute("msg", "-1");
            }
            return "redirect:/usuario/perfil";
        }

    }

}
