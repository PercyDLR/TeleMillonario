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
import java.time.LocalDateTime;
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
            personita.setOauth2(1);
            personita.setContrasenia("123456789abcdefg"); //Campo password
            model.addAttribute("recontrasenia","123456789abcdefg");
            model.addAttribute("usuario",personita);
            model.addAttribute("google", 1);
            return "login/signup";
        }else if (persona.getCorreo().equals(personita.getCorreo()) && (persona.getOauth2() == 0)){
            try {
                redirectAttributes.addFlashAttribute("msg2", 1);
                session.invalidate();
                request.logout();
                return "login/signin";
            }catch (Exception e){

            }
            return "redirect:/login";
        }else if (persona.getCorreo().equals(personita.getCorreo()) && (persona.getOauth2() == 1)){
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
                String fotoPerfilOper;
                try{
                    fotoPerfilOper = fotoRepository.findByIdpersonaOrderByNumero(persona.getId()).get(0).getRuta();
                } catch (Exception e){
                    fotoPerfilOper = "/img/user.png";
                }

                session.setAttribute("fotoPerfil",fotoPerfilOper);

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
                        urlAnterior.contains("login") || urlAnterior.contains("cambiar") || urlAnterior.contains("sucessPassword")){
                    return "redirect:/";
                }
                return "redirect:" + urlAnterior;
        }
    }


    @PostMapping("/validacionSignUp")
    public String validacionSignUp(@ModelAttribute("usuario") @Valid Persona usuario, BindingResult bindingResult,
                                   @RequestParam(value = "recontrasenia", required = false) String recontrasenia,
                                   @RequestParam(value = "google", required = false) Integer google,
                                   Model model,HttpServletRequest request,
                                   RedirectAttributes a) throws InterruptedException, IOException, MessagingException {

        System.out.println("------------------------------------------------------------------");
        //Validacion nombre
        /*Pattern pattern = Pattern.compile(regex);
        Matcher matcherNombres = pattern.matcher(usuario.getNombres());
        boolean errorNombre = matcherNombres.find();

        Pattern pattern1 = Pattern.compile(regex);
        Matcher matcherApellidos = pattern1.matcher(usuario.getApellidos());
        boolean errorApellido = matcherApellidos.find();

        System.out.println(matcherNombres.find());
        System.out.println(matcherApellidos.find());

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
                String content = obtenerContentCreacionCuenta(usuario,request);
                sendEmailSuccessRegistration(usuario.getCorreo(),content);
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
    public String processForgotPasswordForm(@RequestParam("correo") String correo,RedirectAttributes redirectAttributes,HttpServletRequest request){

        Persona persona = personaRepository.findByCorreo(correo);

        if(persona == null){
            redirectAttributes.addFlashAttribute("msg","Ingrese un correo válido");
            return "redirect:/cambioDeContrasenia";
        }


        if(persona.getPasswordToken() == null || persona.getPasswordToken().equals("")){
                String token = RandomString.make(45);

                usuarioService.updateResetPassword(token,correo);


                //String resetPasswordLink  = "http://localhost:8080/" + "resetPassword?token=" + token;
                String resetPasswordLink = "http://"+request.getServerName()+":"+request.getServerPort()+"/resetPassword?token=" + token;
            try {
                    sendEmail(correo,resetPasswordLink);

                    /*String user = "root";
                    String password = "root";
                    String url = "jdbc:mysql://localhost:3306/telemillonario";*/

                    String user = "adminsito";
                    String password = "Telemillonariogrupo2";
                    String url = "jdbc:mysql://bdtelemillonario.mysql.database.azure.com:3306/telemillonario";

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
    public String reseteadaDeContrasenia(@RequestParam("token") String tokensito,@RequestParam("password") String password,@RequestParam("repassword") String confirmarContrasena,HttpServletRequest request,RedirectAttributes redirectAttributes){

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
                        /*String user = "root";
                        String pass = "root";
                        String url = "jdbc:mysql://localhost:3306/telemillonario";*/

                        String user = "adminsito";
                        String pass = "Telemillonariogrupo2";
                        String url = "jdbc:mysql://bdtelemillonario.mysql.database.azure.com:3306/telemillonario";

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

                    try {
                        String urlpath="http://"+request.getServerName()+":"+request.getServerPort()+"/login";
                        sendEmailChangePassSuccesfully(personita.getCorreo(),urlpath);

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

        String content = obtenerContentCambioPass(resetPasswordLink);

        helper.setSubject(subject);
        helper.setText(content,true);

        mailSender.send(message);

    }

    private void sendEmailSuccessRegistration(String correo,String content) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);



        helper.setFrom("TeleMillonario@gmail.com","TeleMillonario");
        helper.setTo(correo);

        String subject = "¡Bienvenido a Telemillonario!";

        helper.setSubject(subject);
        helper.setText(content,true);

        mailSender.send(message);

    }

    private void sendEmailChangePassSuccesfully(String correo,String link)throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);


        helper.setFrom("TeleMillonario@gmail.com","TeleMillonario");
        helper.setTo(correo);

        String subject = "Cambio Exitoso de Contraseña";

        String content = obtenerContentCambioExitosoPass(link);

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

    private String obtenerContentCambioExitosoPass(String link){
        String content = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "\n" +
                "    <head>\n" +
                "        <title></title>\n" +
                "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "        <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" +
                "        <style type=\"text/css\">\n" +
                "            @media screen {\n" +
                "                @font-face {\n" +
                "                    font-family: 'Lato';\n" +
                "                    font-style: normal;\n" +
                "                    font-weight: 400;\n" +
                "                    src: local('Lato Regular'), local('Lato-Regular'), url(https://fonts.gstatic.com/s/lato/v11/qIIYRU-oROkIk8vfvxw6QvesZW2xOQ-xsNqO47m55DA.woff) format('woff');\n" +
                "                }\n" +
                "\n" +
                "                @font-face {\n" +
                "                    font-family: 'Lato';\n" +
                "                    font-style: normal;\n" +
                "                    font-weight: 700;\n" +
                "                    src: local('Lato Bold'), local('Lato-Bold'), url(https://fonts.gstatic.com/s/lato/v11/qdgUG4U09HnJwhYI-uK18wLUuEpTyoUstqEm5AMlJo4.woff) format('woff');\n" +
                "                }\n" +
                "\n" +
                "                @font-face {\n" +
                "                    font-family: 'Lato';\n" +
                "                    font-style: italic;\n" +
                "                    font-weight: 400;\n" +
                "                    src: local('Lato Italic'), local('Lato-Italic'), url(https://fonts.gstatic.com/s/lato/v11/RYyZNoeFgb0l7W3Vu1aSWOvvDin1pK8aKteLpeZ5c0A.woff) format('woff');\n" +
                "                }\n" +
                "\n" +
                "                @font-face {\n" +
                "                    font-family: 'Lato';\n" +
                "                    font-style: italic;\n" +
                "                    font-weight: 700;\n" +
                "                    src: local('Lato Bold Italic'), local('Lato-BoldItalic'), url(https://fonts.gstatic.com/s/lato/v11/HkF_qI1x_noxlxhrhMQYELO3LdcAZYWl9Si6vvxL-qU.woff) format('woff');\n" +
                "                }\n" +
                "            }\n" +
                "\n" +
                "            /* CLIENT-SPECIFIC STYLES */\n" +
                "            body,\n" +
                "            table,\n" +
                "            td,\n" +
                "            a {\n" +
                "                -webkit-text-size-adjust: 100%;\n" +
                "                -ms-text-size-adjust: 100%;\n" +
                "            }\n" +
                "\n" +
                "            table,\n" +
                "            td {\n" +
                "                mso-table-lspace: 0pt;\n" +
                "                mso-table-rspace: 0pt;\n" +
                "            }\n" +
                "\n" +
                "            img {\n" +
                "                -ms-interpolation-mode: bicubic;\n" +
                "            }\n" +
                "\n" +
                "            /* RESET STYLES */\n" +
                "            img {\n" +
                "                border: 0;\n" +
                "                height: auto;\n" +
                "                line-height: 100%;\n" +
                "                outline: none;\n" +
                "                text-decoration: none;\n" +
                "            }\n" +
                "\n" +
                "            table {\n" +
                "                border-collapse: collapse !important;\n" +
                "            }\n" +
                "\n" +
                "            body {\n" +
                "                height: 100% !important;\n" +
                "                margin: 0 !important;\n" +
                "                padding: 0 !important;\n" +
                "                width: 100% !important;\n" +
                "            }\n" +
                "\n" +
                "            /* iOS BLUE LINKS */\n" +
                "            a[x-apple-data-detectors] {\n" +
                "                color: inherit !important;\n" +
                "                text-decoration: none !important;\n" +
                "                font-size: inherit !important;\n" +
                "                font-family: inherit !important;\n" +
                "                font-weight: inherit !important;\n" +
                "                line-height: inherit !important;\n" +
                "            }\n" +
                "\n" +
                "            /* MOBILE STYLES */\n" +
                "            @media screen and (max-width:600px) {\n" +
                "                h1 {\n" +
                "                    font-size: 32px !important;\n" +
                "                    line-height: 32px !important;\n" +
                "                }\n" +
                "            }\n" +
                "\n" +
                "            /* ANDROID CENTER FIX */\n" +
                "            div[style*=\"margin: 16px 0;\"] {\n" +
                "                margin: 0 !important;\n" +
                "            }\n" +
                "        </style>\n" +
                "    </head>\n" +
                "\n" +
                "    <body style=\"background-color: #f4f4f4; margin: 0 !important; padding: 0 !important;\">\n" +
                "        <div tabindex=\"-1\" class=\"YE9Rk customScrollBar\" data-is-scrollable=\"true\">\n" +
                "            <div>\n" +
                "                <div class=\"wide-content-host\">\n" +
                "            <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" +
                "                <tr>\n" +
                "                    <td  align=\"center\">\n" +
                "                        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
                "                            <tr>\n" +
                "                                <td align=\"center\" valign=\"top\" style=\"padding: 40px 10px 40px 10px;\"> </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td  align=\"center\" style=\"padding: 0px 10px 0px 10px;\">\n" +
                "                        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
                "                           <tr>\n" +
                "                                <td height=\"80\" style='background-color: #2b2b31;'>\n" +
                "                                    <img data-imagetype=\"AttachmentByCid\"  naturalheight=\"0\" naturalwidth=\"0\" src='https://telemillonariocontainer.blob.core.windows.net/telemillonario/tele.png' alt=\"logo_cinemark\" style=\"width: 250px; margin: 0px auto; display: block; cursor: pointer; min-width: auto; min-height: auto;\" crossorigin=\"use-credentials\" class=\"ADIae\">\n" +
                "                                </td>\n" +
                "                           </tr>\n" +
                "                        </table>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td bgcolor=\"#f4f4f4\" align=\"center\" style=\"padding: 0px 10px 0px 10px;\">\n" +
                "                        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
                "                            <tr>\n" +
                "                                <td bgcolor=\"#ffffff\" align=\"left\" style=\"padding: 20px 30px 40px 30px; color: #666666; font-family: 'Lato', Helvetica, Arial, sans-serif; font-size: 18px; font-weight: 400; line-height: 25px;\">\n" +
                "                                    <p style=\"margin: 0;\">Cordiales Saludos:</p>\n" +
                "                                    <br>\n" +
                "                                    <p style=\"margin: 0;\">Su contraseña ha sido cambiado satisfactoriamente. Haga click en el siguiente enlace para poder iniciar sesión.</p>\n" +
                "                                    <br>\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                               <center><button align='center' style='border-radius: 3px; background-color:#ff5860'><a href='"+link+"' target=\"_blank\" style=\"font-size: 20px; font-family: Helvetica, Arial, sans-serif; color: #ffffff; text-decoration: none; color: #ffffff; text-decoration: none; padding: 15px 25px; border-radius: 2px; display: inline-block;\">Iniciar Sesión</a></button></center>\n" +
                "                            </tr>\n"+
                "                        </table>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </body>\n" +
                "\n" +
                "</html>";

        return  content;
    }

    private String obtenerContentCambioPass(String link){
        String content = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "\n" +
                "    <head>\n" +
                "        <title></title>\n" +
                "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "        <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" +
                "        <style type=\"text/css\">\n" +
                "            @media screen {\n" +
                "                @font-face {\n" +
                "                    font-family: 'Lato';\n" +
                "                    font-style: normal;\n" +
                "                    font-weight: 400;\n" +
                "                    src: local('Lato Regular'), local('Lato-Regular'), url(https://fonts.gstatic.com/s/lato/v11/qIIYRU-oROkIk8vfvxw6QvesZW2xOQ-xsNqO47m55DA.woff) format('woff');\n" +
                "                }\n" +
                "\n" +
                "                @font-face {\n" +
                "                    font-family: 'Lato';\n" +
                "                    font-style: normal;\n" +
                "                    font-weight: 700;\n" +
                "                    src: local('Lato Bold'), local('Lato-Bold'), url(https://fonts.gstatic.com/s/lato/v11/qdgUG4U09HnJwhYI-uK18wLUuEpTyoUstqEm5AMlJo4.woff) format('woff');\n" +
                "                }\n" +
                "\n" +
                "                @font-face {\n" +
                "                    font-family: 'Lato';\n" +
                "                    font-style: italic;\n" +
                "                    font-weight: 400;\n" +
                "                    src: local('Lato Italic'), local('Lato-Italic'), url(https://fonts.gstatic.com/s/lato/v11/RYyZNoeFgb0l7W3Vu1aSWOvvDin1pK8aKteLpeZ5c0A.woff) format('woff');\n" +
                "                }\n" +
                "\n" +
                "                @font-face {\n" +
                "                    font-family: 'Lato';\n" +
                "                    font-style: italic;\n" +
                "                    font-weight: 700;\n" +
                "                    src: local('Lato Bold Italic'), local('Lato-BoldItalic'), url(https://fonts.gstatic.com/s/lato/v11/HkF_qI1x_noxlxhrhMQYELO3LdcAZYWl9Si6vvxL-qU.woff) format('woff');\n" +
                "                }\n" +
                "            }\n" +
                "\n" +
                "            /* CLIENT-SPECIFIC STYLES */\n" +
                "            body,\n" +
                "            table,\n" +
                "            td,\n" +
                "            a {\n" +
                "                -webkit-text-size-adjust: 100%;\n" +
                "                -ms-text-size-adjust: 100%;\n" +
                "            }\n" +
                "\n" +
                "            table,\n" +
                "            td {\n" +
                "                mso-table-lspace: 0pt;\n" +
                "                mso-table-rspace: 0pt;\n" +
                "            }\n" +
                "\n" +
                "            img {\n" +
                "                -ms-interpolation-mode: bicubic;\n" +
                "            }\n" +
                "\n" +
                "            /* RESET STYLES */\n" +
                "            img {\n" +
                "                border: 0;\n" +
                "                height: auto;\n" +
                "                line-height: 100%;\n" +
                "                outline: none;\n" +
                "                text-decoration: none;\n" +
                "            }\n" +
                "\n" +
                "            table {\n" +
                "                border-collapse: collapse !important;\n" +
                "            }\n" +
                "\n" +
                "            body {\n" +
                "                height: 100% !important;\n" +
                "                margin: 0 !important;\n" +
                "                padding: 0 !important;\n" +
                "                width: 100% !important;\n" +
                "            }\n" +
                "\n" +
                "            /* iOS BLUE LINKS */\n" +
                "            a[x-apple-data-detectors] {\n" +
                "                color: inherit !important;\n" +
                "                text-decoration: none !important;\n" +
                "                font-size: inherit !important;\n" +
                "                font-family: inherit !important;\n" +
                "                font-weight: inherit !important;\n" +
                "                line-height: inherit !important;\n" +
                "            }\n" +
                "\n" +
                "            /* MOBILE STYLES */\n" +
                "            @media screen and (max-width:600px) {\n" +
                "                h1 {\n" +
                "                    font-size: 32px !important;\n" +
                "                    line-height: 32px !important;\n" +
                "                }\n" +
                "            }\n" +
                "\n" +
                "            /* ANDROID CENTER FIX */\n" +
                "            div[style*=\"margin: 16px 0;\"] {\n" +
                "                margin: 0 !important;\n" +
                "            }\n" +
                "        </style>\n" +
                "    </head>\n" +
                "\n" +
                "    <body style=\"background-color: #f4f4f4; margin: 0 !important; padding: 0 !important;\">\n" +
                "        <div tabindex=\"-1\" class=\"YE9Rk customScrollBar\" data-is-scrollable=\"true\">\n" +
                "            <div>\n" +
                "                <div class=\"wide-content-host\">\n" +
                "            <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" +
                "                <tr>\n" +
                "                    <td  align=\"center\">\n" +
                "                        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
                "                            <tr>\n" +
                "                                <td align=\"center\" valign=\"top\" style=\"padding: 40px 10px 40px 10px;\"> </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td  align=\"center\" style=\"padding: 0px 10px 0px 10px;\">\n" +
                "                        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
                "                           <tr>\n" +
                "                                <td height=\"80\" style='background-color: #2b2b31;'>\n" +
                "                                    <img data-imagetype=\"AttachmentByCid\"  naturalheight=\"0\" naturalwidth=\"0\" src='https://telemillonariocontainer.blob.core.windows.net/telemillonario/tele.png' alt=\"logo_cinemark\" style=\"width: 250px; margin: 0px auto; display: block; cursor: pointer; min-width: auto; min-height: auto;\" crossorigin=\"use-credentials\" class=\"ADIae\">\n" +
                "                                </td>\n" +
                "                           </tr>\n" +
                "                        </table>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td bgcolor=\"#f4f4f4\" align=\"center\" style=\"padding: 0px 10px 0px 10px;\">\n" +
                "                        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
                "                            <tr>\n" +
                "                                <td bgcolor=\"#ffffff\" align=\"left\" style=\"padding: 20px 30px 40px 30px; color: #666666; font-family: 'Lato', Helvetica, Arial, sans-serif; font-size: 18px; font-weight: 400; line-height: 25px;\">\n" +
                "                                    <p style=\"margin: 0;\">Cordiales Saludos:</p>\n" +
                "                                    <br>\n" +
                "                                    <p style=\"margin: 0;\">Se ha realizado una solicitud para el cambio de contraseña. Haga click en el siguiente enlace para realizar su cambio.</p>\n" +
                "                                    <br>\n" +
                "                                    <p style=\"margin: 0;\">Ignorar este mensaje si usted no ha solicitado dicho cambio.</p>\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                               <center><button align='center' style='border-radius: 3px; background-color:#ff5860'><a href='"+link+"' target=\"_blank\" style=\"font-size: 20px; font-family: Helvetica, Arial, sans-serif; color: #ffffff; text-decoration: none; color: #ffffff; text-decoration: none; padding: 15px 25px; border-radius: 2px; display: inline-block;\">Cambiar Contraseña</a></button></center>\n" +
                "                            </tr>\n"+
                "                        </table>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </body>\n" +
                "\n" +
                "</html>";

        return  content;
    }


    private String obtenerContentCreacionCuenta(Persona usuario,HttpServletRequest request){
        String urlpath="http://"+request.getServerName()+":"+request.getServerPort()+"/login";
        String content = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "\n" +
                "    <head>\n" +
                "        <title></title>\n" +
                "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "        <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" +
                "        <style type=\"text/css\">\n" +
                "            @media screen {\n" +
                "                @font-face {\n" +
                "                    font-family: 'Lato';\n" +
                "                    font-style: normal;\n" +
                "                    font-weight: 400;\n" +
                "                    src: local('Lato Regular'), local('Lato-Regular'), url(https://fonts.gstatic.com/s/lato/v11/qIIYRU-oROkIk8vfvxw6QvesZW2xOQ-xsNqO47m55DA.woff) format('woff');\n" +
                "                }\n" +
                "\n" +
                "                @font-face {\n" +
                "                    font-family: 'Lato';\n" +
                "                    font-style: normal;\n" +
                "                    font-weight: 700;\n" +
                "                    src: local('Lato Bold'), local('Lato-Bold'), url(https://fonts.gstatic.com/s/lato/v11/qdgUG4U09HnJwhYI-uK18wLUuEpTyoUstqEm5AMlJo4.woff) format('woff');\n" +
                "                }\n" +
                "\n" +
                "                @font-face {\n" +
                "                    font-family: 'Lato';\n" +
                "                    font-style: italic;\n" +
                "                    font-weight: 400;\n" +
                "                    src: local('Lato Italic'), local('Lato-Italic'), url(https://fonts.gstatic.com/s/lato/v11/RYyZNoeFgb0l7W3Vu1aSWOvvDin1pK8aKteLpeZ5c0A.woff) format('woff');\n" +
                "                }\n" +
                "\n" +
                "                @font-face {\n" +
                "                    font-family: 'Lato';\n" +
                "                    font-style: italic;\n" +
                "                    font-weight: 700;\n" +
                "                    src: local('Lato Bold Italic'), local('Lato-BoldItalic'), url(https://fonts.gstatic.com/s/lato/v11/HkF_qI1x_noxlxhrhMQYELO3LdcAZYWl9Si6vvxL-qU.woff) format('woff');\n" +
                "                }\n" +
                "            }\n" +
                "\n" +
                "            /* CLIENT-SPECIFIC STYLES */\n" +
                "            body,\n" +
                "            table,\n" +
                "            td,\n" +
                "            a {\n" +
                "                -webkit-text-size-adjust: 100%;\n" +
                "                -ms-text-size-adjust: 100%;\n" +
                "            }\n" +
                "\n" +
                "            table,\n" +
                "            td {\n" +
                "                mso-table-lspace: 0pt;\n" +
                "                mso-table-rspace: 0pt;\n" +
                "            }\n" +
                "\n" +
                "            img {\n" +
                "                -ms-interpolation-mode: bicubic;\n" +
                "            }\n" +
                "\n" +
                "            /* RESET STYLES */\n" +
                "            img {\n" +
                "                border: 0;\n" +
                "                height: auto;\n" +
                "                line-height: 100%;\n" +
                "                outline: none;\n" +
                "                text-decoration: none;\n" +
                "            }\n" +
                "\n" +
                "            table {\n" +
                "                border-collapse: collapse !important;\n" +
                "            }\n" +
                "\n" +
                "            body {\n" +
                "                height: 100% !important;\n" +
                "                margin: 0 !important;\n" +
                "                padding: 0 !important;\n" +
                "                width: 100% !important;\n" +
                "            }\n" +
                "\n" +
                "            /* iOS BLUE LINKS */\n" +
                "            a[x-apple-data-detectors] {\n" +
                "                color: inherit !important;\n" +
                "                text-decoration: none !important;\n" +
                "                font-size: inherit !important;\n" +
                "                font-family: inherit !important;\n" +
                "                font-weight: inherit !important;\n" +
                "                line-height: inherit !important;\n" +
                "            }\n" +
                "\n" +
                "            /* MOBILE STYLES */\n" +
                "            @media screen and (max-width:600px) {\n" +
                "                h1 {\n" +
                "                    font-size: 32px !important;\n" +
                "                    line-height: 32px !important;\n" +
                "                }\n" +
                "            }\n" +
                "\n" +
                "            /* ANDROID CENTER FIX */\n" +
                "            div[style*=\"margin: 16px 0;\"] {\n" +
                "                margin: 0 !important;\n" +
                "            }\n" +
                "        </style>\n" +
                "    </head>\n" +
                "\n" +
                "    <body style=\"background-color: #f4f4f4; margin: 0 !important; padding: 0 !important;\">\n" +
                "        <div tabindex=\"-1\" class=\"YE9Rk customScrollBar\" data-is-scrollable=\"true\">\n" +
                "            <div>\n" +
                "                <div class=\"wide-content-host\">\n" +
                "            <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" +
                "                <tr>\n" +
                "                    <td  align=\"center\">\n" +
                "                        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
                "                            <tr>\n" +
                "                                <td align=\"center\" valign=\"top\" style=\"padding: 40px 10px 40px 10px;\"> </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td  align=\"center\" style=\"padding: 0px 10px 0px 10px;\">\n" +
                "                        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
                "                           <tr>\n" +
                "                                <td height=\"80\" style='background-color: #2b2b31;'>\n" +
                "                                    <img data-imagetype=\"AttachmentByCid\"  naturalheight=\"0\" naturalwidth=\"0\" src='https://telemillonariocontainer.blob.core.windows.net/telemillonario/tele.png' alt=\"logo_cinemark\" style=\"width: 250px; margin: 0px auto; display: block; cursor: pointer; min-width: auto; min-height: auto;\" crossorigin=\"use-credentials\" class=\"ADIae\">\n" +
                "                                </td>\n" +
                "                           </tr>\n" +
                "                           <tr>\n" +
                "                              <td height=\"80\" bgcolor=\"#ff5860\" style=\"padding:0 20px\">\n" +
                "                                  <h2 style=\"font-family:'Arial',sans-serif; font-weight:500; font-size:20px; margin:0 0 5px\">\n" +
                "                                      <center><span style=\"color:#fff;text-transform:uppercase; font-family:'Arial',sans-serif\">¡ Bienvenide "+usuario.getNombres()+' '+usuario.getApellidos()+ " ! </span></center>\n" +
                "                                  </h2>\n" +
                "                              </td>\n" +
                "                           </tr>\n" +
                "                        </table>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td bgcolor=\"#f4f4f4\" align=\"center\" style=\"padding: 0px 10px 0px 10px;\">\n" +
                "                        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
                "                            <tr>\n" +
                "                                <td bgcolor=\"#ffffff\" align=\"left\" style=\"padding: 20px 30px 40px 30px; color: #666666; font-family: 'Lato', Helvetica, Arial, sans-serif; font-size: 18px; font-weight: 400; line-height: 25px;\">\n" +
                "                                    <p style=\"margin: 0;\">Usted se ha registrado exitosamente a nuestra plataforma. Ahora podra realizar lo siguiente:</p>\n" +
                "                                    <br>\n" +
                "                                    <p style=\"margin: 0;\">1. Acceso a la compra de boletos y visualización de su carrito de compras</p>\n" +
                "                                    <br>\n" +
                "                                    <p style=\"margin: 0;\">2. Acceso a su historial de compras, junto a las funciones a las que ha asistido</p>\n" +
                "                                    <br>\n" +
                "                                    <p style=\"margin: 0;\">3. Calificar la obra a la que ha asistido, a su director y a sus actores</p>\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                                <td bgcolor=\"#ffffff\" align=\"left\">\n" +
                "                                    <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                        <tr>\n" +
                "                                            <td bgcolor=\"#ffffff\" align=\"center\" style=\"padding: 20px 30px 60px 30px;\">\n" +
                "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                                    <tr>\n" +
                "                                                        <td align=\"center\" style=\"border-radius: 3px;\" bgcolor='#ff5860'><a href='"+urlpath+"' target=\"_blank\" style=\"font-size: 20px; font-family: Helvetica, Arial, sans-serif; color: #ffffff; text-decoration: none; color: #ffffff; text-decoration: none; padding: 15px 25px; border-radius: 2px; display: inline-block;\">Iniciar Sesión</a></td>\n" +
                "                                                    </tr>\n" +
                "                                                </table>\n" +
                "                                            </td>\n" +
                "                                        </tr>\n" +
                "                                    </table>\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </body>\n" +
                "\n" +
                "</html>";
        return content;
    }
}


















