package com.example.telemillonario.controller;

import com.azure.core.annotation.Get;
import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.entity.Rol;
import com.example.telemillonario.repository.PersonaRepository;
import com.example.telemillonario.service.UsuarioService;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;

@Controller
public class LoginController {

    @Autowired
    PersonaRepository personaRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JavaMailSender mailSender;


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
            return "redirect:/admin/sedes";

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

    @GetMapping("/cambioDeContrasenia")
    public String cambioDeContrasenia(){
        return "login/CambioDeContrasena";
    }

    @PostMapping("/cambioPassword")
    public String processForgotPasswordForm(@RequestParam("correo") String correo,RedirectAttributes redirectAttributes){

        Persona persona = personaRepository.findByCorreo(correo);

        if(persona == null){
            redirectAttributes.addFlashAttribute("msg","Ingrese un correo válido");
            return "redirect:/cambioDeContrasenia";
        }

        String token = RandomString.make(45);

        System.out.println(correo);
        System.out.println(token);

        usuarioService.updateResetPassword(token,correo);

        //Generamos el link para el reseteo de contraseña

        String resetPasswordLink  = "http://localhost:8080/" + "resetPassword?token=" + token;

        System.out.println(resetPasswordLink);
        try {
            sendEmail(correo,resetPasswordLink);
        } catch (MessagingException | UnsupportedEncodingException e) {
            redirectAttributes.addFlashAttribute("msg","Ha ocurrido un error, vuelve a intentarlo mas tarde.");
            return "redirect:/cambioDeContrasenia";
        }

        redirectAttributes.addFlashAttribute("msgexito","Se ha enviado el link de recuperacion al correo ingresado");
        return "redirect:/cambioDeContrasenia";

    }

    @GetMapping("/resetPassword")
    public String resetPassword(@RequestParam("token") String tokensito, Model model){

        if(tokensito.equalsIgnoreCase("")){
            return "redirect:/anErrorHasOcurred";
        }

        Persona personita = personaRepository.buscarPersonaPorTokensito(tokensito);
        if(personita == null){
            return "redirect:/anErrorHasOcurred";
        }

        model.addAttribute("token",tokensito);
        return "login/actualizacionContrasena";
    }

    @PostMapping("/resetearContrasenia")
    public String reseteadaDeContrasenia(@RequestParam("token") String tokensito,@RequestParam("password") String password,@RequestParam("repassword") String confirmarContrasena,RedirectAttributes redirectAttributes){

        if(tokensito.equalsIgnoreCase("")){
            return "redirect:/anErrorHasOcurred";
        }

        Persona personita = personaRepository.buscarPersonaPorTokensito(tokensito);

        if(personita == null){
            return "redirect:/anErrorHasOcurred";

        }else{
            if(password.equals(confirmarContrasena)){
                usuarioService.updatePassword(personita,password);
                redirectAttributes.addFlashAttribute("msgexitoso","Su contraseña se ha cambiado satisfactoriamente.");
                return "redirect:/sucessPassword";
            }else{
                redirectAttributes.addFlashAttribute("msg","Las constraseñas deben coincidir");
                return "redirect:/resetPassword?token="+tokensito;
            }
        }

    }

    @GetMapping("/sucessPassword")
    public String resetPassword(){
        return "login/actualizacionContrasena";
    }



    private void sendEmail(String correo, String resetPasswordLink) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("a20191566@pucp.edu.pe","TeleMillonario");
        helper.setTo(correo);

        String subject = "Link para cambiar contraseña";

        String content = "<p>Cordiales Saludos: </p>"
                + "<p>Se ha realizado una solicitud para el cambio de contraseña </p>"
                + "<p>Haga click en el siguiente link para cambiar su contraseña </p>"
                + "<p><b><a href="+ resetPasswordLink + ">Cambiar contraseña</a></b></p>"
                + "<p>Ignorar este mensaje , si usted no ha solicitado dicho cambio.</p>";

        helper.setSubject(subject);
        helper.setText(content,true);

        mailSender.send(message);

    }

    @GetMapping("/anErrorHasOcurred")
    public String error(Model model){
        model.addAttribute("status",404);
        model.addAttribute("error","");
        return "error";
    }


}


















