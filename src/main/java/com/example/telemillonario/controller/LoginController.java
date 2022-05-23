package com.example.telemillonario.controller;

import com.azure.core.annotation.Get;
import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.entity.Rol;
import com.example.telemillonario.repository.PersonaRepository;
import com.example.telemillonario.service.DniAPI;
import com.example.telemillonario.service.UsuarioAPI;
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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

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
        personita.setId(persona.getId());
        personita.setNombres(persona.getNombres());
        personita.setApellidos(persona.getApellidos());
        personita.setDni(persona.getDni());
        personita.setCorreo(persona.getCorreo());
        personita.setNacimiento(persona.getNacimiento());
        personita.setIdrol(persona.getIdrol());

        if (persona.getIdsede() !=null){
            personita.setIdsede(persona.getIdsede());
            System.out.println(personita.getIdsede().getId());
            System.out.println(personita.getIdsede().getNombre());
        }

        session.setAttribute("usuario",personita);
        System.out.println("llego aca");

        if(personita.getIdrol().getNombre().equalsIgnoreCase("Administrador")){
            return "redirect:/admin/sedes";

        }else if(personita.getIdrol().getNombre().equalsIgnoreCase("Usuario")){
            return "redirect:/";

        }else {
            return "redirect:/operador/funciones/lista"; //Cual es su pagina principal del Operador?
        }
    }

    @PostMapping("/validacionSignUp")
    public String validacionSignUp(@ModelAttribute("usuario") @Valid Persona usuario, BindingResult bindingResult,ModelAttribute modelAttribute) throws InterruptedException, IOException {

        if(bindingResult.hasErrors()){
            return "login/signup";
        }

        //Obtenemos los datos del usuario que ingreso su DNI con la API
        UsuarioAPI infoUsuarioAPI = DniAPI.FormRestAPI(usuario.getDni());

        //Aca va la validacion de lo obtenido de la API con lo ingresado por el usuario



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

        if(persona.getPasswordToken().equalsIgnoreCase("")){
            System.out.println("comillas");
        }else if(persona.getPasswordToken() == null){
            System.out.println("es null");
        }else{
            System.out.println("es ' ' ");
        }
        if(persona.getPasswordToken().equalsIgnoreCase("")){
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


















