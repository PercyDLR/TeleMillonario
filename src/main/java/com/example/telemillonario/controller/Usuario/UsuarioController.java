package com.example.telemillonario.controller.Usuario;

import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.repository.PersonaRepository;
import com.example.telemillonario.validation.Perfil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
        //@Validated(Perfil.class)

        Persona usuarioSesion = (Persona) session.getAttribute("usuario");
        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult.getFieldError());

            usuario.setNombres(usuarioSesion.getNombres());
            usuario.setApellidos(usuarioSesion.getApellidos());
            usuario.setDni(usuarioSesion.getDni());
            usuario.setCorreo(usuarioSesion.getCorreo());

            return "usuario/editarPerfil";
        } else {

            if (usuario.getId().equals(usuarioSesion.getId())) {

                LocalDate fechaActual = LocalDate.now();
                LocalDate fechaRegistrada = usuario.getNacimiento();
                Period period = Period.between(fechaRegistrada, fechaActual);

                System.out.print(period.getYears() + " years,");
                System.out.print(period.getMonths() + " months,");
                System.out.print(period.getDays() + " days");

                if (period.getYears() < 0 || period.getMonths() < 0 || (period.getDays() <= 0 && (period.getYears() < 0 || period.getMonths() < 0))) {
                    model.addAttribute("errDate", -1);
                    return "usuario/editarPerfil";
                }

                // Sobreescribe los nuevos valores
                usuarioSesion.setNacimiento(usuario.getNacimiento());
                usuarioSesion.setDireccion(usuario.getDireccion());
                usuarioSesion.setTelefono(usuario.getTelefono());

                //Actualiza la DB
                personaRepository.save(usuarioSesion);
                //Actualiza la Sesión
                session.setAttribute("usuario",usuarioSesion);

                a.addFlashAttribute("msg", 0);
            } else { //No debe dejar guardar el usuario editado
                a.addFlashAttribute("msg", -1);
            }

            return "redirect:/perfil";
        }

    }

}
