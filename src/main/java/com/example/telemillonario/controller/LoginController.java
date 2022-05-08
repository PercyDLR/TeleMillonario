package com.example.telemillonario.controller;

import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.entity.Rol;
import com.example.telemillonario.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class LoginController {

    @Autowired
    PersonaRepository personaRepository;


    @GetMapping("/login")
    public String loginForm(){
        return "login/signin";
    }

    @GetMapping("/crearCuenta")
    public String registrarse(@ModelAttribute("usuario") Persona usuario){
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
        personita.setIdsede(persona.getIdsede());

        System.out.println(personita.getIdsede().getId());
        System.out.println(personita.getIdsede().getNombre());
        session.setAttribute("usuario",personita);
        System.out.println("llego aca");

        if(personita.getIdrol().getNombre().equalsIgnoreCase("Administrador")){
            return "redirect:/listarSedes";

        }else if(personita.getIdrol().getNombre().equalsIgnoreCase("Usuario")){
            return "redirect:/";

        }else {
            return "redirect:/funciones/lista"; //Cual es su pagina principal del Operador?
        }
    }

    @PostMapping("/validacionSignUp")
    public String validacionSignUp(@ModelAttribute("usuario") @Valid Persona usuario, BindingResult bindingResult,ModelAttribute modelAttribute){

        if(bindingResult.hasErrors()){
            return "login/signup";
        }

        //generamos su bcript de contraseña
        String contraseniaBCrypt = new BCryptPasswordEncoder().encode(usuario.getContrasenia());
        usuario.setContrasenia(contraseniaBCrypt);

        //le asignamos el rol 2 -> usuario
        Rol rol = new Rol(2,1,"Usuario");
        usuario.setIdrol(rol);

        //Estado -> 1
        usuario.setEstado(1);
        personaRepository.save(usuario);

        return "redirect:/crearCuenta";

    }




}
