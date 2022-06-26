package com.example.telemillonario.controller;


import com.example.telemillonario.entity.Compra;
import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.entity.Rol;
import com.example.telemillonario.repository.CompraRepository;
import com.example.telemillonario.repository.FotoRepository;
import com.example.telemillonario.repository.PersonaRepository;
import com.example.telemillonario.repository.RolRepository;
import com.example.telemillonario.service.DatosAPI;
import com.example.telemillonario.service.DniAPI;
import com.example.telemillonario.service.UsuarioService;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Optional;

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

    @Autowired
    CompraRepository compraRepository;

    //String regex = "^[\\'\\-,.][^0-9_!¡?÷?¿/\\\\+=@#$%ˆ&*(){}|~<>;:[\\]]]{1,}$";

    @GetMapping("/loginByGoogle")
    public String listar(Model model, OAuth2AuthenticationToken authentication, HttpSession session, RedirectAttributes redirectAttributes,HttpServletRequest request){
        OAuth2AuthorizedClient client = auth2AuthorizedClientService.loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(),authentication.getName());
        String name = (String)  authentication.getPrincipal().getAttributes().get("given_name");
        String lastname = (String)  authentication.getPrincipal().getAttributes().get("family_name");
        String email = (String) authentication.getPrincipal().getAttributes().get("email");
        Persona personita = new Persona();
        personita.setNombres(name);
        personita.setApellidos(lastname);
        personita.setCorreo(email);
        String password = new BCryptPasswordEncoder().encode("123456789abcdefg");
        String pasw = null; //Contrase;a sacada de la base de datos
        System.out.println(password);
        Persona persona = personaRepository.findByCorreo(email);
        if (persona == null){
            Rol rol = new Rol(2,1,"Usuario");
            personita.setIdrol(rol);
            personita.setContrasenia("123456789abcdefg"); //Campo password
            model.addAttribute("recontrasenia","123456789abcdefg");
            model.addAttribute("usuario",personita);
            model.addAttribute("google", 1);
            /*
             * Faltarian fecha de nacimiento, dni, direccion y estado
             * */
            return "login/signup";
        }else if (persona.getCorreo().equals(personita.getCorreo()) && persona.getContrasenia().equals(password)){
            /*Aca debe ir mensaje de error*/
            return "redirect:/login";
        }else if (persona.getCorreo().equals(personita.getCorreo())){
            /*Aca se ingresa al sistema*/
            //redirectAttributes.addAttribute("username",email);
            //redirectAttributes.addAttribute("password","123456789abcdefg");
            session.setAttribute("usuario",persona);
            return "redirect:/redirectByRole";

        }else {
            return "redirect:/login";
        }
    }

    @GetMapping("/login")
    public String loginForm(HttpServletRequest request){

        //Permite regresar a la página anterior
        String urlAnterior = request.getHeader("Referer");
        if(urlAnterior!=null){
            request.getSession().setAttribute("urlAnterior", urlAnterior);
        }

        return "login/signin";
   }

    @GetMapping("/crearCuenta")
    public String registrarse(@ModelAttribute("usuario") Persona usuario, Model model){
        return "login/signup";
    }


    @GetMapping("/redirectByRole")
    public String redirectByRole(Authentication auth, HttpSession session){

        Persona persona = null;
        String urlAnterior = "/";

        // Obtiene la URL anterior y la quita de Sesión
        if(session.getAttribute("urlAnterior") != null){
            urlAnterior = (String) session.getAttribute("urlAnterior");
            session.removeAttribute("urlAnterior");
        }


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

                //Se regresa a la anterior url sólo si es usuario
                if(urlAnterior.contains("crearCuenta") || urlAnterior.contains("validacionSignUp") ||
                        urlAnterior.contains("login") || urlAnterior.contains("cambiar")){
                    return "redirect:/";
                }
                return "redirect:" + urlAnterior;
        }
    }


    @PostMapping("/validacionSignUp")
    public String validacionSignUp(@ModelAttribute("usuario") @Valid Persona usuario, BindingResult bindingResult,
                                   @RequestParam(value = "recontrasenia", required = false) String recontrasenia,
                                   @RequestParam(value = "google", required = false) Integer google,
                                   Model model,
                                   RedirectAttributes a) throws InterruptedException, IOException, MessagingException {

        System.out.println("------------------------------------------------------------------");
        //Validacion nombre
        /*Pattern pattern = Pattern.compile(regex);
        Matcher matcherNombres = pattern.matcher(usuario.getNombres());
        boolean errorNombre = matcherNombres.find();

        Pattern pattern1 = Pattern.compile(regex);
        Matcher matcherApellidos = pattern1.matcher(usuario.getApellidos());
        boolean errorApellido = matcherApellidos.find();


        if(errorNombre == false){
            errorNombre = true;
        }

        if(errorApellido == false){
            errorNombre = true;
        }

        System.out.println("Error Nombre: "+errorNombre);
        System.out.println("Errror Apellido: "+errorApellido);


        if(matcherApellidos.find() != false){
            errorApellido = true;
        }
        System.out.println("Errror Apellido 2: "+errorApellido);*/
        //Validacion contraseña y Recontraseña
        boolean errorRecontrasenia = false;
        boolean contraseniaDiferente = recontrasenia.equals(usuario.getContrasenia());
        if (recontrasenia.equals("") || recontrasenia == null || contraseniaDiferente == false) {
            model.addAttribute("errRecontrasenia", 1);
            errorRecontrasenia = true;
        }

        //Validacion Fecha de Nacimiento
        boolean errorNacimiento = false;
        LocalDate fechaActual = LocalDate.now();
        LocalDate fechaRegistrada = usuario.getNacimiento();
        Period period = Period.between(fechaRegistrada, fechaActual);
        if (period.getYears() < 0 || period.getMonths() < 0 || (period.getDays() <= 0 && (period.getYears() < 0 || period.getMonths() < 0))) {
            model.addAttribute("errDate", -1);
            errorNacimiento = true;
        }

        //Validacion DNI
        DatosAPI datosPersona = DniAPI.consulta(usuario.getDni());
        boolean errDNI = true;
        if(datosPersona.getSuccess().equalsIgnoreCase("true")){
            String nombresUpperCase = usuario.getNombres();
            String cadenaNormalize = Normalizer.normalize(nombresUpperCase,Normalizer.Form.NFD);
            nombresUpperCase = cadenaNormalize.replaceAll("[^\\p{ASCII}]", "").toUpperCase();
            String apellidosUpperCase = usuario.getApellidos();
            String cadenaNormalize2 = Normalizer.normalize(apellidosUpperCase,Normalizer.Form.NFD);
            apellidosUpperCase = cadenaNormalize2.replaceAll("[^\\p{ASCII}]", "").toUpperCase();
            String ApellidosUpperCase = nombresUpperCase + " " + apellidosUpperCase;
            String ApellidosAPI = datosPersona.getApellido_paterno() + " " + datosPersona.getApellido_materno();
            String NombresAPI = datosPersona.getNombres();
            if(ApellidosAPI.contains(apellidosUpperCase) && NombresAPI.contains(nombresUpperCase)){
                errDNI = false;
            }

        }

        boolean coincidencias = false;
        List<Persona> listaPersonas = personaRepository.findAll();
        for (Persona p : listaPersonas) {
            if (p.getDni() != null && p.getDni().equals(usuario.getDni())) {
                model.addAttribute("errDni", errDNI);
                coincidencias = true;
            }
            if (p.getCorreo() != null && p.getCorreo().equals(usuario.getCorreo())) {
                coincidencias = true;
                model.addAttribute("errCorreo", 1);
            }
        }

        //if(bindingResult.hasErrors() || coincidencias || errorRecontrasenia || errorNacimiento || errDNI || errorNombre || errorApellido){
        if(bindingResult.hasErrors() || coincidencias || errorRecontrasenia || errorNacimiento || errDNI){
            if (google != null && google == 1) {
                model.addAttribute("google", 1);
            }

            if(errDNI == true){
                model.addAttribute("errDni", errDNI);
            }

            /*if(errorNombre == true){
                model.addAttribute("errNombre", errorNombre);
            }

            if(errorApellido == true){
                model.addAttribute("errApellido", errorApellido);
            }*/
            return "login/signup";
        } else {

            String contraseniaBCrypt = new BCryptPasswordEncoder().encode(usuario.getContrasenia());
            usuario.setContrasenia(contraseniaBCrypt);

            //le asignamos el rol 2 -> usuario
            usuario.setIdrol(rolRepository.getById(2));
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
                            pstmt.execute();
                        }
                    } catch (SQLException e) {
                        return "redirect:/anErrorHasOcurred";
                    }

                    usuarioService.updatePassword(personita, password);
                    redirectAttributes.addFlashAttribute("msgexitoso", "Su contraseña se ha cambiado satisfactoriamente.");

                    //Envio de correo donde le indica que su contraseña se cambio exitosamente
                    try {
                        sendEmailSuccessRegistration(personita.getCorreo());
                    } catch (MessagingException | UnsupportedEncodingException e) {
                        return "redirect:/anErrorHasOcurred";
                    }

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

        //mailSender.send(message);

    }

    @GetMapping("/anErrorHasOcurred")
    public String error(Model model){
        model.addAttribute("status",404);
        model.addAttribute("error","");
        return "error";
    }


}


















