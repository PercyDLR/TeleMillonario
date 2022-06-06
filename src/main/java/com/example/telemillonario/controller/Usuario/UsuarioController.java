package com.example.telemillonario.controller.Usuario;

import com.example.telemillonario.entity.*;
import com.example.telemillonario.repository.*;
import com.example.telemillonario.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    @Autowired
    CompraRepository compraRepository;

    @Autowired
    CarritoRepository carritoRepository;

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


    /*Reserva de tickets*/
    //Se le pasa a carrito donde aca se le agrega la tarjeta y se compra
    //Se establecera un limite de tiempo para la reserva?
    @PostMapping("/compra")
    public String compraBoletos(@RequestParam(value = "Obra") String idObraStr,
                                @RequestParam(value = "idSede") String idSedeStr,
                                @RequestParam(value = "cantBoletos") String cantBoletosStr,
                                @RequestParam(value = "fecha")String fechaStr,
                                @RequestParam(value = "hora")String horaStr,RedirectAttributes redirectAttributes,HttpSession session){

        //Se supone que la persona lo mapeamos a la variable "usuario"
        Persona persona = (Persona) session.getAttribute("usuario");

        int idObra = 0;

        int idSede = 0;
        boolean errorIdSede = false;

        int cantBoletos = 0;
        boolean errorCantBoletos = false;

        ///Date fechaHoraFuncion;
        ///SimpleDateFormat fechaHora = new SimpleDateFormat("dd/MM hh:mm");
        LocalDate fechaFuncion = null;
        LocalTime horaFuncion = null;
        boolean errorFechaHora = false;

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

                    if(obra.getEstado() == 0){
                        return "anErrorHasOcurred";
                    }

                    try{
                        idSede = Integer.parseInt(idSedeStr);
                        if(idSede <= 0){
                            errorIdSede = true;
                        }else{
                            Optional<Sede> sede1 = sedeRepository.findById(idSede);
                            if(sede1.isPresent()){
                                sede = sede1.get();

                                if(sede.getEstado() == 0){
                                    errorIdSede = true;
                                }

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
                    }catch (NumberFormatException m){
                        errorCantBoletos = true;
                    }

                    ///Validacion fecha y hora
                    try{
                        //fechaHoraFuncion = fechaHora.parse(fechaHoraStr);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM");
                        fechaFuncion = LocalDate.parse(fechaStr,formatter);

                        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("HH:mm");
                        horaFuncion = LocalTime.parse(horaStr,formatter1);
                    }catch (Exception a){
                        errorFechaHora = true;
                    }

                    if(errorIdSede || errorCantBoletos || errorFechaHora){
                        if(errorIdSede){
                            redirectAttributes.addFlashAttribute("mensajeErrorSede","La sede no es valido");
                        }
                        if(errorCantBoletos){
                            redirectAttributes.addFlashAttribute("mensajeErrorCantBoletos","El numero de boletos es incorrecto");
                        }
                        if(errorFechaHora){
                            redirectAttributes.addFlashAttribute("mensajeErrorFechaHora","Fecha - Hora no disponible");
                        }

                        return "redirect:/detallesObra?Obra="+obra.getId();
                    }

                }else{
                    return "anErrorHasOcurred";
                }
            }
        }catch (NumberFormatException j){
            return "anErrorHasOcurred";
        }

        LocalDate fechaFuncionDB = obra.getFecha();
        LocalTime horaFuncionDB = obra.getInicio();
        if(fechaFuncionDB != fechaFuncion || horaFuncionDB != horaFuncion){
            redirectAttributes.addFlashAttribute("mensajeErrorFechaHora","Fecha - Hora no disponible");
            return "redirect:/detallesObra?Obra="+obra.getId();
        }

        /*Si no ha ocurrido ningun error ya tengo la funcion - sede - boletos - fecha y hora*/
        //Toca validar de que exista una funcion a dicha hora en esa sede en especifico
        Funcion funcion = funcionRepository.encontrarFuncionHoraSede(obra.getId(),sede.getId(),fechaFuncion,horaFuncion);

        if(funcion == null){
            //Se decide por mostrarle un mensaje al usuario de que la funcion no existe
            //Tambien se pudo optar por redirigir a la vista de error
            redirectAttributes.addFlashAttribute("mensajeNoExisteFuncion","La funcion no se encuentra disponible");
            return "redirect:/detallesObra?Obra="+obra.getId();
        }else{
            int stockFuncion = funcion.getStockentradas();
            if(stockFuncion > 0 && cantBoletos <= stockFuncion){
                LocalDate fechaActual = LocalDate.now();
                LocalDate fechaNacimientoUsuario = persona.getNacimiento();
                Period period = Period.between(fechaNacimientoUsuario,fechaActual);
                int edad = period.getYears();

                if((funcion.getRestriccionedad() == 1 && edad>=18) || funcion.getRestriccionedad() == 0){
                    /*Calculo del monto total*/
                    double precioEntradaFuncion = funcion.getPrecioentrada();
                    double montoTotal = precioEntradaFuncion*cantBoletos;
                    int cantidadAsistentes = funcion.getCantidadasistentes();

                    //Guardado de la compra
                    Compra compra = new Compra();
                    compra.setEstado(1); //Se reserva , cuando ya se compra en el carrito esto se pone en 0
                    compra.setCantidad(cantBoletos);
                    compra.setMontoTotal(montoTotal);
                    compra.setFuncion(funcion);
                    compra.setPersona(persona);
                    carritoRepository.save(compra);

                    int stockRestanteFuncion = stockFuncion - cantBoletos;
                    cantidadAsistentes = cantidadAsistentes + cantBoletos;
                    if(stockRestanteFuncion == 0){
                        funcion.setStockentradas(0);
                        funcion.setEstado(0);
                        funcion.setCantidadasistentes(cantidadAsistentes);
                        funcionRepository.save(funcion);

                    }else{
                        funcion.setStockentradas(stockRestanteFuncion);
                        funcion.setCantidadasistentes(cantidadAsistentes);
                        funcionRepository.save(funcion);
                    }

                    redirectAttributes.addFlashAttribute("compraExitosa","Se ha realizado su compra correctamente. ");
                    return "redirect:/detallesObra?Obra="+obra.getId();

                }else{
                    redirectAttributes.addFlashAttribute("mensajeFaltaEdad","La funcion tiene restriccion de edad");
                    return "redirect:/detallesObra?Obra="+obra.getId();

                }
            }
            else{
                redirectAttributes.addFlashAttribute("mensajeNoHayStock","Ya no hay stock disponible");
                return "redirect:/detallesObra?Obra="+obra.getId();
            }
        }

    }

    float sedesxpagina=2;
    @GetMapping("/sedes")
    public String listaSedesUsuario(Model model,@RequestParam(value = "busqueda",defaultValue = "") String busqueda,
                                    @RequestParam(value = "pag",defaultValue = "0") String pag){
        //lista de sedes habilitadas
        int pagina;
        int estado=1;
        int cantSedes = sedeRepository.buscarSedesTotal();
        try{
            pagina = Integer.parseInt(pag);
        }catch(Exception e) {
            pagina=0;
        }
        if (busqueda.equals("")) { // verifica que no esté vacío

            List<Foto> listSedesConFoto= fotoRepository.listadoSedes(estado,(int)sedesxpagina*pagina, (int)sedesxpagina);
            model.addAttribute("listSedes",listSedesConFoto);
        }else{
            List<Foto> listSedesConFoto= fotoRepository.buscarSedePorNombre(busqueda, estado, (int)sedesxpagina*pagina, (int)sedesxpagina);
            model.addAttribute("listSedes",listSedesConFoto);
        }

        model.addAttribute("pagActual",pagina);
        model.addAttribute("pagTotal",(int) Math.ceil(cantSedes/sedesxpagina));

        model.addAttribute("busqueda", busqueda);

        return "usuario/sedes/sedes";
    }

    @PostMapping("/sedes/buscar")
    String busquedaSedesUsuario(@RequestParam(value="busqueda", defaultValue = "") String busqueda){

        return "redirect:/admin/actores?busqueda="+busqueda;
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

        List<Foto> listFuncSede=fotoRepository.buscarFotoFuncionesTotal(idsede);
        LinkedHashMap<Foto, List<Funciongenero>> fotoFuncionGenero = new LinkedHashMap<>();
        for (Foto func : listFuncSede) {

            fotoFuncionGenero.put(func, funcionGeneroRepository.buscarFuncionGenero(func.getFuncion().getId()));

        }

        model.addAttribute("funcionessede",fotoFuncionGenero);
        model.addAttribute("sede",sedeRepository.findById(idsede).get());
        return "usuario/sedes/sedeDetalles";
    }

    @GetMapping("/carrito")
    public String carritoUsuario(HttpSession session,Model model) {
        //Se supone que la persona lo mapeamos a la variable "usuario"
        Persona persona = (Persona) session.getAttribute("usuario");
        List<Compra> carritoUsuario = carritoRepository.obtenerCarritoUsuario(persona.getId());
        model.addAttribute("carrito",carritoUsuario);
        return "usuario/carritoCompras";
    }


}
