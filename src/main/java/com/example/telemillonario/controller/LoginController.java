package com.example.telemillonario.controller;


import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.entity.Rol;
import com.example.telemillonario.repository.FotoRepository;
import com.example.telemillonario.repository.PersonaRepository;
import com.example.telemillonario.repository.RolRepository;
import com.example.telemillonario.service.UsuarioService;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Controller
public class LoginController {

    @Autowired
    PersonaRepository personaRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private OAuth2AuthorizedClientService auth2AuthorizedClientService;

    @Autowired
    RolRepository rolRepository;

    @Autowired
    FotoRepository fotoRepository;

    @GetMapping("/list")
    public String listar(Model model, OAuth2AuthenticationToken authentication, HttpSession session){
        OAuth2AuthorizedClient client = auth2AuthorizedClientService.loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(),authentication.getName());
        String name = (String)  authentication.getPrincipal().getAttributes().get("given_name");
        String lastname = (String)  authentication.getPrincipal().getAttributes().get("family_name");
        String email = (String) authentication.getPrincipal().getAttributes().get("email");
        Persona personita = new Persona();
        personita.setNombres(name);
        personita.setApellidos(lastname);
        personita.setCorreo(email);
        String password = new BCryptPasswordEncoder().encode("123456789abcdefg");
        Persona persona = personaRepository.findByCorreo(email);
        if (persona == null){
            Rol rol = new Rol(2,1,"Usuario");
            personita.setIdrol(rol);
            personita.setContrasenia(password); //Campo password
            model.addAttribute("recontrasenia",password);
            model.addAttribute("usuario",personita);
            model.addAttribute("google", 1);
            /*
             * Faltarian fecha de nacimiento, dni, direccion y estado
             * */
            return "login/signup";
        }else if (persona.getCorreo().equals(personita.getCorreo()) && !persona.getContrasenia().equals(password)){
            /*Aca debe ir mensaje de error*/
            return "redirect:/login";
        }else if (persona.getCorreo().equals(personita.getCorreo()) && persona.getContrasenia().equals(password)){
            /*Aca se ingresa al sistema*/
            session.setAttribute("usuario",persona);
            return "redirect:/redirectByRole";
        }else {
            return "redirect:/login";
        }
    }

    @GetMapping("/login")
    public String loginForm(){
        return "login/signin";
    }

    @GetMapping("/crearCuenta")
    public String registrarse(@ModelAttribute("usuario") Persona usuario, Model model){
        return "login/signup";
    }


    @GetMapping("/redirectByRole")
    public String redirectByRole(Authentication auth, HttpSession session){

        Persona persona = null;
        if (session.getAttribute("usuario") == null){
            persona = personaRepository.findByCorreo(auth.getName());
            session.setAttribute("usuario",persona);
        }else{
            persona = (Persona) session.getAttribute("usuario");
        }


        switch(persona.getIdrol().getNombre()){
            case "Administrador":
                return "redirect:/admin/sedes";
            case "Operador":
                return "redirect:/operador/funciones/lista";
            default:
                String fotoPerfil;
                try{
                    fotoPerfil = fotoRepository.findByIdpersonaOrderByNumero(persona.getId()).get(0).getRuta();
                } catch (Exception e){
                    fotoPerfil = "/img/user.png";
                }

                session.setAttribute("fotoPerfil",fotoPerfil);
                return "redirect:/";
        }
    }


    @PostMapping("/validacionSignUp")
    public String validacionSignUp(@ModelAttribute("usuario") @Valid Persona usuario, BindingResult bindingResult,
                                   @RequestParam(value = "recontrasenia", required = false) String recontrasenia,
                                   @RequestParam(value = "google", required = false) Integer google,
                                   Model model,
                                   RedirectAttributes a) throws InterruptedException, IOException, MessagingException {

        boolean errorRecontrasenia = false;
        if (recontrasenia.equals("") || recontrasenia == null) {
            model.addAttribute("errRecontrasenia", 1);
            errorRecontrasenia = true;
        }

        boolean errorNacimiento = false;
        LocalDate fechaActual = LocalDate.now();
        LocalDate fechaRegistrada = usuario.getNacimiento();
        Period period = Period.between(fechaRegistrada, fechaActual);

        System.out.print(period.getYears() + " years,");
        System.out.print(period.getMonths() + " months,");
        System.out.print(period.getDays() + " days");

        if (period.getYears() < 0 || period.getMonths() < 0 || (period.getDays() <= 0 && (period.getYears() < 0 || period.getMonths() < 0))) {
            model.addAttribute("errDate", -1);
            errorNacimiento = true;
        }

        boolean coincidencias = false;
        List<Persona> listaPersonas = personaRepository.findAll();
        for (Persona p : listaPersonas) {
            if (p.getDni() != null && p.getDni().equals(usuario.getDni())) {
                model.addAttribute("errDni", 1);
                coincidencias = true;
            }
            if (p.getCorreo() != null && p.getCorreo().equals(usuario.getCorreo())) {
                coincidencias = true;
                model.addAttribute("errCorreo", 1);
            }
        }

         /*

                Aca va lo de la API DNI con tod0 y vlaidation

         */


        if(bindingResult.hasErrors() || coincidencias || errorRecontrasenia || errorNacimiento){
            if (google != null && google == 1) {
                model.addAttribute("google", 1);
            }
            return "/login/signup";
        } else {

            //generamos su bcript de contraseña
            String contraseniaBCrypt = new BCryptPasswordEncoder().encode(usuario.getContrasenia());
            usuario.setContrasenia(contraseniaBCrypt);

            //le asignamos el rol 2 -> usuario
            usuario.setIdrol(rolRepository.getById(2));
            //Estado -> 1
            usuario.setEstado(1);
            personaRepository.save(usuario);
            a.addFlashAttribute("msg", 1);

            try {
                sendEmailSuccessRegistration(usuario.getCorreo());
            } catch (MessagingException | UnsupportedEncodingException e) {
            }

            return "redirect:/login";
        }

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


        if(persona.getPasswordToken() == null || persona.getPasswordToken().equals("")){
                String token = RandomString.make(45);

                usuarioService.updateResetPassword(token,correo);

                //Generamos el link para el reseteo de contraseña

                String resetPasswordLink  = "http://localhost:8080/" + "resetPassword?token=" + token;

                try {
                    sendEmail(correo,resetPasswordLink);

                    String user = "root";
                    String password = "root";
                    String url = "jdbc:mysql://localhost:3306/telemillonario";

                    String sentenciaSQL = "CREATE EVENT telemillonario.eventoborrar"+persona.getId()+"\n ON SCHEDULE AT CURRENT_TIMESTAMP + INTERVAL 15 minute DO UPDATE telemillonario.persona SET passwordtoken = '' WHERE persona.correo = ?;";
                    try (Connection conn = DriverManager.getConnection(url, user, password);
                         PreparedStatement pstmt = conn.prepareStatement(sentenciaSQL);) {

                        pstmt.setString(1,correo);
                        pstmt.executeUpdate();

                    }

                } catch (MessagingException | UnsupportedEncodingException | SQLException e) {
                    redirectAttributes.addFlashAttribute("msg","Ha ocurrido un error, vuelve a intentarlo mas tarde.");
                    return "redirect:/cambioDeContrasenia";
                }

                redirectAttributes.addFlashAttribute("msgexito","Se ha enviado el link de recuperacion al correo ingresado");
                return "redirect:/cambioDeContrasenia";
        }
            else{
                redirectAttributes.addFlashAttribute("msg","Debe esperar 15 minutos para poder solicitar denuevo el cambio");
                return "redirect:/cambioDeContrasenia";
            }


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
            if((password.equals("") || password == null) || (confirmarContrasena.equals("") || confirmarContrasena == null)) {
                redirectAttributes.addFlashAttribute("msg1", "Llene ambos campos de contraseña");
                return "redirect:/resetPassword?token=" + tokensito;
            } else {
                if (password.equals(confirmarContrasena)) {

                    try {
                        String user = "root";
                        String pass = "root";
                        String url = "jdbc:mysql://localhost:3306/telemillonario";

                        String sentenciaSQL = "DROP EVENT IF EXISTS telemillonario.eventoborrar" + personita.getId() + ";";
                        try (Connection conn = DriverManager.getConnection(url, user, pass);
                             PreparedStatement pstmt = conn.prepareStatement(sentenciaSQL);) {
                            System.out.println("llego aca");
                            pstmt.execute();
                            System.out.println("pase la prueba");
                        }
                    } catch (SQLException e) {
                        return "redirect:/anErrorHasOcurred";
                    }

                    usuarioService.updatePassword(personita, password);
                    redirectAttributes.addFlashAttribute("msgexitoso", "Su contraseña se ha cambiado satisfactoriamente.");
                    return "redirect:/sucessPassword";
                } else {
                    redirectAttributes.addFlashAttribute("msg2", "Las constraseñas deben coincidir");
                    return "redirect:/resetPassword?token=" + tokensito;
                }
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

        helper.setFrom("TeleMillonario@gmail.com","TeleMillonario");
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

    private void sendEmailSuccessRegistration(String correo) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        String longin = "http://localhost:8080/login";

        helper.setFrom("TeleMillonario@gmail.com","TeleMillonario");
        helper.setTo(correo);

        String subject = "¡Bienvenido a Telemillonario!";

        String content = "<p>Cordiales Saludos: </p>"
                + "<p>Usted se ha registrado exitosamente a nuestra plataforma </p>"
                + "<p>Ahora usted podrá realizar lo siguiente: </p>"
                + "<p> - Acceso a la compra de boletos y visualización de su carrito de compras<p>"
                + "<p> - Acceso a su historial de compras, junto a las funciones a las que ha asistido<p>"
                + "<p> - Calificar la obra a la que ha asistido, a su director y a sus actores"
                + "<p>Ingrese sesión mediante el siguiente <b><a href=" + longin +">enlace</a></b><p>";

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


















