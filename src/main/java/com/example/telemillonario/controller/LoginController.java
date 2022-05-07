package com.example.telemillonario.controller;

import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    PersonaRepository personaRepository;


    @GetMapping("/login")
    public String loginForm(){
        return "login/signin";
    }

    @GetMapping("/crearCuenta")
    public String registrarse(){
        return "login/signup";
    }


    @GetMapping("/redirectByRole")
    public String redirectByRole(Authentication auth, HttpSession session){


        Persona persona = personaRepository.findByCorreo(auth.getName());

        Persona personita = new Persona();
        personita.setNombres(persona.getNombres());
        personita.setApellidos(persona.getApellidos());
        personita.setDni(persona.getDni());
        personita.setCorreo(persona.getCorreo());
        personita.setNacimiento(persona.getNacimiento());
        personita.setIdrol(persona.getIdrol());

        session.setAttribute("usuario",personita);
        System.out.println("llego aca");

        if(personita.getIdrol().getNombre().equalsIgnoreCase("Administrador")){
            return "redirect:/listarSedes";

        }else if(personita.getIdrol().getNombre().equalsIgnoreCase("Usuario")){
            return "redirect:/";

        }else {
            return "redirect:/"; //Cual es su pagina principal del Operador?
        }
    }




}
