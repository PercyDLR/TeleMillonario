package com.example.telemillonario.controller;

import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.entity.Rol;
import com.example.telemillonario.repository.PersonaRepository;
import com.example.telemillonario.repository.RolRepository;
import com.example.telemillonario.service.DniAPI;
import com.example.telemillonario.service.UsuarioAPI;
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

    @GetMapping("/prueba")
    public String prueba(){
        return "login/correoVerificacion";
    }

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
        Rol rol = new Rol(2,1,"Usuario");
        personita.setIdrol(rol);
        model.addAttribute("usuario",personita);
        /*
        * Faltarian contraseña, fecha de nacimiento, dni, direccion y estado
        * */
        return "login/signup";
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

        Persona persona = personaRepository.findByCorreo(auth.getName());
        session.setAttribute("usuario",persona);

        switch(persona.getIdrol().getNombre()){
            case "Administrador":
                return "redirect:/admin/sedes";
            case "Operador":
                return "redirect:/operador/funciones/lista";
            default:
                return "redirect:/";
        }
    }


    @PostMapping("/validacionSignUp")
    public String validacionSignUp(@ModelAttribute("usuario") @Valid Persona usuario, BindingResult bindingResult, Model model, RedirectAttributes a) throws InterruptedException, IOException {

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
        if(bindingResult.hasErrors() || coincidencias){
            return "/login/signup";
        } else {
            //Obtenemos los datos del usuario que ingreso su DNI con la API
            //UsuarioAPI infoUsuarioAPI = DniAPI.FormRestAPI(usuario.getDni());

            //Aca va la validacion de lo obtenido de la API con lo ingresado por el usuario

            //generamos su bcript de contraseña
            String contraseniaBCrypt = new BCryptPasswordEncoder().encode(usuario.getContrasenia());
            usuario.setContrasenia(contraseniaBCrypt);

            //le asignamos el rol 2 -> usuario
            usuario.setIdrol(rolRepository.getById(2));
            //Estado -> 1
            usuario.setEstado(1);
            personaRepository.save(usuario);
            a.addFlashAttribute("msg", 1);

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

        //System.out.println("---------------");
        /*if(persona.getPasswordToken().equalsIgnoreCase("")){
            System.out.println("comillas");
        }else if(persona.getPasswordToken() == null){
            System.out.println("es null");
        }else{
            System.out.println("es ' ' ");
        }*/
        //if(persona.getPasswordToken().equalsIgnoreCase("")){
        if(persona.getPasswordToken() == null){
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
            if(password.equals(confirmarContrasena)){

                try {
                    String user = "root";
                    String pass = "root";
                    String url = "jdbc:mysql://localhost:3306/telemillonario";

                    String sentenciaSQL = "DROP EVENT IF EXISTS telemillonario.eventoborrar"+personita.getId()+";";
                    try (Connection conn = DriverManager.getConnection(url, user, pass);
                         PreparedStatement pstmt = conn.prepareStatement(sentenciaSQL);) {
                        System.out.println("llego aca");
                        pstmt.execute();
                        System.out.println("pase la prueba");
                    }
                } catch (SQLException e) {
                    return "redirect:/anErrorHasOcurred";
                }

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

    @GetMapping("/anErrorHasOcurred")
    public String error(Model model){
        model.addAttribute("status",404);
        model.addAttribute("error","");
        return "error";
    }


}


















