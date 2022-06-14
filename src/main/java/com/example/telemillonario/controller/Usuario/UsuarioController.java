package com.example.telemillonario.controller.Usuario;

import com.example.telemillonario.dto.QrDto;
import com.example.telemillonario.dto.ValidacionTarjetaDto;
import com.example.telemillonario.entity.*;
import com.example.telemillonario.repository.*;
import com.example.telemillonario.service.CoderService;
import com.example.telemillonario.service.DatosTarjeta;
import com.example.telemillonario.service.FileService;
import com.example.telemillonario.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class UsuarioController {

    @Autowired
    PersonaRepository personaRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    FileService fileService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    FotoRepository fotoRepository;

    @Autowired
    FuncionRepository funcionRepository;

    @Autowired
    ObraGeneroRepository obraGeneroRepository;

    @Autowired
    SedeRepository sedeRepository;

    @Autowired
    ObraRepository obraRepository;

    @Autowired
    CompraRepository compraRepository;

    @Autowired
    PagoRepository pagoRepository;

    @Autowired
    CoderService coderService;

    @GetMapping("")
    public String paginaPrincipal(Model model){
        List<Obra> listaObras = obraRepository.obtenerObrasDestacadasPaginaPrincipal();
        model.addAttribute("listaObras",listaObras);

        ArrayList<Foto> listaCaratulas = new ArrayList<>();
        for (Obra o : listaObras) {
            listaCaratulas.add(fotoRepository.caratulaDeObra(o.getId()));
        }
        model.addAttribute("listaCaratulas", listaCaratulas);

        List<Obragenero> obraGenero = obraGeneroRepository.findAll();
        model.addAttribute("obraGenero",obraGenero);

        return "vistaPrincipal";
    }

    @GetMapping("/historial")
    public String historialCompraPersona(Model model, HttpSession session) {
        Compra compraEnProceso = (Compra) session.getAttribute("compraEnProceso");
        compraEnProceso = null;
        session.setAttribute("compraEnProceso", compraEnProceso);

        Persona personita = (Persona) session.getAttribute("usuario");
        List<Compra> historialCompras = compraRepository.historialCompras(personita.getId());
        model.addAttribute("historialCompras", historialCompras);
        return "/usuario/historial";
    }

    @GetMapping("/borrarCompra")
    public String borrarCompraDeHistorialPersona(Model model,@RequestParam(value = "idCompra")String idCompra,HttpSession session){
        Persona usuario = (Persona) session.getAttribute("usuario");
        int identificadorCompra = 0;
        boolean errorIDCompra = false;
        Compra compra = null;

        if(idCompra == null || idCompra.equalsIgnoreCase("")){
            errorIDCompra = true;
        }else{
            try{
                identificadorCompra = Integer.parseInt(idCompra);
                Optional<Compra> compra1 = compraRepository.findById(identificadorCompra);
                if(compra1.isPresent()){
                    compra = compra1.get();
                }else{
                    errorIDCompra = true;
                }

            }catch (NumberFormatException e){
                errorIDCompra = true;
            }

        }

        if(compra.getPersona().getId() != usuario.getId()){
            errorIDCompra = true;
        }

        boolean estaATiempo = true;
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalTime fechaHoraFuncion = compra.getFuncion().getInicio();
        LocalTime fechaHoraActual = LocalTime.now();

        Duration duration = Duration.between(fechaHoraFuncion, fechaHoraActual);
        long minutes = duration.toMinutes();

        if (minutes > 15) {
            estaATiempo = false;
        }


        if(errorIDCompra == false){

            if(estaATiempo == true){

                Pago pago = pagoRepository.encontrarPagoPorIdCompra(compra.getId());
                pago.setEstado("0");
                pagoRepository.save(pago);


                compra.setEstado(0);
                compraRepository.save(compra);

                /*

                    Se le devuelve el dinero a la tarjeta al usuario y se invalida la QR asignada
                 */

                Optional<Funcion> funcion1 = funcionRepository.findById(compra.getFuncion().getId());
                Funcion funcion = funcion1.get();
                int cantidadAsistentes = funcion.getCantidadasistentes();
                int stockFuncion = funcion.getStockentradas();
                int stockRestanteFuncion = stockFuncion + compra.getCantidad();

                cantidadAsistentes = cantidadAsistentes - compra.getCantidad();
                funcion.setStockentradas(stockRestanteFuncion);
                funcion.setCantidadasistentes(cantidadAsistentes);
                funcionRepository.save(funcion);

                model.addAttribute("compraBorradaExitosamente","Se ha eliminado la compra exitosamente.La devolucion de su dinero se realizara en los proximos minutos");
                return "/usuario/historial";

            }else{
                model.addAttribute("errorBorrarCompra","Ya no se puede borrar su compra porque supero nuestro timepo limite de tolerancia.");
                return "/usuario/historial";
            }

        }else{
            model.addAttribute("errorBorrarCompra","Error al quere borrar la compra");
            return "/usuario/historial";
        }

    }



    @GetMapping("/perfil")
    public String verPerfilUsuario(@ModelAttribute("usuario") Persona usuario, Model model, HttpSession session) {
        usuario = (Persona) session.getAttribute("usuario");
        model.addAttribute("usuario", usuario);
        return "usuario/verPerfil";
    }

    @GetMapping("/perfil/editar")
    public String editarPerfilUsuario(@ModelAttribute("usuario") Persona usuario, Model model, HttpSession session) {
        usuario = (Persona) session.getAttribute("usuario");
        model.addAttribute("usuario", usuario);
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
                switch (img.getContentType()) {

                    case "image/jpeg":
                    case "image/png":
                    case "application/octet-stream":
                        break;
                    default:
                        usuario.setNombres(usuarioSesion.getNombres());
                        usuario.setApellidos(usuarioSesion.getApellidos());
                        usuario.setDni(usuarioSesion.getDni());
                        usuario.setCorreo(usuarioSesion.getCorreo());

                        model.addAttribute("err", "Solo se deben de enviar imágenes");
                        model.addAttribute("imagenes", fotoRepository.findByIdpersonaOrderByNumero(usuarioSesion.getId()));
                        return "usuario/editarPerfil";
                }

                if (img.getOriginalFilename().length() != 0) {
                    try {
                        List<Foto> fotosEnDB = fotoRepository.findByIdpersonaOrderByNumero(usuario.getId());

                        // Borado de fotos
                        if (fotosEnDB.size() > 0) {
                            for (Foto foto : fotosEnDB) {
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

                        if (fileService.subirArchivo(newImg)) {
                            fotoPerfil.setRuta(fileService.obtenerUrl(newImg.getOriginalFilename()));
                        }

                        // Guardado de la Foto en DB
                        fotoPerfil.setNumero(0);
                        fotoPerfil.setIdpersona(usuario);
                        fotoPerfil.setEstado(1);

                        fotoRepository.save(fotoPerfil);
                        session.setAttribute("fotoPerfil", fotoPerfil.getRuta());

                    } catch (Exception e) {
                        e.getMessage();
                        System.out.println(e.getMessage());
                        a.addFlashAttribute("msg", -2);
                        return "redirect:/perfil";
                    }
                }

                //Actualiza la DB
                personaRepository.save(usuarioSesion);

                //Actualiza la Sesión
                session.setAttribute("usuario", usuarioSesion);

                a.addFlashAttribute("msg", 0);
            } else { //No debe dejar guardar el usuario editado
                a.addFlashAttribute("msg", -1);
            }

            return "redirect:/perfil";
        }

    }

    /*Redireccion para ver los detalles de la obra para comprar tickets*/
    @GetMapping("/detallesObra")
    public String detallesObras(@RequestParam(value = "Obra") String funcionID, Model model, HttpSession session) {
        Compra compraEnProceso = (Compra) session.getAttribute("compraEnProceso");
        compraEnProceso = null;
        session.setAttribute("compraEnProceso", compraEnProceso);

        int idFuncion;
        try {
            idFuncion = Integer.parseInt(funcionID);
        } catch (NumberFormatException e) {
            return "anErrorHasOcurred";
        }

        Optional<Funcion> funcion = funcionRepository.findById(idFuncion);


        if(funcion.isPresent()){
            List<Obragenero> funcionGenero = obraGeneroRepository.findAll();
            model.addAttribute("funcionGenero",funcionGenero);

            return "usuario/obras/obraDetalles";

        } else {
            return "anErrorHasOcurred";
        }

    }


    /*Reserva de tickets*/
    //Se le pasa a carrito donde aca se le agrega la tarjeta y se compra
    //Se establecera un limite de tiempo para la reserva?
    @PostMapping("/reserva")
    public String reservaBoletos(@RequestParam(value = "Obra") String idObraStr,
                                 @RequestParam(value = "idSede") String idSedeStr,
                                 @RequestParam(value = "cantBoletos") String cantBoletosStr,
                                 @RequestParam(value = "fecha") String fechaStr,
                                 @RequestParam(value = "hora") String horaStr, RedirectAttributes redirectAttributes, HttpSession session) {


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

        //Funcion obra = null;
        Obra obra = null;
        Sede sede = null;
        String fecha = null;
        String hora = null;

        try {
            idObra = Integer.parseInt(idObraStr);
            if (idObra <= 0) {
                return "anErrorHasOcurred";
            } else {
                //Optional<Funcion> funcion = funcionRepository.findById(idObra);
                Optional<Obra> funcion = obraRepository.findById(idObra);
                if (funcion.isPresent()) {
                    obra = funcion.get();

                    if (obra.getEstado() == 0) {
                        return "anErrorHasOcurred";
                    }

                    try {
                        idSede = Integer.parseInt(idSedeStr);
                        if (idSede <= 0) {
                            errorIdSede = true;
                        } else {
                            Optional<Sede> sede1 = sedeRepository.findById(idSede);
                            if (sede1.isPresent()) {
                                sede = sede1.get();

                                if (sede.getEstado() == 0) {
                                    errorIdSede = true;
                                }

                            } else {
                                errorIdSede = true;
                            }
                        }
                    } catch (NumberFormatException e) {
                        errorIdSede = true;
                    }

                    try {
                        cantBoletos = Integer.parseInt(cantBoletosStr);
                        if (cantBoletos <= 0) {
                            errorCantBoletos = true;
                        }
                    } catch (NumberFormatException m) {
                        errorCantBoletos = true;
                    }


                    try {

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        fechaFuncion = LocalDate.parse(fechaStr, formatter);

                        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("hh:mm:ss");
                        horaFuncion = LocalTime.parse(horaStr, formatter1);
                    } catch (Exception a) {
                        errorFechaHora = true;
                    }

                    if (errorIdSede || errorCantBoletos || errorFechaHora) {
                        if (errorIdSede) {
                            redirectAttributes.addFlashAttribute("mensajeErrorSede", "La sede no es valido");
                        }
                        if (errorCantBoletos) {
                            redirectAttributes.addFlashAttribute("mensajeErrorCantBoletos", "El numero de boletos es incorrecto");
                        }
                        if (errorFechaHora) {
                            redirectAttributes.addFlashAttribute("mensajeErrorFechaHora", "Fecha - Hora no disponible");
                        }

                        return "redirect:/detallesObra?Obra=" + obra.getId();
                    }

                } else {
                    return "anErrorHasOcurred";
                }
            }
        } catch (NumberFormatException j) {
            return "anErrorHasOcurred";
        }

        /*Si no ha ocurrido ningun error ya tengo la funcion - sede - boletos - fecha y hora*/
        //Toca validar de que exista una funcion a dicha hora en esa sede en especifico
        Funcion funcion = funcionRepository.encontrarFuncionHoraSede(obra.getId(), sede.getId(), fechaFuncion, horaFuncion);

        if (funcion == null) {
            //Se decide por mostrarle un mensaje al usuario de que la funcion no existe
            //Tambien se pudo optar por redirigir a la vista de error
            redirectAttributes.addFlashAttribute("mensajeNoExisteFuncion", "La funcion no se encuentra disponible");
            return "redirect:/detallesObra?Obra=" + obra.getId();
        } else {
            int stockFuncion = funcion.getStockentradas();
            if (stockFuncion > 0 && cantBoletos <= stockFuncion) {
                LocalDate fechaActual = LocalDate.now();
                LocalDate fechaNacimientoUsuario = persona.getNacimiento();
                Period period = Period.between(fechaNacimientoUsuario, fechaActual);
                int edad = period.getYears();

                if ((obra.getRestriccionedad() == 1 && edad >= 18) || obra.getRestriccionedad() == 0) {
                    LinkedHashMap<String, Compra> carrito = (LinkedHashMap<String, Compra>) session.getAttribute("carritoDeComprasDeUsuario");
                    boolean existeCruce = false;
                    if (carrito.size() != 0) {
                        Collection<Compra> reservas = carrito.values();
                        ArrayList<String> crucesHorarios = new ArrayList<>();
                        for (Compra reserva : reservas) {
                            LocalTime inicioReserva = reserva.getFuncion().getInicio();
                            LocalTime finReserva = reserva.getFuncion().getFin();
                            LocalDate fechaReserva = reserva.getFuncion().getFecha();

                            if (fechaReserva == fechaFuncion) {
                                if ((inicioReserva.isAfter(reserva.getFuncion().getInicio()) && inicioReserva.isBefore(reserva.getFuncion().getFin())) || (finReserva.isAfter(reserva.getFuncion().getInicio()) && finReserva.isBefore(reserva.getFuncion().getFin()))) {
                                    existeCruce = true;
                                    String mensaje = "Existe un cruce de horario de funcion con la obra " + funcion.getIdobra().getNombre() + " " + ",con hora de inicio:" + funcion.getInicio() + ",con hora fin :" + funcion.getFin();
                                    crucesHorarios.add(mensaje);
                                }
                            }
                        }
                        if (existeCruce) {
                            redirectAttributes.addFlashAttribute("cruceHorarioFuncion", crucesHorarios);
                            return "redirect:/detallesObra?Obra=" + obra.getId();
                        }
                    }
                    /*Calculo del monto total*/
                    double precioEntradaFuncion = funcion.getPrecioentrada();
                    double montoTotal = precioEntradaFuncion * cantBoletos;

                    Compra reserva = new Compra();
                    reserva.setEstado(1);
                    reserva.setCantidad(cantBoletos);
                    reserva.setMontoTotal(montoTotal);
                    reserva.setFuncion(funcion);
                    reserva.setPersona(persona);

                    DateTimeFormatter fch = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss");
                    String fechaDeReservaCompra = fch.format(LocalDateTime.now());
                    LinkedHashMap<String, Compra> carritoDeComprasDeUsuario = new LinkedHashMap<>();
                    carritoDeComprasDeUsuario.put(fechaDeReservaCompra, reserva);
                    session.setAttribute("carritoDeComprasDeUsuario", carritoDeComprasDeUsuario);
                    redirectAttributes.addFlashAttribute("reservaExitosa", "Se ha realizado su reserva correctamente.Puede encontrarla " +
                            "dirigiendose a su carrito de compras.");
                    return "redirect:/detallesObra?Obra=" + obra.getId();


                        /*--------------------------------------------------------------------------------------------------------
                        int cantidadAsistentes = funcion.getCantidadasistentes();

                        //Guardado de la compra
                        Compra reserva = new Compra();
                        reserva.setEstado(1); //Se reserva , cuando ya se compra en el carrito esto se pone en 0
                        reserva.setCantidad(cantBoletos);
                        reserva.setMontoTotal(montoTotal);
                        reserva.setFuncion(funcion);
                        reserva.setPersona(persona);

                        //https://www.baeldung.com/java-linked-hashmap
                        //Se le mapea la fecha de reserva ya que va a tener un tiempo limitado para poder efectuar su compra sino
                        //se le borra de carrito y el stock de la funcion vuelve a como estaba
                        DateTimeFormatter fch = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss");
                        String fechaDeReservaCompra = fch.format(LocalDateTime.now());
                        LinkedHashMap<String,Compra> carritoDeComprasDeUsuario = new LinkedHashMap<>();
                        carritoDeComprasDeUsuario.put(fechaDeReservaCompra,reserva);
                        session.setAttribute("carritoDeComprasDeUsuario",carritoDeComprasDeUsuario);

                        int stockRestanteFuncion = stockFuncion - cantBoletos;
                        cantidadAsistentes = cantidadAsistentes + cantBoletos;//Cantidad de asistentes lo mapeo como si fuera la cant. de boletos vendidos
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

                        redirectAttributes.addFlashAttribute("reservaExitosa","Se ha realizado su reserva correctamente.Puede encontrarla " +
                                "dirigiendose a su carrito de compras ");
                        return "redirect:/detallesObra?Obra="+obra.getId();
                       --------------------------------------------------------------------------------------------------------*/
                } else {
                    redirectAttributes.addFlashAttribute("mensajeFaltaEdad", "La funcion tiene restriccion de edad");
                    return "redirect:/detallesObra?Obra=" + obra.getId();

                }
            } else {
                redirectAttributes.addFlashAttribute("mensajeNoHayStock", "Ya no hay stock disponible");
                return "redirect:/detallesObra?Obra=" + obra.getId();
            }
        }

    }

    //-----------------------------------------------------------------------------------
    @PostMapping("/compra")
    public String compraBoletos(@RequestParam(value = "Obra") String idObraStr,
                                @RequestParam(value = "idSede") String idSedeStr,
                                @RequestParam(value = "cantBoletos") String cantBoletosStr,
                                @RequestParam(value = "fecha") String fechaStr,
                                @RequestParam(value = "hora") String horaStr, RedirectAttributes redirectAttributes, HttpSession session) {

        Persona persona = (Persona) session.getAttribute("usuario");

        int idObra = 0;

        int idSede = 0;
        boolean errorIdSede = false;

        int cantBoletos = 0;
        boolean errorCantBoletos = false;

        LocalDate fechaFuncion = null;
        LocalTime horaFuncion = null;
        boolean errorFechaHora = false;

        Obra obra = null;
        Sede sede = null;
        String fecha = null;
        String hora = null;

        try {
            idObra = Integer.parseInt(idObraStr);
            if (idObra <= 0) {
                return "anErrorHasOcurred";
            } else {
                Optional<Obra> funcion = obraRepository.findById(idObra);
                if (funcion.isPresent()) {
                    obra = funcion.get();

                    if (obra.getEstado() == 0) {
                        return "anErrorHasOcurred";
                    }

                    try {
                        idSede = Integer.parseInt(idSedeStr);
                        if (idSede <= 0) {
                            errorIdSede = true;
                        } else {
                            Optional<Sede> sede1 = sedeRepository.findById(idSede);
                            if (sede1.isPresent()) {
                                sede = sede1.get();

                                if (sede.getEstado() == 0) {
                                    errorIdSede = true;
                                }

                            } else {
                                errorIdSede = true;
                            }
                        }
                    } catch (NumberFormatException e) {
                        errorIdSede = true;
                    }

                    try {
                        cantBoletos = Integer.parseInt(cantBoletosStr);
                        if (cantBoletos <= 0) {
                            errorCantBoletos = true;
                        }
                    } catch (NumberFormatException m) {
                        errorCantBoletos = true;
                    }

                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        fechaFuncion = LocalDate.parse(fechaStr, formatter);

                        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("hh:mm:ss");
                        horaFuncion = LocalTime.parse(horaStr, formatter1);
                    } catch (Exception a) {
                        errorFechaHora = true;
                    }

                    if (errorIdSede || errorCantBoletos || errorFechaHora) {
                        if (errorIdSede) {
                            redirectAttributes.addFlashAttribute("mensajeErrorSede", "La sede no es valido");
                        }
                        if (errorCantBoletos) {
                            redirectAttributes.addFlashAttribute("mensajeErrorCantBoletos", "El numero de boletos es incorrecto");
                        }
                        if (errorFechaHora) {
                            redirectAttributes.addFlashAttribute("mensajeErrorFechaHora", "Fecha - Hora no disponible");
                        }

                        return "redirect:/detallesObra?Obra=" + obra.getId();
                    }

                } else {
                    return "anErrorHasOcurred";
                }
            }
        } catch (NumberFormatException j) {
            return "anErrorHasOcurred";
        }

        Funcion funcion = funcionRepository.encontrarFuncionHoraSede(obra.getId(), sede.getId(), fechaFuncion, horaFuncion);

        if (funcion == null) {
            redirectAttributes.addFlashAttribute("mensajeNoExisteFuncion", "La funcion no se encuentra disponible");
            return "redirect:/detallesObra?Obra=" + obra.getId();
        } else {
            int stockFuncion = funcion.getStockentradas();
            if (stockFuncion > 0 && cantBoletos <= stockFuncion) {
                LocalDate fechaActual = LocalDate.now();
                LocalDate fechaNacimientoUsuario = persona.getNacimiento();
                Period period = Period.between(fechaNacimientoUsuario, fechaActual);
                int edad = period.getYears();
                if ((obra.getRestriccionedad() == 1 && edad >= 18) || obra.getRestriccionedad() == 0) {
                    /*-------------------------------------------------------------------------------------*/
                    LinkedHashMap<String, Compra> carrito = (LinkedHashMap<String, Compra>) session.getAttribute("carritoDeComprasDeUsuario");
                    boolean existeCruce = false;
                    if (carrito.size() != 0) {
                        Collection<Compra> reservas = carrito.values();
                        ArrayList<String> crucesHorarios = new ArrayList<>();
                        for (Compra reserva : reservas) {
                            LocalTime inicioReserva = reserva.getFuncion().getInicio();
                            LocalTime finReserva = reserva.getFuncion().getFin();
                            LocalDate fechaReserva = reserva.getFuncion().getFecha();

                            if (fechaReserva == fechaFuncion) {
                                if ((inicioReserva.isAfter(reserva.getFuncion().getInicio()) && inicioReserva.isBefore(reserva.getFuncion().getFin())) || (finReserva.isAfter(reserva.getFuncion().getInicio()) && finReserva.isBefore(reserva.getFuncion().getFin()))) {
                                    existeCruce = true;
                                    String mensaje = "Existe un cruce de horario de funcion con la obra " + funcion.getIdobra().getNombre() + " " + ",con hora de inicio:" + funcion.getInicio() + ",con hora fin :" + funcion.getFin();
                                    crucesHorarios.add(mensaje);
                                }
                            }
                        }
                        if (existeCruce) {
                            redirectAttributes.addFlashAttribute("cruceHorarioFuncion", crucesHorarios);
                            return "redirect:/detallesObra?Obra=" + obra.getId();
                        }
                    }
                    /*-------------------------------------------------------------------------------------*/
                    /*Calculo del monto total*/
                    double precioEntradaFuncion = funcion.getPrecioentrada();
                    double montoTotal = precioEntradaFuncion * cantBoletos;

                    Compra compraEnProceso = new Compra();
                    compraEnProceso.setEstado(1);//No es necesario
                    compraEnProceso.setCantidad(cantBoletos);
                    compraEnProceso.setMontoTotal(montoTotal);
                    compraEnProceso.setFuncion(funcion);
                    compraEnProceso.setPersona(persona);

                    session.setAttribute("compraEnProceso", compraEnProceso);
                    return "redirect:/compraprocess";

                } else {
                    redirectAttributes.addFlashAttribute("mensajeFaltaEdad", "La funcion tiene restriccion de edad");
                    return "redirect:/detallesObra?Obra=" + obra.getId();

                }
            } else {
                redirectAttributes.addFlashAttribute("mensajeNoHayStock", "Ya no hay stock disponible");
                return "redirect:/detallesObra?Obra=" + obra.getId();
            }
        }

    }

    @GetMapping("/compraprocess")
    public String procesoDeCompra(@ModelAttribute("datosTarjeta") DatosTarjeta datosTarjeta, HttpSession session) {
        Compra compraEnProceso = (Compra) session.getAttribute("compraEnProceso");
        if (compraEnProceso == null) {
            return "anErrorHasOcurred";
        } else {
            return "/usuario/pago";
        }

    }

    @PostMapping("/pago") //@Agustin
    public String pagoDeCompra(@ModelAttribute("datosTarjeta") @Valid DatosTarjeta datosTarjeta, BindingResult bindingResult, HttpSession session
            , RedirectAttributes redirectAttributes) {
        Compra compraEnProceso = (Compra) session.getAttribute("compraEnProceso");
        if (bindingResult.hasErrors()) {
            return "/usuario/pago";
        } else {
            RestTemplate restTemplate = new RestTemplate();
            String numero = coderService.codificar(datosTarjeta.getNumeroTarjeta());
            String nombre = coderService.codificar(datosTarjeta.getNombresTitular());
            String expiracion = coderService.codificar(datosTarjeta.getFechaVencimiento());
            String cvv = coderService.codificar(datosTarjeta.getCodigoSeguridad());
            String correo = coderService.codificar(datosTarjeta.getCorreo());
            String url = "http://20.90.180.72/validacion/verificar?numero="+numero+"&nombre="+nombre+
                    "&expiracion="+expiracion+"&cvv="+cvv+"&correo="+correo;
            ResponseEntity<ValidacionTarjetaDto> response = restTemplate.getForEntity(url,ValidacionTarjetaDto.class);
            boolean todoOK;
            if(response.getBody().getMsg().equalsIgnoreCase("Se ha verificado y agregado la tarjeta de manera correcta")||response.getBody().getMsg().equalsIgnoreCase("Se ha verificado la tarjeta de manera correcta")){
                todoOK=true;
            }else{
                todoOK=false;
            }
            if (todoOK) {
                Optional<Funcion> funcion1 = funcionRepository.findById(compraEnProceso.getFuncion().getId());
                Funcion funcion = funcion1.get();
                int cantidadAsistentes = funcion.getCantidadasistentes();
                int stockFuncion = funcion.getStockentradas();
                int stockRestanteFuncion = stockFuncion - compraEnProceso.getCantidad();

                cantidadAsistentes = cantidadAsistentes + compraEnProceso.getCantidad();
                if (stockRestanteFuncion == 0) {
                    funcion.setStockentradas(0);
                    funcion.setEstado(0);
                    funcion.setCantidadasistentes(cantidadAsistentes);
                    funcionRepository.save(funcion);
                } else {
                    funcion.setStockentradas(stockRestanteFuncion);
                    funcion.setCantidadasistentes(cantidadAsistentes);
                    funcionRepository.save(funcion);
                }

                compraRepository.save(compraEnProceso);
                //Codigo unico de operacion
                //https://www.baeldung.com/java-uuid
                UUID uuid = UUID.randomUUID();
                String numero_operacion = uuid.toString();
                /*
                Generación de la URL a setear en el codigo para que redirija al metodo ->Alonso
                   p.e:
                   url: http://localhost:8080/{nombre de controlador}/numero_operacion={numero de operacion}
                    tener en cuenta que el preludio de la url tiene que ser dinamico y que se tiene que mostrar
                    el resumen de compras
                 */
                String url_for_qr = "url a definir"+"/numero_operacion="+numero_operacion;//@Alonso
                String url_encoded = coderService.codificar(url_for_qr);
                RestTemplate restTemplate_for_qr = new RestTemplate();
                String qr_service = "http://20.90.180.72/validacion/qrcode?link="+url_encoded;
                ResponseEntity<QrDto> response_for_qr = restTemplate_for_qr.getForEntity(qr_service, QrDto.class);
                String qr_link = response_for_qr.getBody().getUrl();//->Se envia a la vista
                //Se llena la tabla de pago
                Pago pago = new Pago();
                pago.setEstado("1"); //estado->1
                //Aca va el set del idtarjeta   @Agustin
                if(datosTarjeta.getNombresTitular().equalsIgnoreCase("Visa")){
                    //visa
                    pago.setIdTarjeta(1);
                }else if(datosTarjeta.getNombresTitular().equalsIgnoreCase("Mastercard")){
                    //mastercard
                    pago.setIdTarjeta(2);
                }else if(datosTarjeta.getNumeroTarjeta().equalsIgnoreCase("Diners Club")){
                    //diners club
                    pago.setIdTarjeta(3);
                }else{
                    System.out.println("Ha sucedido algo malo");
                }
                //Aca va el set del numerotarjeta   @Agustin
                pago.setNumeroTarjeta(datosTarjeta.getNumeroTarjeta());
                //Aca va el set de la fechade pago  @Agustin
                pago.setFechaPago(LocalDate.now());
                pago.setIdCompra(compraEnProceso);
                //Aca va el set del codigo QR   @Agustin
                pago.setQr(qr_link);
                //Aca va el set del codigo de operacion @Agustin
                pago.setCodigo(numero_operacion);
                pagoRepository.save(pago);
                String content = "<p>Cordiales Saludos: </p>"
                        + "<p>Se ha efectuado correctamente la siguiente compra:</p>"
                        + "<p>- Funcion de la obra:" + compraEnProceso.getFuncion().getIdobra().getNombre() + " " + "<p>"
                        + "<p>- Sede:" + compraEnProceso.getFuncion().getIdsala().getIdsede().getNombre() + " " + "<p>"
                        + "<p>- Sala:" + compraEnProceso.getFuncion().getIdsala().getNumero() + " " + "<p>"
                        + "<p>- Fecha:" + compraEnProceso.getFuncion().getFecha() + " " + "<p>"
                        + "<p>- Hora de inicio:" + compraEnProceso.getFuncion().getInicio() + " " + "<p>"
                        + "<p>- Hora fin:" + compraEnProceso.getFuncion().getFin() + " " + "<p>"
                        + "<p>- Cantidad de boletos: "+ compraEnProceso.getCantidad() + " " + "<p>";

                        /*
                        Falta poner el detalle del pago con el QR -> Eso es mapeado con la ruta al servicio que quiera acceder @Alonso
                         */

                try {
                    sendInfoCompraCorreo(compraEnProceso.getPersona().getCorreo(),content);
                } catch (MessagingException | UnsupportedEncodingException e) {
                    return "redirect:/anErrorHasOcurred";
                }

                compraEnProceso = null;
                session.setAttribute("compraEnProceso", compraEnProceso);

                redirectAttributes.addFlashAttribute("compraExitosa", "Se ha realizado su compra correctamente.");
                return "redirect:/historial";

            } else {
                redirectAttributes.addFlashAttribute("msg",response.getBody().getMsg());
                return "/usuario/pago";
            }
        }

    }

    //-----------------------------------------------------------------------------------

    @GetMapping("/carrito")
    public String carritoUsuario(HttpSession session) {
        LinkedHashMap<String, Compra> carrito = (LinkedHashMap<String, Compra>) session.getAttribute("carritoDeComprasDeUsuario");
        if (carrito.size() == 0) {
            return "usuario/carritoCompras";
        } else {
            ArrayList<Compra> reservasBorrarCarrito = new ArrayList<>();
            Set<String> llaveDeHoras = carrito.keySet();
            for (String fechaReserva : llaveDeHoras) {
                boolean estaATiempo = true;
                /*Hago la validacion de si ya paso los 15min despues de su reserva.Dicha validacion lo guardo en una variable*/
                //https://www.delftstack.com/es/howto/java/java-string-to-timestamp/#:~:text=Timestamp%20.-,Usa%20TimeStamp.,en%20una%20marca%20de%20tiempo.
                DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                LocalDateTime fechaHoraReserva = LocalDateTime.from(formato.parse(fechaReserva));
                LocalDateTime fechaHoraActual = LocalDateTime.now();

                Duration duration = Duration.between(fechaHoraReserva, fechaHoraActual);
                long minutes = duration.toMinutes();

                if (minutes > 15) {
                    estaATiempo = false;
                }

                if (estaATiempo == false) {
                    /*busco por la fecha la compra en carrito,lo borro y lo pongo en la nueva lista*/
                    Compra compra = carrito.get(fechaReserva);
                    reservasBorrarCarrito.add(compra);
                    carrito.remove(fechaReserva);
                }
            }
            session.setAttribute("carritoDeComprasDeUsuario", carrito);
            if (reservasBorrarCarrito.size() == 0) {
                return "usuario/carritoCompras";
            } else {
                //Mensajes de las reservas que se han borrado de su carrito , se le envia por correo.
                String content = "<p>Cordiales Saludos: </p>"
                        + "<p>Debido al tiempo prudente dado para que terminara su reserva , se procedio a eliminar de su carrito las siguientes reservas:</p>";

                for (Compra compra : reservasBorrarCarrito) {
                    String lineaHTML = "<p>- Funcion de la obra:" + compra.getFuncion().getIdobra().getNombre() + " " + ",con hora de inicio:" + compra.getFuncion().getInicio() + " " + " y con una cantidad de boletos de:" + compra.getCantidad() + "</p>";
                    content = content + lineaHTML;
                }

                Persona persona = (Persona) session.getAttribute("usuario");
                String correo = persona.getCorreo();

                try {
                    sendReservasBorradas(correo, content);
                } catch (MessagingException | UnsupportedEncodingException e) {
                    return "redirect:/anErrorHasOcurred";
                }

                return "usuario/carritoCompras";
            }
        }

    }

    @PostMapping("/compraReservasCarrito") //@Agustin
    private String comprarReservasDelCarrito(Compra reserva,DatosTarjeta datosTarjeta,RedirectAttributes redirectAttributes, HttpSession session){
        //Los cambios que realize en la cantidad de boletos , se tiene que mapear en el html,aca simplemente cuando le da a comprar
        //Por mientras se establece que una reserva a la vez se compra , de ahi se le implementa las reservas que quiera.
        /*-----------------------------------------------------------------------------------------------------------------------------------------*/
        //Se supone que la persona lo mapeamos a la variable "usuario"
        Persona persona = (Persona) session.getAttribute("usuario");
        LinkedHashMap<String, Compra> carrito = (LinkedHashMap<String, Compra>) session.getAttribute("carritoDeComprasDeUsuario");

        /*LinkedHashMap<String, Compra> carrito = (LinkedHashMap<String, Compra>) session.getAttribute("carritoDeComprasDeUsuario");
        Collection<Compra> reservas = carrito.values();
        boolean errorCantBoletos = false;
        double montoTotal = 0;*/
        double montoTotal = 0;

        //Por mientras se mapea como que si hay una reserva que le ha cambiado la cant de boletos <0 indepdneimiente si lo compra o no , sale error.
        /*for (Compra reserva : reservas) {*/
            if(reserva.getCantidad() <= 0) {
                redirectAttributes.addFlashAttribute("mensajeErrorCantBoletos", "El numero de boletos es incorrecto");
                return "redirect:/carrito";
            }
            Optional<Funcion> funcion1 = funcionRepository.findById(reserva.getFuncion().getId());
            Funcion funcion = funcion1.get();
            int stockFuncion = funcion.getStockentradas();
            if(stockFuncion > 0 && reserva.getCantidad() <= stockFuncion) {
                double precioEntradFuncion = funcion.getPrecioentrada();
                montoTotal = montoTotal + precioEntradFuncion*reserva.getCantidad();

                int cantidadAsistentes = funcion.getCantidadasistentes();
                int stockRestanteFuncion = stockFuncion - reserva.getCantidad();
                cantidadAsistentes = cantidadAsistentes + reserva.getCantidad();
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


                /*

               Validar datos tarjeta    @Agustin

                 */

                /*

                    QR

                 */


                Pago pago = new Pago();
                pago.setEstado("1"); //Que estado se le va a poner?
                //Aca va el set del idtarjeta   @Agustin
                //Aca va el set del numerotarjeta   @Agustin
                //Aca va el set de la fechade pago  @Agustin
                pago.setIdCompra(reserva);
                //Aca va el set del codigo QR   @Agustin
                //Aca va el set del codigo de operacion @Agustin
                pagoRepository.save(pago);

                compraRepository.save(reserva);

                Set<String> llaveDeHoras = carrito.keySet();
                String llaveReserva = null;
                for (String fechaReserva : llaveDeHoras) {
                   Compra compra = carrito.get(fechaReserva);
                    if(compra.getId() == reserva.getId()){
                        llaveReserva = fechaReserva;
                    }

                }
                carrito.remove(llaveReserva);
                session.setAttribute("carritoDeComprasDeUsuario", carrito);
                redirectAttributes.addFlashAttribute("compraExitosa", "Se ha realizado su compra correctamente.");
                return "redirect:/carrito";

            }else {
                redirectAttributes.addFlashAttribute("mensajeNoHayStock", "Ya no hay stock disponible para la obra : "+reserva.getFuncion().getIdobra().getNombre());
                return "redirect:/carrito";
            }

       /* }*/

        /*-----------------------------------------------------------------------------------------------------------------------------------------*/

    }


    private void sendReservasBorradas(String correo,String content) throws MessagingException,UnsupportedEncodingException{
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("TeleMillonario@gmail.com","TeleMillonario");
        helper.setTo(correo);

        String subject = "¡Aviso Parroquial!";

        helper.setSubject(subject);
        helper.setText(content,true);

        mailSender.send(message);
    }

    private void sendInfoCompraCorreo(String correo,String content) throws  MessagingException,UnsupportedEncodingException{
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("TeleMillonario@gmail.com","TeleMillonario");
        helper.setTo(correo);

        String subject = "Compra Realizada";

        helper.setSubject(subject);
        helper.setText(content,true);

        mailSender.send(message);
    }



}
