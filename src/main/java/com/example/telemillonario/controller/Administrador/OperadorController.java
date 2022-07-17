package com.example.telemillonario.controller.Administrador;

import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.entity.Rol;
import com.example.telemillonario.repository.PersonaRepository;
import com.example.telemillonario.repository.RolRepository;
import com.example.telemillonario.repository.SedeRepository;
import com.example.telemillonario.service.DatosAPI;
import com.example.telemillonario.service.DniAPI;
import com.example.telemillonario.validation.Operador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/operadores")
public class OperadorController {

    @Autowired
    RolRepository rolRepository;
    @Autowired
    PersonaRepository personaRepository;
    @Autowired
    SedeRepository sedeRepository;

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("")
    public String listarOperadores(Model model) {
        List<Persona> operadores = personaRepository.listarOperadores();
        //considerando una cantidad de Operador inicial base de 5
        int cantidad_por_pagina = 5;//modificar en caso se requiera variar la cantidad de operadores por pagina
        if(operadores.size()%cantidad_por_pagina==0){
            //quiere decir que tengo un multiplo
            model.addAttribute("paginacion",(operadores.size())/cantidad_por_pagina);
        }else{
            //quiere decir que tengo un número No multiplo de la cantidad de elementos por página
            model.addAttribute("paginacion",(operadores.size()/cantidad_por_pagina)+1);
        }
        model.addAttribute("listaSede", sedeRepository.findAll());
        model.addAttribute("pagActual",0);
        model.addAttribute("listaOperadores", operadores);
        //Usar en caso se requiera
        //model.addAttribute("listaOperadores",operadores.subList(1,cantidad_por_pagina));
        return "Administrador/Operador/listaOperadores";
    }

    @GetMapping(value = {"/listar","/listar/a"})
    public String listarPorPagina(@RequestParam(value = "pag", required = false) Optional<String> num,Model model,RedirectAttributes attr){
        if(num.isPresent()){
            try{
                //verificar si el numero se encuentra dentro del rango de la paginacion
                int cantidad_por_pagina = 5;
                int num_paginas;
                int num_pagina = Integer.parseInt(num.get());
                if(personaRepository.listarOperadores().size()%cantidad_por_pagina==0){
                    num_paginas = personaRepository.listarOperadores().size()/cantidad_por_pagina;
                }else{
                    num_paginas = (personaRepository.listarOperadores().size()/cantidad_por_pagina) + 1;
                }
                //una vez obtenidos los numeros
                if(num_pagina>0&&num_pagina<=num_paginas){
                    List<Persona> operadores = personaRepository.listarOperadores();
                    List<Persona> operadores_filtrados = operadores.subList((cantidad_por_pagina*(num_pagina-1)+1),(cantidad_por_pagina*(num_pagina-1)+cantidad_por_pagina+1));
                    model.addAttribute("listaOperadores",operadores_filtrados);
                    model.addAttribute("pagActual",num_pagina);
                    model.addAttribute("paginacion",num_paginas);
                    return "Administrador/Operador/listaOperadores";
                }else{
//                    attr.addFlashAttribute("msg","Debe ingresar un numero de página entre"+1+" y "+num_paginas);
                    return "redirect:/admin/operadores";
                }
            }catch (Exception e){
                attr.addFlashAttribute("msg","Debe ingresar un numero para la página");
                return "redirect:/admin/operadores";
            }
        }else{
            attr.addFlashAttribute("msg","Debe Ingresar un valor de paginación");
            return "redirect:/admin/operadores";
        }
    }

    @GetMapping(value = "/crear")
    public String crearOperador(Model model,@ModelAttribute("operador") Persona operador) {
        model.addAttribute("listaSedes", sedeRepository.findByEstado(1));
        return "Administrador/Operador/agregarOperadores";
    }

    @PostMapping(value = "/buscar")
    public String filtrarOperador(Model model,@RequestParam("filtro") String filtro,@RequestParam("nombre") String nombre,RedirectAttributes attr){
        if(filtro.equals("0") && nombre.equals("")){
            return "redirect:/admin/operadores";
        }else{
            //algo está buscando
            if(filtro.equals("0")&& !nombre.equals("")){
                System.out.println(filtro);
                System.out.println(nombre);
                //busqueda solo por nombre
                attr.addFlashAttribute("msg","resultado filtrado por nombre");
                model.addAttribute("listaSede", sedeRepository.findAll());
                model.addAttribute("listaOperadores",personaRepository.listarOperadoresPorNombre(nombre));
            }
            if(nombre.equals("") && !filtro.equals("0")){
                System.out.println(filtro);
                System.out.println(nombre);
                //busqueda solo por el filtro
                attr.addFlashAttribute("msg","resultado filtrado por "+filtro);
                model.addAttribute("listaSede", sedeRepository.findAll());
                model.addAttribute("listaOperadores",personaRepository.listarOperadoresPorFiltro(filtro));
            }
            if(!nombre.equals("") && !filtro.equals("0")){
                System.out.println(filtro);
                System.out.println(nombre);
                //busqueda por filtro y por nombre
                attr.addFlashAttribute("msg"," resultado filtrado por "+filtro+" y por nombre");
                model.addAttribute("listaSede", sedeRepository.findAll());
                model.addAttribute("listaOperadores",personaRepository.listarOperadoresPorFiltroyNombre(nombre,filtro));

            }
            model.addAttribute("busqueda",nombre);
            return "Administrador/Operador/listaOperadores";
        }
    }

    @GetMapping(value = "/editar")
    public String editarOperador(Model model, @RequestParam("id") Optional<String> optid, RedirectAttributes attr, @ModelAttribute("operador") Persona operador_b) {
        if (optid.isPresent()) {
            try {
                int id = Integer.parseInt(optid.get());
                if (personaRepository.existsById(id)) {
                    Optional<Persona> operador = personaRepository.findById(id);
                    model.addAttribute("listaSedes", sedeRepository.findByEstado(1));
                    model.addAttribute("operador", operador.get());
                    return "Administrador/Operador/editarOperadores";
                } else {
                    attr.addFlashAttribute("msg", "No existe el operador con el ID" + id);
                    return "redirect:/admin/operadores/";
                }
            } catch (Exception e) {
                attr.addFlashAttribute("msg", "Envió un ID inválido");
                return "redirect:/admin/operadores/";
            }
        } else {
            attr.addFlashAttribute("msg", "Se envió el ID vacío");
            return "redirect:/admin/operadores/";
        }
    }

    @PostMapping(value = "/guardar")
    public String guardarOperador(@ModelAttribute("operador") @Validated(Operador.class) Persona operador, BindingResult bindingResult, RedirectAttributes attr, Model model,HttpServletRequest request) {
        try {
            Integer id = operador.getId();
            System.out.println(id);
            if (id != null) {
            //if (personaRepository.existsById(id)) {
                //Editar Operador
                if(bindingResult.hasErrors()||operador.getIdsede()==null){
                    System.out.println(bindingResult.getFieldError());
                    if(operador.getIdsede()==null){
                        //attr.addFlashAttribute("msg","La sede del operador no puede quedar vacía");
                    }
                    return "Administrador/Operador/editarOperadores";
                }else{
                    Optional<Persona> aux = personaRepository.findById(id);
                    Persona op = aux.get();

                    op.setEstado(1);
                    //Creación de rol
                    Rol rol = new Rol();
                    rol.setId(3);
                    rol.setEstado(1);//Opcional
                    rol.setNombre("Operador");//Opcional

                    op.setIdrol(rol);
                    //DNI / Nombre / Apellido son campos no editables
                    op.setIdsede(operador.getIdsede());
                    personaRepository.save(op);
                    attr.addFlashAttribute("msg2","Se actualizó el operador de manera exitosa");
                    return "redirect:/admin/operadores/";
                }
            } else {
                //Agregar Operador
                if(bindingResult.hasErrors()||operador.getIdsede()==null){
                    System.out.println(bindingResult.getFieldError());
                    model.addAttribute("listaSedes", sedeRepository.findByEstado(1));
                    //attr.addFlashAttribute("msg","La sede del operador no puede quedar vacía");
                    /*if(operador.getIdsede()==null){
                        attr.addFlashAttribute("msg","La sede del operador no puede quedar vacía");
                    }*/
                    return "Administrador/Operador/agregarOperadores";
                }else{
                    if(personaRepository.obtenerDnis().contains(operador.getDni())){
                        model.addAttribute("listaSedes", sedeRepository.findByEstado(1));
                        //attr.addFlashAttribute("msg","El dni ingresado ya existe");
                        //Falta ver como hacer para pasarle ese mensaje de error al admin
                        model.addAttribute("msg","El DNI ingresado ya existe");
                        return "Administrador/Operador/agregarOperadores";
                    }else{
                        //**********************************************************
                        //Validar Correo
                        Persona Personabuscada = personaRepository.findByCorreo(operador.getCorreo());

                        if (Personabuscada != null){
                            model.addAttribute("listaSedes", sedeRepository.findByEstado(1));
                            attr.addFlashAttribute("err","No se puede crear un operador con un correo ya registrado");
                            return "Administrador/Operador/agregarOperadores";
                        }
                        //Validacion DNI
                        DatosAPI datosPersona = DniAPI.consulta(operador.getDni());
                        boolean errDNI = true;
                        if(datosPersona.getSuccess().equalsIgnoreCase("true")){
                            String nombresUpperCase = operador.getNombres();
                            String cadenaNormalize = Normalizer.normalize(nombresUpperCase,Normalizer.Form.NFD);
                            nombresUpperCase = cadenaNormalize.replaceAll("[^\\p{ASCII}]", "").toUpperCase();
                            String apellidosUpperCase = operador.getApellidos();
                            String cadenaNormalize2 = Normalizer.normalize(apellidosUpperCase,Normalizer.Form.NFD);
                            apellidosUpperCase = cadenaNormalize2.replaceAll("[^\\p{ASCII}]", "").toUpperCase();
                            String ApellidosUpperCase = nombresUpperCase + " " + apellidosUpperCase;
                            String ApellidosAPI = datosPersona.getApellido_paterno() + " " + datosPersona.getApellido_materno();
                            String NombresAPI = datosPersona.getNombres();
                            if(ApellidosAPI.contains(apellidosUpperCase) && NombresAPI.contains(nombresUpperCase)){
                                errDNI = false;
                            }
                        }

                        if(errDNI == true){
                            model.addAttribute("listaSedes", sedeRepository.findByEstado(1));
                            model.addAttribute("msg","Verifique el DNI ingresado");
                            return "Administrador/Operador/agregarOperadores";
                        }

                        //**********************************************************

                        //configuración en activo
                        operador.setEstado(1);
                        //Creación de rol
                        Rol rol = new Rol();
                        rol.setId(3);
                        rol.setEstado(1);//Opcional
                        rol.setNombre("Operador");//Opcional
                        //asignación de rol
                        operador.setIdrol(rol);
                        operador.setContrasenia("$2a$10$hE1dT9Lj6ppwAT0mg9mb8.xEsZPstCA5nNK2xqTHLNyvmVZU7zRYW");
                        personaRepository.save(operador);
                        attr.addFlashAttribute("msg2", "Se creo el operador de manera exitosa y se envió un correo de confirmacion ");
                        try {
                            sendEmailSuccessRegistration(operador.getCorreo(),operador.getIdsede().getNombre(),request);
                        } catch (MessagingException | UnsupportedEncodingException e) {
                        }
                        return "redirect:/admin/operadores";
                    }
                }
            }
        } catch (Exception e) {
            //attr.addFlashAttribute("msg", "Envió un ID inválido");
            System.out.println(e.getMessage());
            model.addAttribute("listaSedes", sedeRepository.findByEstado(1));
            return "Administrador/Operador/editarOperadores";//solo será valido cuando se encuentre en el formulario de editar
        }
    }

    @GetMapping(value = "/borrar")
    public String borrarOperador(@RequestParam("id") Optional<String> optid, RedirectAttributes attr){
        if(optid.isPresent()) {
            try{
                int id = Integer.parseInt(optid.get());

                if(personaRepository.existsById(id)){
                    Optional<Persona> aux = personaRepository.findById(id);
                    Persona operador = aux.get();
                    operador.setEstado(0);//Borrado lógico
                    personaRepository.save(operador);
                    attr.addFlashAttribute("msg","El operador ha sido borrado exitosamente");
                }else{
                    attr.addFlashAttribute("msg", "El operador con ID:"+id+" no se encuentra presente");
                }
                return "redirect:/admin/operadores/";

            }catch (Exception e){
                attr.addFlashAttribute("msg", "Envió un ID inválido");
                return "redirect:/admin/operadores/";
            }
        }else{
            attr.addFlashAttribute("msg", "Se envió el ID vacío");
            return "redirect:/admin/operadores/";
        }
    }


    private void sendEmailSuccessRegistration(String correo, String nombresede, HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        String path="http://"+request.getServerName()+":"+request.getServerPort()+"/login";

//        String longin = "http://localhost:8080/login";

        helper.setFrom("TeleMillonario@gmail.com","TeleMillonario");
        helper.setTo(correo);

        String subject = "¡Bienvenido a Telemillonario!";

        String content = "<p>Cordiales Saludos: </p>"
                + "<p>El administrador ha creado su cuenta para que gestione la sede "+nombresede +" </p>"
                + "<p>Ahora usted podrá realizar lo siguiente: </p>"
                + "<p> - Programar o modificar una funcion en la sede <p>"
                + "<p> - Gestionar las salas en donde se realizan las funciones<p>"
                + "<p> - Ver y exportar las estadisticas relacionadas a las funciones de la sede<p>"
                + "<p> - Gestionar las reseñas de la sede<p>"
                + "<p>Ingrese sesión mediante el siguiente <b><a href=" + path +">enlace</a></b><p>"
                + "<p> <b>Usuario:</b> "+correo+"<p>"
                + "<p> <b>Contraseña:</b> 123456<p>"
                + "<p> Por motivos de seguridad, se recomienda cambiar su contraseña brindada por defecto<p>";


        helper.setSubject(subject);
        helper.setText(content,true);

        mailSender.send(message);

    }

}
