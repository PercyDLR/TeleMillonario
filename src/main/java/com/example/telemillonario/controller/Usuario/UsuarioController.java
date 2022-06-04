package com.example.telemillonario.controller.Usuario;

import com.example.telemillonario.entity.*;
import com.example.telemillonario.repository.*;
import com.example.telemillonario.service.FileService;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class UsuarioController {

    @Autowired
    PersonaRepository personaRepository;

    @Autowired
    FileService fileService;

    @Autowired
    FotoRepository fotoRepository;

    @Autowired
    FuncionRepository funcionRepository;

    @Autowired
    FuncionGeneroRepository funcionGeneroRepository;

    @Autowired
    SedeRepository sedeRepository;

    @GetMapping("")
    public String paginaPrincipal(Model model){
        List<Funcion> listaFunciones = funcionRepository.obtenerFuncionesDestacadasPaginaPrincipal();
        model.addAttribute("listaFunciones",listaFunciones);

        List<Funciongenero> funcionGenero = funcionGeneroRepository.findAll();
        model.addAttribute("funcionGenero",funcionGenero);
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
    public String guardarPerfilUsuario(@ModelAttribute("usuario") @Valid Persona usuario,
                                       @RequestParam("imagen") MultipartFile img,
                                       BindingResult bindingResult, Model model,
                                       RedirectAttributes a, HttpSession session) {
        //@Validated(Perfil.class)

        Persona usuarioSesion = (Persona) session.getAttribute("usuario");
        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult.getFieldError());
            return "usuario/editarPerfil";
        } else {

            if (usuario.getId().equals(usuarioSesion.getId())) {

                // Validación Fechas
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

                // Validar imágen
                switch(img.getContentType()){

                    case "image/jpeg":
                    case "image/png":
                    case "application/octet-stream":
                        break;
                    default:
                        usuario.setNombres(usuarioSesion.getNombres());
                        usuario.setApellidos(usuarioSesion.getApellidos());
                        usuario.setDni(usuarioSesion.getDni());
                        usuario.setCorreo(usuarioSesion.getCorreo());

                        model.addAttribute("err","Solo se deben de enviar imágenes");
                        model.addAttribute("imagenes",fotoRepository.findByIdpersonaOrderByNumero(usuarioSesion.getId()));
                        return "usuario/editarPerfil";
                }

                if (img.getOriginalFilename().length() != 0) {
                    try {
                        List<Foto> fotosEnDB = fotoRepository.findByIdpersonaOrderByNumero(usuario.getId());

                        // Borado de fotos
                        if (fotosEnDB.size() > 0){
                            for (Foto foto : fotosEnDB){
                                // Vacía las fotos en DB
                                fotoRepository.delete(foto);

                                // Borrado del fileService
                                String ruta = foto.getRuta();
                                String nombreFoto = ruta.substring(ruta.lastIndexOf('/') + 1);
                                //System.out.println("Nombre de la foto a eliminar: " + nombreFoto);
                                fileService.eliminarArchivo(nombreFoto);
                            }
                        }

                        Foto fotoPerfil = new Foto();

                        // Guardado en el Contenedor de archivos
                        MultipartFile newImg = fileService.formatearArchivo(img, "usuario");

                        if (fileService.subirArchivo(newImg)){
                            fotoPerfil.setRuta(fileService.obtenerUrl(newImg.getOriginalFilename()));
                        }

                        // Guardado de la Foto en DB
                        fotoPerfil.setNumero(0);
                        fotoPerfil.setIdpersona(usuario.getId());
                        fotoPerfil.setEstado(1);

                        fotoRepository.save(fotoPerfil);
                        session.setAttribute("fotoPerfil",fotoPerfil.getRuta());

                    } catch (Exception e){
                        e.getMessage();
                        System.out.println(e.getMessage());
                        a.addFlashAttribute("msg",-2);
                        return "redirect:/perfil";
                    }
                }

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

    /*Redireccion para ver los detalles de la obra para comprar tickets*/
    @GetMapping("/detallesObra")
    public String detallesObras(@RequestParam(value = "Obra") String funcionID,Model model){

        int idFuncion;
        try{
            idFuncion = Integer.parseInt(funcionID);
        }catch (NumberFormatException e){
            return "anErrorHasOcurred";
        }

        Optional<Funcion> funcion = funcionRepository.findById(idFuncion);

        if(funcion.isPresent()){
            List<Funciongenero> funcionGenero = funcionGeneroRepository.findAll();
            model.addAttribute("funcionGenero",funcionGenero);
            return "usuario/obras/obraDetalles";

        }else{
            return "anErrorHasOcurred";
        }

    }


    /*Compra de tickets*/
    @PostMapping("/compra")
    public String compraBoletos(@RequestParam(value = "Obra") String idObraStr, @RequestParam(value = "idSede") String idSedeStr,
                                @RequestParam(value = "cantBoletos") String cantBoletosStr,
                                @RequestParam(value = "fechaHora")String fechaHoraStr, RedirectAttributes redirectAttributes){

        int idObra;

        int idSede;
        boolean errorIdSede = false;

        int cantBoletos;
        boolean errorCantBoletos = false;


        Funcion obra = null;
        Sede sede = null;
        String fecha = null;
        String hora = null;

        try{
            idObra = Integer.parseInt(idObraStr);
            if(idObra <= 0){
                return "anErrorHasOcurred";
            }else{
                Optional<Funcion> funcion = funcionRepository.findById(idObra);
                if(funcion.isPresent()){
                    obra = funcion.get();

                    try{
                        idSede = Integer.parseInt(idSedeStr);
                        if(idSede <= 0){
                            errorIdSede = true;
                        }else{
                            Optional<Sede> sede1 = sedeRepository.findById(idSede);
                            if(sede1.isPresent()){
                                sede = sede1.get();
                            }else{
                                errorIdSede = true;
                            }
                        }
                    }catch (NumberFormatException e){
                        errorIdSede = true;
                    }

                    try{
                        cantBoletos = Integer.parseInt(cantBoletosStr);
                        if(cantBoletos <= 0){
                            errorCantBoletos = true;
                        }
                    }catch (NumberFormatException e){
                        errorCantBoletos = true;
                    }

                    /*
                        Validacion fecha y hora
                    */

                    if(errorIdSede || errorCantBoletos){
                        if(errorIdSede){
                            redirectAttributes.addFlashAttribute("mensajeErrorSede","El id de sede no es valido");
                        }
                        if(errorCantBoletos){
                            redirectAttributes.addFlashAttribute("mensajeErrorCantBoletos","El numero de boletos es incorrecto");
                        }

                        return "redirect:/detallesObra?Obra="+obra.getId();
                    }

                }else{
                    return "anErrorHasOcurred";
                }
            }
        }catch (NumberFormatException e){
            return "anErrorHasOcurred";
        }

        /*Si no ha ocurrido ningun error ya tengo la funcion - sede - boletos - fecha y hora*/

        //Toca validar de que exista una funcion a dicha hora en esa sede en especifico
        Funcion funcion = funcionRepository.encontrarFuncionHoraSede(obra.getId(),sede.getId(),fecha,hora);

        //Una vez validado toca realizar la logica para comprar


        return "detallesObra";
    }


    @GetMapping("/sedes")
    public String listaSedesUsuario(Model model){
        //lista de sedes habilitadas
        model.addAttribute("listSedes",fotoRepository.listSedesHabilitadas());
        return "usuario/sedes/sedes";
    }

    @GetMapping("/sedes/sede")
    public String infoSedeDetalles(@RequestParam(value = "idsede") String idsedeStr,Model model,RedirectAttributes attr){
        //lista de sedes habilitadas

        // Se verifica que el ID sea un número
        int idsede = 0;
        try{
            idsede = Integer.parseInt(idsedeStr);
        } catch (NumberFormatException e){
            attr.addFlashAttribute("err", "El ID ingresado no es inválido");
            return "redirect:/sedes";
        }
        List<Foto> fotosSede= fotoRepository.buscarFotosSede(idsede);
        model.addAttribute("imagenes",fotosSede);
        model.addAttribute("funcionessede",fotoRepository.buscarFotoFuncionesTotal(idsede));
        model.addAttribute("sede",sedeRepository.findById(idsede).get());
        return "usuario/sedes/sedeDetalles";
    }

}
