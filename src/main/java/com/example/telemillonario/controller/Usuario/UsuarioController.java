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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class UsuarioController {

    @Autowired
    TarjetaRepository tarjetaRepository;

    @Autowired
    PagoRepository pagoRepository;

    @Autowired
    FuncionElencoRepository funcionElencoRepository;

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
    CoderService coderService;

    @Autowired
    CalificacionesRepository calificacionesRepository;

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

        /*if(compra.getPersona().getId() != usuario.getId()){
            errorIDCompra = true;
        }*/

        boolean estaATiempo = true;
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalTime fechaHoraFuncion = compra.getFuncion().getInicio();
        LocalTime fechaHoraActual = LocalTime.now();

        Duration duration = Duration.between(fechaHoraFuncion, fechaHoraActual);
        long minutes = duration.toMinutes();
        System.out.println(minutes);
        if (minutes < 15) {
            estaATiempo = false;
        }

        if(errorIDCompra == false){
            System.out.println("entr eprimer if");
            if(estaATiempo == true){
                System.out.println("entr segundoi if");

                /*Pago pago = pagoRepository.encontrarPagoPorIdCompra(compra.getId());
                pago.setEstado("0");
                pagoRepository.save(pago);*/

                System.out.println("seteo cancelado");
                compra.setEstado("Cancelado");
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
                return "redirect:/historialPrueba";
                //return "usuario/carrito/historialComprasUsuario";

            }else{
                model.addAttribute("errorBorrarCompra","Ya no se puede borrar su compra porque supero nuestro timepo limite de tolerancia.");
                return "usuario/carrito/historialComprasUsuario";
            }

        }else{
            model.addAttribute("errorBorrarCompra","Error al quere borrar la compra");
            return "usuario/carrito/historialComprasUsuario";
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

    //@PostMapping("/reserva")
    @GetMapping("/reserva")
    public String reservaBoletos(@RequestParam(value = "idFuncion",required = false) String idFuncionStr,
                                 @RequestParam(value = "cantBoletos",required = false) String cantBoletosStr,RedirectAttributes redirectAttributes, HttpSession session) {
        Persona persona = (Persona) session.getAttribute("usuario");
        int idFuncion = 0;
        int cantBoletos = 0;
        Funcion funcion = null;

        if(idFuncionStr == null || cantBoletosStr == null){
            return "redirect:/anErrorHasOcurred";
        }

        try {
            idFuncion = Integer.parseInt(idFuncionStr);
            if (idFuncion <= 0) {
                return "redirect:/anErrorHasOcurred";
            } else {
                Optional<Funcion> funcion1 = funcionRepository.findById(idFuncion);
                if (funcion1.isPresent()) {
                    funcion = funcion1.get();
                    if (funcion.getEstado() == 0) {
                        return "redirect:/anErrorHasOcurred";
                    }
                } else {
                    return "redirect:/anErrorHasOcurred";
                }
            }
        } catch (NumberFormatException j) {
            return "redirect:/anErrorHasOcurred";
        }

        try {
            cantBoletos = Integer.parseInt(cantBoletosStr);
            if (cantBoletos <= 0) {
                redirectAttributes.addFlashAttribute("mensajeError", "El numero de boletos es incorrecto");
                return "redirect:/cartelera/DetallesObra?id=" + funcion.getIdobra().getId();
            }
        } catch (NumberFormatException m) {
            redirectAttributes.addFlashAttribute("mensajeError", "El numero de boletos es incorrecto");
            return "redirect:/cartelera/DetallesObra?id=" + funcion.getIdobra().getId();
        }

        Obra obra = funcion.getIdobra();

        int stockFuncion = funcion.getStockentradas();
        if (stockFuncion > 0 && cantBoletos <= stockFuncion) {
            LocalDate fechaActual = LocalDate.now();
            LocalDate fechaNacimientoUsuario = persona.getNacimiento();
            long edad = ChronoUnit.YEARS.between(fechaNacimientoUsuario,fechaActual);
            boolean mayorEdad;
            if(edad > 16){
                mayorEdad = true;
            }else if(edad == 16){
                long cumpleAntesFuncion = fechaActual.until(fechaNacimientoUsuario.plusYears(ChronoUnit.YEARS.between(fechaNacimientoUsuario,fechaActual)),ChronoUnit.DAYS);
                if(cumpleAntesFuncion <= 0){
                    mayorEdad = true;
                }else{
                    mayorEdad = false;
                }
            }else{
                mayorEdad = false;
            }
            if ((obra.getRestriccionedad() == 1 && mayorEdad == true) || obra.getRestriccionedad() == 0) {
                //LinkedHashMap<String, Compra> carrito = (LinkedHashMap<String, Compra>) session.getAttribute("carritoDeComprasDeUsuario");
                LinkedHashMap<Map<Integer,String>,Compra> carrito = (LinkedHashMap<Map<Integer,String>, Compra>) session.getAttribute("carritoDeComprasDeUsuario");
                if(carrito == null){
                    double precioEntradaFuncion = funcion.getPrecioentrada();
                    double montoTotal = precioEntradaFuncion * cantBoletos;

                    Compra reserva = new Compra();
                    reserva.setEstado("Reservado");
                    reserva.setCantidad(cantBoletos);
                    reserva.setMontoTotal(montoTotal);
                    reserva.setFuncion(funcion);
                    reserva.setPersona(persona);

                    LocalDate fecha = LocalDate.now();
                    LocalTime hora1 = LocalTime.now();
                    String horita = String.valueOf(hora1);
                    LocalTime hora = LocalTime.parse(horita);
                    String fechaDeReservaCompra = fecha + " " + hora;

                    Map<Integer,String> llavesCarrito = new HashMap<>();
                    llavesCarrito.put(1,fechaDeReservaCompra);
                    LinkedHashMap<Map<Integer,String>,Compra> carritoDeComprasDeUsuario = new LinkedHashMap<>();
                    carritoDeComprasDeUsuario.put(llavesCarrito,reserva);
                    //LinkedHashMap<String, Compra> carritoDeComprasDeUsuario = new LinkedHashMap<>();
                    //carritoDeComprasDeUsuario.put(fechaDeReservaCompra, reserva);
                    session.setAttribute("carritoDeComprasDeUsuario", carritoDeComprasDeUsuario);
                    redirectAttributes.addFlashAttribute("reservaExitosa", "Se ha realizado su reserva correctamente.Puede encontrarla " +
                            "dirigiendose a su carrito de compras.");
                    return "redirect:/cartelera/DetallesObra?id=" + funcion.getIdobra().getId();

                }
                boolean existeCruce = false;
                Collection<Compra> reservas = carrito.values();
                ArrayList<String> crucesHorarios = new ArrayList<>();
                for (Compra reserva : reservas) {
                    if(!reserva.getEstado().equals("Borrado")){
                        LocalTime inicioReserva = reserva.getFuncion().getInicio();
                        LocalTime finReserva = reserva.getFuncion().getFin();
                        LocalDate fechaReserva = reserva.getFuncion().getFecha();
                        if (fechaReserva.equals(funcion.getFecha())){
                            boolean validacion1 = inicioReserva.isAfter(funcion.getInicio()) || inicioReserva.equals(funcion.getInicio());
                            boolean validacion2 = inicioReserva.isBefore(funcion.getFin()) || inicioReserva.equals(funcion.getFin());

                            boolean validacion3 = finReserva.isAfter(funcion.getInicio()) || finReserva.equals(funcion.getInicio());
                            boolean validacion4 = finReserva.isBefore(funcion.getFin()) || finReserva.equals(funcion.getFin());

                            if ((validacion1 && validacion2) || (validacion3 && validacion4)) {
                                existeCruce = true;
                                String mensaje = "Existe un cruce de horario de funcion con la obra " + reserva.getFuncion().getIdobra().getNombre() + " " + ",con hora de inicio:" + reserva.getFuncion().getInicio() + ",con hora fin :" + reserva.getFuncion().getFin();
                                crucesHorarios.add(mensaje);
                            }
                        }
                    }
                }
                if (existeCruce) {
                    redirectAttributes.addFlashAttribute("cruceHorarioFuncion", crucesHorarios);
                    return "redirect:/cartelera/DetallesObra?id=" + funcion.getIdobra().getId();
                }else{
                    double precioEntradaFuncion = funcion.getPrecioentrada();
                    double montoTotal = precioEntradaFuncion * cantBoletos;

                    Compra reserva = new Compra();
                    reserva.setEstado("Reservado");
                    reserva.setCantidad(cantBoletos);
                    reserva.setMontoTotal(montoTotal);
                    reserva.setFuncion(funcion);
                    reserva.setPersona(persona);

                    LocalDate fecha = LocalDate.now();
                    LocalTime hora1 = LocalTime.now();
                    String horita = String.valueOf(hora1);
                    LocalTime hora = LocalTime.parse(horita);

                    String fechaDeReservaCompra = fecha + " " + hora;
                    Map<Integer,String> llavesCarrito = new HashMap<>();
                    llavesCarrito.put(carrito.size()+1,fechaDeReservaCompra);
                    System.out.println(llavesCarrito);
                    carrito.put(llavesCarrito,reserva);
                    //carrito.put(fechaDeReservaCompra,reserva);
                    session.setAttribute("carritoDeComprasDeUsuario", carrito);
                    redirectAttributes.addFlashAttribute("reservaExitosa", "Se ha realizado su reserva correctamente.Puede encontrarla " +
                            "dirigiendose a su carrito de compras.");
                    return "redirect:/cartelera/DetallesObra?id=" + funcion.getIdobra().getId();
                }
            } else {
                redirectAttributes.addFlashAttribute("mensajeError", "La funcion tiene restriccion de edad");
                return "redirect:/cartelera/DetallesObra?id=" + funcion.getIdobra().getId();

            }
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Ya no hay stock disponible");
            return "redirect:/cartelera/DetallesObra?id=" + funcion.getIdobra().getId();
        }
    }



    //@PostMapping("/compra")
    @GetMapping("/compra")
    public String compraBoletos(@ModelAttribute("datosTarjeta") DatosTarjeta datosTarjeta,@RequestParam(value = "idFuncion",required = false) String idFuncionStr,
                                @RequestParam(value = "cantBoletos",required = false) String cantBoletosStr,RedirectAttributes redirectAttributes, HttpSession session,
                                Model model) {

        Persona persona = (Persona) session.getAttribute("usuario");
        int idFuncion = 0;
        int cantBoletos = 0;
        Funcion funcion = null;
        System.out.println(idFuncionStr);
        System.out.println(cantBoletosStr);
        if(idFuncionStr == null || cantBoletosStr == null){
            return "redirect:/anErrorHasOcurred";
        }

        try {
            idFuncion = Integer.parseInt(idFuncionStr);
            if (idFuncion <= 0) {
                return "redirect:/anErrorHasOcurred";
            } else {
                Optional<Funcion> funcion1 = funcionRepository.findById(idFuncion);
                if (funcion1.isPresent()) {
                    funcion = funcion1.get();
                    if (funcion.getEstado() == 0) {
                        return "redirect:/anErrorHasOcurred";
                    }
                } else {
                    return "redirect:/anErrorHasOcurred";
                }
            }
        } catch (NumberFormatException j) {
            return "redirect:/anErrorHasOcurred";
        }
        try {
            cantBoletos = Integer.parseInt(cantBoletosStr);
            if (cantBoletos <= 0) {
                redirectAttributes.addFlashAttribute("mensajeError", "El numero de boletos es incorrecto");
                return "redirect:/cartelera/DetallesObra?id=" + funcion.getIdobra().getId();
            }
        } catch (NumberFormatException m) {
            redirectAttributes.addFlashAttribute("mensajeError", "El numero de boletos es incorrecto");
            return "redirect:/cartelera/DetallesObra?id=" + funcion.getIdobra().getId();
        }

        Obra obra = funcion.getIdobra();
        int stockFuncion = funcion.getStockentradas();
        if (stockFuncion > 0 && cantBoletos <= stockFuncion) {
            LocalDate fechaActual = LocalDate.now();
            LocalDate fechaNacimientoUsuario = persona.getNacimiento();
            long edad = ChronoUnit.YEARS.between(fechaNacimientoUsuario,fechaActual);
            boolean mayorEdad;
            if(edad > 16){
                mayorEdad = true;
            }else if(edad == 16){
                long cumpleAntesFuncion = fechaActual.until(fechaNacimientoUsuario.plusYears(ChronoUnit.YEARS.between(fechaNacimientoUsuario,fechaActual)),ChronoUnit.DAYS);
                if(cumpleAntesFuncion <= 0){
                    mayorEdad = true;
                }else{
                    mayorEdad = false;
                }
            }else{
                mayorEdad = false;
            }
            if ((obra.getRestriccionedad() == 1 && mayorEdad == true) || obra.getRestriccionedad() == 0) {
                //LinkedHashMap<String, Compra> carrito = (LinkedHashMap<String, Compra>) session.getAttribute("carritoDeComprasDeUsuario");
                LinkedHashMap<Map<Integer,String>,Compra> carrito = (LinkedHashMap<Map<Integer,String>, Compra>) session.getAttribute("carritoDeComprasDeUsuario");
                if(carrito == null){
                    double precioEntradaFuncion = funcion.getPrecioentrada();
                    double montoTotal = precioEntradaFuncion * cantBoletos;

                    Compra compraEnProceso = new Compra();
                    compraEnProceso.setCantidad(cantBoletos);
                    compraEnProceso.setMontoTotal(montoTotal);
                    compraEnProceso.setFuncion(funcion);
                    compraEnProceso.setPersona(persona);
                    model.addAttribute("compraEnProceso",compraEnProceso);

                    List<Foto> listaFotos = fotoRepository.unaFotoPorObra();
                    model.addAttribute("foto",listaFotos);
                    List<Obragenero> listaGeneros = obraGeneroRepository.findAll();
                    model.addAttribute("listaGeneros",listaGeneros);

                    return "usuario/pagoUsuario";
                    //session.setAttribute("compraEnProceso",compraEnProceso);
                    //return "redirect:/compraprocess";

                }
                boolean existeCruce = false;
                Collection<Compra> reservas = carrito.values();
                ArrayList<String> crucesHorarios = new ArrayList<>();
                for (Compra reserva : reservas) {
                    if(!reserva.getEstado().equals("Borrado")) {
                        LocalTime inicioReserva = reserva.getFuncion().getInicio();
                        LocalTime finReserva = reserva.getFuncion().getFin();
                        LocalDate fechaReserva = reserva.getFuncion().getFecha();
                        if (fechaReserva.equals(funcion.getFecha())){
                            boolean validacion1 = inicioReserva.isAfter(funcion.getInicio()) || inicioReserva.equals(funcion.getInicio());
                            boolean validacion2 = inicioReserva.isBefore(funcion.getFin()) || inicioReserva.equals(funcion.getFin());

                            boolean validacion3 = finReserva.isAfter(funcion.getInicio()) || finReserva.equals(funcion.getInicio());
                            boolean validacion4 = finReserva.isBefore(funcion.getFin()) || finReserva.equals(funcion.getFin());

                            if ((validacion1 && validacion2) || (validacion3 && validacion4)) {
                                existeCruce = true;
                                String mensaje = "Existe un cruce de horario de funcion con la obra " + reserva.getFuncion().getIdobra().getNombre() + " " + ",con hora de inicio:" + reserva.getFuncion().getInicio() + ",con hora fin :" + reserva.getFuncion().getFin();
                                crucesHorarios.add(mensaje);
                            }
                        }
                    }
                }
                if (existeCruce) {
                    redirectAttributes.addFlashAttribute("cruceHorarioFuncion", crucesHorarios);
                    return "redirect:/cartelera/DetallesObra?id=" + funcion.getIdobra().getId();
                }else{
                    double precioEntradaFuncion = funcion.getPrecioentrada();
                    double montoTotal = precioEntradaFuncion * cantBoletos;

                    Compra compraEnProceso = new Compra();
                    compraEnProceso.setCantidad(cantBoletos);
                    compraEnProceso.setMontoTotal(montoTotal);
                    compraEnProceso.setFuncion(funcion);
                    compraEnProceso.setPersona(persona);
                    model.addAttribute("compraEnProceso",compraEnProceso);


                    List<Foto> listaFotos = fotoRepository.unaFotoPorObra();
                    model.addAttribute("foto",listaFotos);
                    List<Obragenero> listaGeneros = obraGeneroRepository.findAll();
                    model.addAttribute("listaGeneros",listaGeneros);
                    return "usuario/pagoUsuario";

                    //session.setAttribute("compraEnProceso",compraEnProceso);
                    //return "redirect:/compraprocess";

                }
            } else {
                redirectAttributes.addFlashAttribute("mensajeError", "La funcion tiene restriccion de edad");
                return "redirect:/cartelera/DetallesObra?id=" + funcion.getIdobra().getId();

            }
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Ya no hay stock disponible");
            return "redirect:/cartelera/DetallesObra?id=" + funcion.getIdobra().getId();
        }

    }

    @PostMapping("/pago")
    public String pagoDeCompra(@ModelAttribute("datosTarjeta") @Valid DatosTarjeta datosTarjeta, BindingResult bindingResult,@RequestParam(value = "idFuncion",required = false) String idFuncionStr,
                               @RequestParam(value = "cantBoletos",required = false) String cantBoletosStr, HttpSession session,
                               RedirectAttributes redirectAttributes,Model model) {

        Persona persona = (Persona) session.getAttribute("usuario");
        int idFuncion = 0;
        int cantBoletos = 0;
        Funcion funcion = null;

        if(idFuncionStr == null || cantBoletosStr == null){
            return "redirect:/anErrorHasOcurred";
        }

        try {
            idFuncion = Integer.parseInt(idFuncionStr);
            if (idFuncion <= 0) {
                return "redirect:/anErrorHasOcurred";
            } else {

                /*try{
                    Random rand = new Random();
                    int randomNum = rand.nextInt(5000);
                    System.out.println("Se esperaron " + randomNum + "s");
                    //TimeUnit.SECONDS.sleep(randomNum);
                    Thread.sleep(randomNum);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }*/

                Optional<Funcion> funcion1 = funcionRepository.findById(idFuncion);
                if (funcion1.isPresent()) {
                    funcion = funcion1.get();
                    if (funcion.getEstado() == 0) {
                        return "redirect:/anErrorHasOcurred";
                    }
                } else {
                    return "redirect:/anErrorHasOcurred";
                }
            }
        } catch (NumberFormatException j) {
            return "redirect:/anErrorHasOcurred";
        }
        try {
            cantBoletos = Integer.parseInt(cantBoletosStr);
            if (cantBoletos <= 0) {
                redirectAttributes.addFlashAttribute("mensajeError", "El numero de boletos es incorrecto");
                return "redirect:/cartelera/DetallesObra?id=" + funcion.getIdobra().getId();
            }
        } catch (NumberFormatException m) {
            redirectAttributes.addFlashAttribute("mensajeError", "El numero de boletos es incorrecto");
            return "redirect:/cartelera/DetallesObra?id=" + funcion.getIdobra().getId();
        }

        Obra obra = funcion.getIdobra();
        int stockFuncion = funcion.getStockentradas();
        if (stockFuncion > 0 && cantBoletos <= stockFuncion) {
            LocalDate fechaActual = LocalDate.now();
            LocalDate fechaNacimientoUsuario = persona.getNacimiento();
            long edad = ChronoUnit.YEARS.between(fechaNacimientoUsuario,fechaActual);
            boolean mayorEdad;
            if(edad > 16){
                mayorEdad = true;
            }else if(edad == 16){
                long cumpleAntesFuncion = fechaActual.until(fechaNacimientoUsuario.plusYears(ChronoUnit.YEARS.between(fechaNacimientoUsuario,fechaActual)),ChronoUnit.DAYS);
                if(cumpleAntesFuncion <= 0){
                    mayorEdad = true;
                }else{
                    mayorEdad = false;
                }
            }else{
                mayorEdad = false;
            }

            if ((obra.getRestriccionedad() == 1 && mayorEdad == true) || obra.getRestriccionedad() == 0) {
                LinkedHashMap<Map<Integer,String>,Compra> carrito = (LinkedHashMap<Map<Integer,String>, Compra>) session.getAttribute("carritoDeComprasDeUsuario");
                boolean existeCruce = false;
                ArrayList<String> crucesHorarios = new ArrayList<>();
                if(carrito != null){
                    Collection<Compra> reservas = carrito.values();
                    for (Compra reserva : reservas) {
                        if(!reserva.getEstado().equals("Borrado")) {
                            LocalTime inicioReserva = reserva.getFuncion().getInicio();
                            LocalTime finReserva = reserva.getFuncion().getFin();
                            LocalDate fechaReserva = reserva.getFuncion().getFecha();
                            if (fechaReserva.equals(funcion.getFecha())){
                                boolean validacion1 = inicioReserva.isAfter(funcion.getInicio()) || inicioReserva.equals(funcion.getInicio());
                                boolean validacion2 = inicioReserva.isBefore(funcion.getFin()) || inicioReserva.equals(funcion.getFin());

                                boolean validacion3 = finReserva.isAfter(funcion.getInicio()) || finReserva.equals(funcion.getInicio());
                                boolean validacion4 = finReserva.isBefore(funcion.getFin()) || finReserva.equals(funcion.getFin());

                                if ((validacion1 && validacion2) || (validacion3 && validacion4)) {
                                    existeCruce = true;
                                    String mensaje = "Existe un cruce de horario de funcion con la obra " + reserva.getFuncion().getIdobra().getNombre() + " " + ",con hora de inicio:" + reserva.getFuncion().getInicio() + ",con hora fin :" + reserva.getFuncion().getFin();
                                    crucesHorarios.add(mensaje);
                                }
                            }
                        }
                    }

                }

                if (existeCruce) {
                    redirectAttributes.addFlashAttribute("cruceHorarioFuncion", crucesHorarios);
                    return "redirect:/cartelera/DetallesObra?id=" + funcion.getIdobra().getId();
                }else{
                    double precioEntradaFuncion = funcion.getPrecioentrada();
                    double montoTotal = precioEntradaFuncion * cantBoletos;
                    if (bindingResult.hasErrors()) {
                        System.out.println("----------------LLEGO BINDING RESULT");
                        Compra compraEnProceso = new Compra();
                        compraEnProceso.setCantidad(cantBoletos);
                        compraEnProceso.setMontoTotal(montoTotal);
                        compraEnProceso.setFuncion(funcion);
                        compraEnProceso.setPersona(persona);
                        model.addAttribute("compraEnProceso",compraEnProceso);

                        List<Foto> listaFotos = fotoRepository.unaFotoPorObra();
                        model.addAttribute("foto",listaFotos);
                        List<Obragenero> listaGeneros = obraGeneroRepository.findAll();
                        model.addAttribute("listaGeneros",listaGeneros);
                        return "usuario/pagoUsuario";
                    } else {
                        RestTemplate restTemplate = new RestTemplate();
                        String numero = coderService.codificar(datosTarjeta.getNumeroTarjeta());
                        String nombre = coderService.codificar(datosTarjeta.getNombresTitular());
                        String expiracion = coderService.codificar(datosTarjeta.getFechaVencimiento());
                        String cvv = coderService.codificar(datosTarjeta.getCodigoSeguridad());
                        String correo = coderService.codificar(persona.getCorreo());
                        String url = "http://20.90.180.72/validacion/verificar?numero="+numero+"&nombre="+nombre+
                                "&expiracion="+expiracion+"&cvv="+cvv+"&correo="+correo;
                        ResponseEntity<ValidacionTarjetaDto> response = restTemplate.getForEntity(url,ValidacionTarjetaDto.class);
                        boolean todoOK;
                        if(response.getBody().getMsg().equalsIgnoreCase("Se ha verificado y agregado la tarjeta de manera correcta")||response.getBody().getMsg().equalsIgnoreCase("Se ha verificado la tarjeta de manera correcta")){
                            todoOK=true;
                        }else{
                            todoOK=false;
                        }
                        System.out.println("-------------------");
                        System.out.println(response.getBody().getMsg());
                        System.out.println(todoOK);
                        if (todoOK) {
                            int cantidadAsistentes = funcion.getCantidadasistentes();
                            int stockRestanteFuncion = stockFuncion - cantBoletos;
                            cantidadAsistentes = cantidadAsistentes + cantBoletos;
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


                            Compra compra = new Compra();
                            compra.setPersona(persona);
                            compra.setCantidad(cantBoletos);
                            compra.setEstado("Vigente");
                            compra.setFuncion(funcion);
                            compra.setMontoTotal(montoTotal);


                            compraRepository.save(compra);

                            //Codigo unico de operacion
                            //https://www.baeldung.com/java-uuid
                            UUID uuid = UUID.randomUUID();
                            String numero_operacion = uuid.toString();
                            String url_for_qr = "http://telemillonario.centralus.cloudapp.azure.com:8080/qr?codigo="+numero_operacion;//se tiene que implementar el metodo
                            String url_encoded = coderService.codificar(url_for_qr);
                            RestTemplate restTemplate_for_qr = new RestTemplate();
                            String qr_service = "http://20.90.180.72/validacion/qrcode?link="+url_encoded;
                            ResponseEntity<QrDto> response_for_qr = restTemplate_for_qr.getForEntity(qr_service, QrDto.class);
                            String qr_link = response_for_qr.getBody().getUrl();
                            Pago pago = new Pago();
                            pago.setEstado(1);
                            if(datosTarjeta.getNombresTitular().equalsIgnoreCase("Visa")){
                                pago.setIdtarjeta(1);
                            }else if(datosTarjeta.getNombresTitular().equalsIgnoreCase("Mastercard")){
                                pago.setIdtarjeta(2);
                            }else if(datosTarjeta.getNombresTitular().equalsIgnoreCase("Diners Club")){
                                pago.setIdtarjeta(3);
                            }else{
                                System.out.println("Ha sucedido algo malo");
                            }

                            pago.setNumerotarjeta(datosTarjeta.getNumeroTarjeta());
                            pago.setFechapago(LocalDateTime.now());
                            pago.setIdcompra(compra);
                            pago.setQrlink(coderService.decodificar(response_for_qr.getBody().getUrl()));
                            pago.setCodigo(numero_operacion);
                            pagoRepository.save(pago);


                            /*String content = "<p>Cordiales Saludos: </p>"
                                    + "<p>Se ha efectuado correctamente la siguiente compra:</p>"
                                    + "<p>- Funcion de la obra:" + compra.getFuncion().getIdobra().getNombre() + " " + "<p>"
                                    + "<p>- Sede:" + compra.getFuncion().getIdsala().getIdsede().getNombre() + " " + "<p>"
                                    + "<p>- Sala:" + compra.getFuncion().getIdsala().getNumero() + " " + "<p>"
                                    + "<p>- Fecha:" + compra.getFuncion().getFecha() + " " + "<p>"
                                    + "<p>- Hora de inicio:" + compra.getFuncion().getInicio() + " " + "<p>"
                                    + "<p>- Hora fin:" + compra.getFuncion().getFin() + " " + "<p>"
                                    + "<p>- Cantidad de boletos: "+ compra.getCantidad() + " " + "<p>"
                                    + "<img src="+coderService.decodificar(response_for_qr.getBody().getUrl())+">";*/

                            String URL = fotoRepository.fotoObra(compra.getFuncion().getIdobra().getId());
                            String content = obtenerContentCorreo(URL,compra,coderService.decodificar(response_for_qr.getBody().getUrl()),datosTarjeta,numero_operacion);

                            try {
                                sendInfoCompraCorreo(compra.getPersona().getCorreo(),content);
                            } catch (MessagingException | UnsupportedEncodingException e) {
                                e.getMessage();
                                System.out.println("llego aca");
                                return "redirect:/anErrorHasOcurred";
                            }

                            redirectAttributes.addFlashAttribute("compraExitosa", "Se ha realizado su compra correctamente.");
                            return "redirect:/historialPrueba";

                        } else {
                            String mensaje = response.getBody().getMsg();
                            redirectAttributes.addFlashAttribute("mensajeError", mensaje);
                            System.out.println(mensaje);
                            System.out.println("llego aca");
                            System.out.println(idFuncionStr);
                            System.out.println(cantBoletosStr);
                            return "redirect:/compra?idFuncion="+idFuncionStr+"&cantBoletos="+cantBoletosStr;
                        }
                    }
                }
            } else {
                redirectAttributes.addFlashAttribute("mensajeError", "La funcion tiene restriccion de edad");
                return "redirect:/cartelera/DetallesObra?id=" + funcion.getIdobra().getId();
            }
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Ya no hay stock disponible");
            return "redirect:/cartelera/DetallesObra?id=" + funcion.getIdobra().getId();
        }

    }

    @GetMapping("/carritoPrueba")
    public String carritoUsuario(HttpSession session,Model model) {
        LinkedHashMap<Map<Integer,String>,Compra> carrito = (LinkedHashMap<Map<Integer,String>, Compra>) session.getAttribute("carritoDeComprasDeUsuario");
        List<Obragenero> listaGeneros = obraGeneroRepository.findAll();
        List<Foto> listaFotos = fotoRepository.unaFotoPorObra();

        if (carrito == null) {
            return "usuario/carrito/carritoComprasUsuario";
        } else {
            ArrayList<Compra> reservasBorrarCarrito = new ArrayList<>();
            Set<Map<Integer,String>> llavesGeneral = carrito.keySet();
            ArrayList<String> llaveDeHoras = new ArrayList<>();
            Integer j = 1;
            for(Map<Integer,String> llaves : llavesGeneral){
                /*for(Integer j = 1; j<=carrito.size(); j++){
                    if(llaves.get(j) != null){
                        llaveDeHoras.add(llaves.get(j));
                        System.out.println(llaves.get(j));
                    }
                }*/
                llaveDeHoras.add(llaves.get(j));
                j++;
            }

            for (String fechaReserva : llaveDeHoras) {
                String[] fechaHorasReserva = fechaReserva.split(" ",2);
                LocalTime hora = LocalTime.parse(fechaHorasReserva[1]);
                String fecha = fechaHorasReserva[0];
                LocalDate fechaActual = LocalDate.now();
                LocalTime horaActual = LocalTime.now();

                //Ojala haga la resta correcta entre minutos
                Duration duration = Duration.between(hora,horaActual);
                long minutes = duration.toMinutes();
                boolean estaATiempo = true;

                if (minutes > 15) {
                    estaATiempo = false;
                }

                if (estaATiempo == false) {
                    Integer id = 1;
                    for(Map<Integer,String> llaves : llavesGeneral){
                        for(Map.Entry<Integer,String> entry : llaves.entrySet()){
                            if(Objects.equals(fechaReserva,entry.getValue())){
                                id = entry.getKey();
                            }
                        }
                    }
                    Map<Integer,String> llav = new HashMap<>();
                    llav.put(id,fechaReserva);
                    Compra compra = carrito.get(llav);

                    if(compra.getEstado().equals("Reservado")){
                        compra.setEstado("Borrado");
                        carrito.put(llav,compra);
                        reservasBorrarCarrito.add(compra);
                    }
                }
            }
            session.setAttribute("carritoDeComprasDeUsuario", carrito);
            model.addAttribute("listaGeneros",listaGeneros);
            model.addAttribute("foto",listaFotos);

            /*LinkedHashMap<String,Integer> llaveInvertida = new LinkedHashMap<>();
            Set<Map<Integer,String>> lla = carrito.keySet();
            int indice = 1;
            for(Map<Integer,String> llaves : lla){
                if(llaves.get(indice) != null){
                    String fechaHora = llaves.get(indice);
                    llaveInvertida.put(fechaHora,indice);
                }
                indice++;
            }
            model.addAttribute("llavesInvertidas",llaveInvertida);*/

            if (reservasBorrarCarrito.size() == 0) {
                return "usuario/carrito/carritoComprasUsuario";
            } else {
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

                return "usuario/carrito/carritoComprasUsuario";
            }
        }

    }

    @GetMapping("/borrarReserva")
    private String borrarReservaDeCarrito(@RequestParam(value = "idReserva",required = false) String idReservaStr,RedirectAttributes redirectAttributes,HttpSession session){

        if(idReservaStr == null){
            return "redirect:/anErrorHasOcurred";
        }

        LinkedHashMap<Map<Integer,String>,Compra> carrito = (LinkedHashMap<Map<Integer,String>, Compra>) session.getAttribute("carritoDeComprasDeUsuario");

        if (carrito == null) {
            redirectAttributes.addFlashAttribute("mensajeError", "Debe adicionar reservas");
            return "redirect:/carritoPrueba";
        }


        int idReserva = 0;

        try {
            idReserva = Integer.parseInt(idReservaStr);
            if (idReserva <= 0 || (idReserva > carrito.size())) {
                redirectAttributes.addFlashAttribute("mensajeError", "La compra no existe");
                return "redirect:/carritoPrueba";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "La compra no existe");
            return "redirect:/carritoPrueba";
        }


        Set<Map<Integer,String>> llavesGeneral = carrito.keySet();
        for(Map<Integer,String> llaves : llavesGeneral){
            if(llaves.get(idReserva) != null){
                String fechaHoraBorrada = llaves.get(idReserva);
                Map<Integer,String> llaveBorrar = new HashMap<>();
                llaveBorrar.put(idReserva,fechaHoraBorrada);

                Compra compra = carrito.get(llaveBorrar);
                if(compra.getEstado().equals("Reservado")){
                    compra.setEstado("Borrado");
                    carrito.put(llaveBorrar,compra);
                    session.setAttribute("carritoDeComprasDeUsuario", carrito);
                    redirectAttributes.addFlashAttribute("reservaEliminada", "Su reserva se ha eliminado satisfactoriamente");
                }else{
                    redirectAttributes.addFlashAttribute("mensajeError", "La reserva ya ha sido cancelada");
                }
                break;
            }
        }
        return "redirect:/carritoPrueba";
    }


    @GetMapping("/compraReservasCarrito")
    private String comprarReservasDelCarrito(@ModelAttribute("datosTarjeta") DatosTarjeta datosTarjeta,@RequestParam(value = "listaReservas",required = false)String listaReservasStr,
                                             @RequestParam(value = "listaCantidadBoletos",required = false)String listaCantidadBoletosStr,
                                             RedirectAttributes redirectAttributes, HttpSession session,Model model){
        LinkedHashMap<Map<Integer,String>,Compra> carrito = (LinkedHashMap<Map<Integer,String>, Compra>) session.getAttribute("carritoDeComprasDeUsuario");
        Persona persona = (Persona) session.getAttribute("usuario");
        if (carrito == null) {
            redirectAttributes.addFlashAttribute("mensajeError", "Debe adicionar por lo menos una reserva");
            return "redirect:/carritoPrueba";
        }
        if(listaReservasStr == null || listaCantidadBoletosStr == null){
            return "redirect:/anErrorHasOcurred";
        }

        String[] reservas = listaReservasStr.split(",");
        String[] cantidad = listaCantidadBoletosStr.split(",");

        if(reservas.length != cantidad.length){
            redirectAttributes.addFlashAttribute("mensajeError", "Error al Procesar la Compra");
            return "redirect:/carritoPrueba";
        }

        ArrayList<Integer> listaReservas = new ArrayList<>();
        ArrayList<Integer> listaCantidadBoletos = new ArrayList<>();
        try{
            int valorReserva;
            int valorCantidad;
            for(int i=0;i<reservas.length;i++){
                valorReserva = Integer.parseInt(reservas[i]);
                if(valorReserva <= 0 || valorReserva>carrito.size()){
                    redirectAttributes.addFlashAttribute("mensajeError", "Error al Procesar la Compra");
                    return "redirect:/carritoPrueba";
                }

                valorCantidad = Integer.parseInt(cantidad[i]);

                if(valorCantidad <= 0){
                    redirectAttributes.addFlashAttribute("mensajeError", "Error al Procesar la Compra");
                    return "redirect:/carritoPrueba";
                }

                listaReservas.add(valorReserva);
                listaCantidadBoletos.add(valorCantidad);
            }

        }catch (NumberFormatException j){
            redirectAttributes.addFlashAttribute("mensajeError", "Error al Procesar la Compra");
            return "redirect:/carritoPrueba";
        }

        for(int k : listaReservas){
            int repetidas = 0;
            for(int j : listaReservas){
                if(k == j){
                    repetidas = repetidas + 1;
                }

                if(repetidas > 1){
                    redirectAttributes.addFlashAttribute("mensajeError", "La reservas no se pueden repetir");
                    return "redirect:/carritoPrueba";
                }
            }
        }

        Set<Map<Integer,String>> llavesGeneral = carrito.keySet();
        ArrayList<Compra> reservasBorrarCarrito = new ArrayList<>();
        ArrayList<Compra> reservasComprarCarrito = new ArrayList<>();
        for(int m : listaReservas){
            for(Map<Integer,String> llaves : llavesGeneral){
                if(llaves.get(m) != null){
                    String fechaHora = llaves.get(m);
                    Map<Integer,String> llave = new HashMap<>();
                    llave.put(m,fechaHora);

                    Compra compra = carrito.get(llave);
                    if(compra.getEstado().equals("Borrado")){
                        redirectAttributes.addFlashAttribute("mensajeError", "Verifique de que la(s) reserva(s) exista(n)");
                        return "redirect:/carritoPrueba";
                    }

                    Optional<Funcion> funcion1 = funcionRepository.findById(compra.getFuncion().getId());
                    Funcion funcion = funcion1.get();
                    Obra obra = funcion.getIdobra();

                    int indiceReserva = listaReservas.indexOf(m);
                    int cantidadBoletos = listaCantidadBoletos.get(indiceReserva);

                    int stockFuncion = funcion.getStockentradas();

                    if (stockFuncion < 0 || cantidadBoletos > stockFuncion){
                        reservasBorrarCarrito.add(compra);
                    }else{
                        Compra compra1 = new Compra();
                        compra1.setFuncion(funcion);
                        compra1.setPersona(persona);
                        compra1.setCantidad(cantidadBoletos);
                        compra1.setMontoTotal(cantidadBoletos*funcion.getPrecioentrada());
                        compra1.setEstado("Reservado");
                        reservasComprarCarrito.add(compra);
                    }

                    break;
                }
            }
        }

        if (reservasBorrarCarrito.size() == 0) {
            List<Foto> listaFotos = fotoRepository.unaFotoPorObra();
            model.addAttribute("foto",listaFotos);
            List<Obragenero> listaGeneros = obraGeneroRepository.findAll();
            model.addAttribute("listaGeneros",listaGeneros);
            model.addAttribute("listaReservasCarrito",reservasComprarCarrito);
            model.addAttribute("listaReservas",listaReservasStr);
            model.addAttribute("listaCantidadBoletos",listaCantidadBoletosStr);
            return "usuario/pagoReservasUsuario";
        } else {
            ArrayList<String> mensajes = new ArrayList<>();
            for (Compra compra : reservasBorrarCarrito) {
                mensajes.add("No hay stock disponible para la Funcion con Obra : "+compra.getFuncion().getIdobra().getNombre()+", con horario de inicio:"+compra.getFuncion().getInicio()+", y con hora fin:"+compra.getFuncion().getFin());
            }
            redirectAttributes.addFlashAttribute("ReservasSinStock", mensajes);
            return "redirect:/carritoPrueba";
        }

    }

    @PostMapping("/pagoReservasCarrito")
    public String pagoReservasCarrito(@RequestParam(value = "listaReservas",required = false)String listaReservasStr,
                               @RequestParam(value = "listaCantidadBoletos",required = false)String listaCantidadBoletosStr,@ModelAttribute("datosTarjeta") @Valid DatosTarjeta datosTarjeta, BindingResult bindingResult, HttpSession session,
                               RedirectAttributes redirectAttributes,Model model) {

        LinkedHashMap<Map<Integer,String>,Compra> carrito = (LinkedHashMap<Map<Integer,String>, Compra>) session.getAttribute("carritoDeComprasDeUsuario");
        Persona persona = (Persona) session.getAttribute("usuario");

        System.out.println("listaReservas: " + listaReservasStr);
        System.out.println("listaBoletos: " + listaCantidadBoletosStr);

        if (carrito == null) {
            System.out.println("El carrito no existe");
            return "redirect:/anErrorHasOcurred";
        }

        if(listaReservasStr == null || listaCantidadBoletosStr == null){
            System.out.println("Los datos no se recibieron bien");
            return "redirect:/anErrorHasOcurred";
        }

        String[] reservas = listaReservasStr.split(",");
        String[] cantidad = listaCantidadBoletosStr.split(",");

        if(reservas.length != cantidad.length){
            redirectAttributes.addFlashAttribute("mensajeError", "Error al Procesar la Compra");
            return "redirect:/carritoPrueba";
        }

        ArrayList<Integer> listaReservas = new ArrayList<>();
        ArrayList<Integer> listaCantidadBoletos = new ArrayList<>();
        try{
            int valorReserva;
            int valorCantidad;
            for(int i=0;i<reservas.length;i++){
                valorReserva = Integer.parseInt(reservas[i]);
                if(valorReserva <= 0 || valorReserva>carrito.size()){
                    redirectAttributes.addFlashAttribute("mensajeError", "Error al Procesar la Compra");
                    return "redirect:/carritoPrueba";
                }

                valorCantidad = Integer.parseInt(cantidad[i]);

                if(valorCantidad <= 0){
                    redirectAttributes.addFlashAttribute("mensajeError", "Error al Procesar la Compra");
                    return "redirect:/carritoPrueba";
                }

                listaReservas.add(valorReserva);
                listaCantidadBoletos.add(valorCantidad);
            }

        }catch (NumberFormatException j){
            redirectAttributes.addFlashAttribute("mensajeError", "Error al Procesar la Compra");
            return "redirect:/carritoPrueba";
        }


        for(int k : listaReservas){
            int repetidas = 0;
            for(int j : listaReservas){
                if(k == j){
                    repetidas = repetidas + 1;
                }

                if(repetidas > 1){
                    redirectAttributes.addFlashAttribute("mensajeError", "La reservas no se pueden repetir");
                    return "redirect:/carritoPrueba";
                }
            }
        }

        Set<Map<Integer,String>> llavesGeneral = carrito.keySet();
        ArrayList<Compra> reservasBorrarCarrito = new ArrayList<>();
        ArrayList<Compra> reservasComprarCarrito = new ArrayList<>();
        for(int m : listaReservas){
            for(Map<Integer,String> llaves : llavesGeneral){
                if(llaves.get(m) != null){
                    String fechaHora = llaves.get(m);
                    Map<Integer,String> llave = new HashMap<>();
                    llave.put(m,fechaHora);

                    Compra compra = carrito.get(llave);
                    if(compra.getEstado().equals("Borrado")){
                        redirectAttributes.addFlashAttribute("mensajeError", "Verifique de que la(s) reserva(s) exista(n)");
                        return "redirect:/carritoPrueba";
                    }

                    Optional<Funcion> funcion1 = funcionRepository.findById(compra.getFuncion().getId());
                    Funcion funcion = funcion1.get();
                    Obra obra = funcion.getIdobra();

                    int indiceReserva = listaReservas.indexOf(m);
                    int cantidadBoletos = listaCantidadBoletos.get(indiceReserva);

                    int stockFuncion = funcion.getStockentradas();
                    if (stockFuncion < 0 || cantidadBoletos > stockFuncion){
                        reservasBorrarCarrito.add(compra);
                    }else{
                        //Compra compra1 = new Compra();
                        //compra1.setFuncion(funcion);
                        //compra1.setPersona(persona);
                        //compra1.setCantidad(cantidadBoletos);
                        //compra1.setMontoTotal(cantidadBoletos*funcion.getPrecioentrada());
                        //compra1.setEstado("Reservado");
                        compra.setCantidad(cantidadBoletos);
                        reservasComprarCarrito.add(compra);
                    }

                    break;
                }
            }
        }
        if (reservasBorrarCarrito.size() == 0) {
            if (bindingResult.hasErrors()) {
                List<Foto> listaFotos = fotoRepository.unaFotoPorObra();
                model.addAttribute("foto",listaFotos);
                List<Obragenero> listaGeneros = obraGeneroRepository.findAll();
                model.addAttribute("listaGeneros",listaGeneros);
                model.addAttribute("listaReservasCarrito",reservasComprarCarrito);
                model.addAttribute("listaReservas",listaReservasStr);
                model.addAttribute("listaCantidadBoletos",listaCantidadBoletosStr);
                return "usuario/pagoReservasUsuario";
            } else {
                RestTemplate restTemplate = new RestTemplate();
                String numero = coderService.codificar(datosTarjeta.getNumeroTarjeta());
                String nombre = coderService.codificar(datosTarjeta.getNombresTitular());
                String expiracion = coderService.codificar(datosTarjeta.getFechaVencimiento());
                String cvv = coderService.codificar(datosTarjeta.getCodigoSeguridad());
                String correo = coderService.codificar(persona.getCorreo());
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
                    UUID uuid = UUID.randomUUID();
                    String numero_operacion = uuid.toString();
                    String url_for_qr = "http://telemillonario.centralus.cloudapp.azure.com:8080/qr?codigo="+numero_operacion;
                    String url_encoded = coderService.codificar(url_for_qr);
                    RestTemplate restTemplate_for_qr = new RestTemplate();
                    String qr_service = "http://20.90.180.72/validacion/qrcode?link="+url_encoded;
                    ResponseEntity<QrDto> response_for_qr = restTemplate_for_qr.getForEntity(qr_service, QrDto.class);
                    String qr_link = response_for_qr.getBody().getUrl();

                    String content = "<p>Cordiales Saludos: </p>"
                            + "<p>Se ha efectuado correctamente la siguientes compras("+reservasComprarCarrito.size()+"):</p>";
                    for(Compra compra : reservasComprarCarrito){

                        /*try{
                            Random rand = new Random();
                            int randomNum = rand.nextInt(5000);
                            System.out.println("Se esperaron " + randomNum + "s");
                            //TimeUnit.SECONDS.sleep(randomNum);
                            Thread.sleep(randomNum);
                        } catch (Exception e){
                            System.out.println(e.getMessage());
                        }*/

                        Optional<Funcion> funcionOptional = funcionRepository.findById(compra.getFuncion().getId());
                        Funcion  funcion = funcionOptional.get();
                        double precioEntradaFuncion = funcion.getPrecioentrada();
                        double montoTotal = precioEntradaFuncion * compra.getCantidad();
                        int cantidadAsistentes = funcion.getCantidadasistentes();
                        int stockRestanteFuncion = funcion.getStockentradas() - compra.getCantidad();
                        cantidadAsistentes = cantidadAsistentes + compra.getCantidad();



                        System.out.println(compra.getEstado());
                        Compra compra1 = new Compra();
                        compra1.setCantidad(compra.getCantidad());
                        compra1.setEstado("Vigente");
                        compra1.setPersona(persona);
                        compra1.setMontoTotal(montoTotal);
                        compra1.setPersona(persona);
                        compra1.setFuncion(funcion);

                        content = content + "<p>- Funcion de la obra:" + compra1.getFuncion().getIdobra().getNombre() + " " + "<p>"
                                + "<p>- Sede:" + compra1.getFuncion().getIdsala().getIdsede().getNombre() + " " + "<p>"
                                + "<p>- Sala:" + compra1.getFuncion().getIdsala().getNumero() + " " + "<p>"
                                + "<p>- Fecha:" + compra1.getFuncion().getFecha() + " " + "<p>"
                                + "<p>- Hora de inicio:" + compra1.getFuncion().getInicio() + " " + "<p>"
                                + "<p>- Hora fin:" + compra1.getFuncion().getFin() + " " + "<p>"
                                + "<p>- Cantidad de boletos: "+ compra1.getCantidad() + " " + "<p>" + "<br>";

                        compraRepository.save(compra1);

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

                        Pago pago = new Pago();
                        pago.setEstado(1);
                        if(datosTarjeta.getNombresTitular().equalsIgnoreCase("Visa")){
                            pago.setIdtarjeta(1);
                        }else if(datosTarjeta.getNombresTitular().equalsIgnoreCase("Mastercard")){
                            pago.setIdtarjeta(2);
                        }else if(datosTarjeta.getNombresTitular().equalsIgnoreCase("Diners Club")){
                            pago.setIdtarjeta(3);
                        }else{
                            System.out.println("Ha sucedido algo malo");
                        }
                        pago.setNumerotarjeta(datosTarjeta.getNumeroTarjeta());
                        pago.setFechapago(LocalDateTime.now());
                        pago.setIdcompra(compra1);
                        pago.setQrlink(coderService.decodificar(response_for_qr.getBody().getUrl()));
                        pago.setCodigo(numero_operacion);
                        pagoRepository.save(pago);

                    }

                    content = content + "<img src="+coderService.decodificar(response_for_qr.getBody().getUrl())+">";


                    Set<Map<Integer,String>> l1 = carrito.keySet();
                    for(int m : listaReservas){
                        for(Map<Integer,String> llaves : llavesGeneral){
                            if(llaves.get(m) != null){
                                String fechaHora = llaves.get(m);
                                Map<Integer,String> llaveBorrar = new HashMap<>();
                                llaveBorrar.put(m,fechaHora);
                                Compra compra = carrito.get(llaveBorrar);
                                compra.setEstado("Borrado");
                                carrito.put(llaveBorrar,compra);
                            }
                        }
                    }

                    session.setAttribute("carritoDeComprasDeUsuario", carrito);

                    try {
                        sendInfoCompraCorreo(persona.getCorreo(),content);
                    } catch (MessagingException | UnsupportedEncodingException e) {
                        System.out.println("No se pudo mandar el correo");
                        return "redirect:/anErrorHasOcurred";
                    }

                    redirectAttributes.addFlashAttribute("compraExitosa", "Se ha realizado su compra correctamente.");
                    return "redirect:/historialPrueba";

                } else {
                    redirectAttributes.addFlashAttribute("mensajeError",response.getBody().getMsg());
                    return "redirect:/compraReservasCarrito?listaReservas="+listaReservasStr+"&listaCantidadBoletos="+listaCantidadBoletosStr;
                }
            }

        } else {
            ArrayList<String> mensajes = new ArrayList<>();
            for (Compra compra : reservasBorrarCarrito) {
                mensajes.add("No hay stock disponible para la Funcion con Obra : "+compra.getFuncion().getIdobra().getNombre()+", con horario de inicio:"+compra.getFuncion().getInicio()+", y con hora fin:"+compra.getFuncion().getFin());
            }
            redirectAttributes.addFlashAttribute("ReservasSinStock", mensajes);
            return "redirect:/carritoPrueba";
        }

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


    private String obtenerContentCorreo(String URL,Compra compra,String QR,DatosTarjeta datosTarjeta,String numeroOperacion){

        DateTimeFormatter dtf5 = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm");

        String content = "<div tabindex=\"-1\" class=\"YE9Rk customScrollBar\" data-is-scrollable=\"true\">\n" +
                "            <div>\n" +
                "                <div class=\"wide-content-host\">\n" +
                "                    <div class=\"VToAP ufHEA\" style=\"margin-right: 20px; margin-left: 8px; padding: 0px 12px 12px; width: calc(100% - 53.5px);\">\n" +
                "                        <div class=\"n5mNi PXxvZ e8EOp\">\n" +
                "                        </div>\n" +
                "                        <div role=\"region\" tabindex=\"-1\" aria-label=\"Cuerpo del mensaje\" class=\"njsxS qhSqO TiApU allowTextSelection\">\n" +
                "                            <div>\n" +
                "                                <div class=\"rps_9eb5\">\n" +
                "                                    <div>\n" +
                "                                        <meta content=\"IE=edge\">\n" +
                "                                        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "                                        <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"500\" style=\"margin-top:40px; margin-bottom:40px; border:2px solid #ddd\">\n" +
                "                                            <tbody>\n" +
                "                                                <tr>\n" +
                "                                                    <td>\n" +
                "                                                        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" +
                "                                                            <tbody>\n" +
                "                                                                <tr>\n" +
                "                                                                    <td height=\"80\">\n" +
                "                                                                        <img data-imagetype=\"AttachmentByCid\"  naturalheight=\"0\" naturalwidth=\"0\" src='https://telemillonariocontainer.blob.core.windows.net/telemillonario/tele.png' alt=\"logo_cinemark\" style=\"width: 250px; margin: 0px auto; display: block; cursor: pointer; min-width: auto; min-height: auto;\" crossorigin=\"use-credentials\" class=\"ADIae\">\n" +
                "                                                                    </td>\n" +
                "                                                                </tr>\n" +
                "                                                                <tr>\n" +
                "                                                                    <td height=\"80\" bgcolor=\"#ff5860\" style=\"padding:0 20px\">\n" +
                "                                                                        <h2 style=\"font-family:'Arial',sans-serif; font-weight:500; font-size:20px; margin:0 0 5px\">\n" +
                "                                                                            <span style=\"color:#fff;text-transform:uppercase; font-family:'Arial',sans-serif\">¡ Hola "+compra.getPersona().getNombres()+' '+compra.getPersona().getApellidos()+ " ! </span>\n" +
                "                                                                        </h2>\n" +
                "                                                                        <p style=\"color:white;font-family:'Arial',sans-serif; font-size:14px; font-weight:300; margin:0\">A continuación verás el detalle de tu compra.</p>\n" +
                "                                                                        <div>\n" +
                "                                                                            <div align=\"left\" style=\"color:#fff\">"+dtf5.format(LocalDateTime.now())+"</div>\n" +
                "                                                                        </div>\n" +
                "                                                                    </td>\n" +
                "                                                                </tr>\n" +
                "                                                                <tr>\n" +
                "                                                                    <td height=\"80\" align=\"center\" bgcolor=\"#eee\" style=\"padding-top:40px; padding-bottom:20px; border-bottom-width:1px; border-bottom-color:#d2d2d2; border-bottom-style:solid\">\n" +
                "                                                                        <span style=\"background-color:#fff!important; display:table; padding:0px\"><img data-imagetype=\"AttachmentByCid\"   naturalheight=\"0\" naturalwidth=\"0\" src='"+QR+"' alt=\"qrcode\" style=\"background-color: rgb(255, 255, 255); margin: 0px auto 10px; padding: 10px 0px 0px; border: 0px; width: 150px !important; cursor: pointer; min-width: auto; min-height: auto;\" crossorigin=\"use-credentials\" class=\"ADIae\">\n" +
                "                                                                        </span>\n" +
                "                                                                    </td>\n" +
                "                                                                </tr>\n" +
                "                                                                <tr>\n" +
                "                                                                    <td bgcolor=\"#eee\" style=\"\">\n" +
                "\n" +
                "                                                                    </td>\n" +
                "                                                                </tr>\n" +
                "                                                                <tr>\n" +
                "                                                                    <td height=\"\" width=\"100%;\" style=\"font-family:'Arial',sans-serif; padding:0 20px 20px\">\n" +
                "                                                                        <h2 style=\"color:#ff5860; margin-top:20px; font-size:18px; text-transform:uppercase; font-family:'Arial',sans-serif\">Descripción de la compra </h2>\n" +
                "                                                                        <div class=\"x_cover-container\" style=\"float:left; margin-right:15px; max-width:250px; overflow:hidden\">\n" +
                "                                                                            <img data-imagetype=\"External\" src='"+URL+"' alt=\"img_movie\" style='max-width:120px;height:130px'>\n" +
                "                                                                        </div>\n" +
                "                                                                        <div class=\"x_text-details\" style=\"display:block\">\n" +
                "                                                                            <span style=\"display:block; margin-bottom:8px\">"+compra.getFuncion().getIdobra().getNombre()+"</span>\n" +
                "                                                                            <span style=\"display:block; margin-bottom:8px\"><strong>Teatro: </strong>"+compra.getFuncion().getIdsala().getIdsede().getNombre()+" </span>\n" +
                "                                                                            <span style=\"display:block; margin-bottom:8px\"><strong>Fecha: </strong>"+compra.getFuncion().getFecha()+" </span>\n" +
                "                                                                            <span style=\"display:block; margin-bottom:8px\"><strong>Hora:</strong>"+compra.getFuncion().getInicio()+"</span>\n" +
                "                                                                            <span style=\"display:block; margin-bottom:8px\"><strong>Sala:</strong>"+compra.getFuncion().getIdsala().getNumero()+"</span>\n" +
                "                                                                        </div>\n" +
                "                                                                    </td>\n" +
                "                                                                </tr>\n" +
                "                                                                <tr>\n" +
                "                                                                    <td bgcolor=\"#eee\" style=\"\"></td>\n" +
                "                                                                </tr>\n" +
                "                                                                <tr>\n" +
                "                                                                    <td height=\"\" width=\"100%;\" style=\"padding:0 20px 20px\">\n" +
                "                                                                        <h2 style=\"color:#ff5860; margin-top:20px; font-size:18px; text-transform:uppercase; font-family:'Arial',sans-serif\">Detalle de la compra </h2>\n" +
                "                                                                        <ul style=\"list-style-type:none; font-family:'Arial',sans-serif; font-size:14px; padding:0 5px\">\n" +
                "                                                                            <div id=\"x_payment-details\" style=\"padding:10px\">\n" +
                "                                                                                <div id=\"x_pricing-table\" style=\"width:75%; margin:0 auto\">\n" +
                "                                                                                    <table class=\"x_pricing\" style=\"width:100%\">\n" +
                "                                                                                        <tbody>\n" +
                "                                                                                            <tr>\n" +
                "                                                                                                <td><strong>Entradas</strong></td>\n" +
                "                                                                                                <td align=\"right\" style=\"\"><strong>Precio</strong></td>\n" +
                "                                                                                            </tr>\n" +
                "                                                                                            <tr>\n" +
                "                                                                                                <td>"+compra.getCantidad()+" BOLETO(S)<br aria-hidden=\"true\"></td>\n" +
                "                                                                                                <td align=\"right\" style=\"\">S/ "+compra.getMontoTotal()+"<br aria-hidden=\"true\"></td>\n" +
                "                                                                                            </tr>\n" +
                "                                                                                            <tr>\n" +
                "                                                                                                <td colspan=\"2\" align=\"right\" style=\"border-top-width:1.5px; border-top-color:#B5121B; border-top-style:solid\"><br aria-hidden=\"true\">Total: S/ "+compra.getMontoTotal()+" </td>\n" +
                "                                                                                            </tr>\n" +
                "                                                                                        </tbody>\n" +
                "                                                                                    </table>\n" +
                "                                                                                </div>\n" +
                "                                                                            </div>\n" +
                "                                                                        </ul>\n" +
                "                                                                    </td>\n" +
                "                                                                </tr>\n" +
                "                                                                <tr>\n" +
                "                                                                </tr>\n" +
                "                                                                <tr>\n" +
                "                                                                    <td bgcolor=\"#c1ce50\" style=\"\">\n" +
                "                                                                        <h4 align=\"center\" style=\"margin-top:20px; color:#000000; text-transform:uppercase; font-size:13px!important\">Recomendaciones para una experiencia segura en nuestros teatros: </h4>\n" +
                "                                                                        <ul style=\"margin:0; padding:0px 40px 40px\">\n" +
                "                                                                            <li style=\"font-size:12px; margin-bottom:10px\">Respeta tu ubicación en la sala y mantén en todo momento la sana distancia.</li>\n" +
                "                                                                            <li style=\"font-size:12px; margin-bottom:10px\">Usa siempre doble mascarilla durante toda tu estadía.</li>\n" +
                "                                                                            <li style=\"font-size:12px; margin-bottom:10px\">Higieniza tus manos constantemente con el alcohol en gel que hemos colocado dentro de todos nuestros cines.</li>\n" +
                "                                                                            <li style=\"font-size:12px; margin-bottom:10px\">Sigue las indicaciones de salida de la sala de manera ordenada y así todos evitaremos aglomeraciones.</li>\n" +
                "                                                                            <li style=\"font-size:12px; margin-bottom:10px\">Si siente síntomas COVID-19 como fiebre, tos, estornudos o dificultad para respirar, expectoración, pérdida del gusto o del olfato, cansancio u otros síntomas, evite salir de casa y asistir a lugares o eventos públicos.</li>\n" +
                "                                                                            <li style=\"font-size:12px; margin-bottom:10px\">Sobre las validaciones de vacunación completa*: te recomendamos llevar tu carnet de vacunación física o virtualmente para presentarlo frente a los podios. Puedes también descargarlo desde la web oficial del minsa y tenerla en tu celular ingresando a este link: https://gis.minsa.gob.pe/CarneVacunacion/</li>\n" +
                "                                                                            <p style=\"font-size:12px; margin-bottom:10px\">*El requisito de validación de vacunación completa aplica únicamente para los Cinemark en ciudades donde se haya superado el 40% de vacunación completa en la ciudad.</p>\n" +
                "                                                                        </ul>\n" +
                "                                                                    </td>\n" +
                "                                                                </tr>\n" +
                "                                                                <tr>\n" +
                "                                                                    <td bgcolor=\"#eee\" style=\"\">\n" +
                "                                                                        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" +
                "                                                                            <tbody>\n" +
                "                                                                                <tr>\n" +
                "                                                                                    <td colspan=\"2\" height=\"\" width=\"100%;\" style=\"padding:0 20px 20px\">\n" +
                "                                                                                        <h2 style=\"color:#ff5860; margin-top:20px; font-size:18px; text-transform:uppercase; font-family:'Arial',sans-serif\">Detalle del pago </h2>\n" +
                "                                                                                    </td>\n" +
                "                                                                                </tr>\n" +
                "                                                                                <tr>\n" +
                "                                                                                    <td height=\"\" width=\"100%;\" style=\"padding:0 20px\">\n" +
                "                                                                                        <p style=\"color:#454545; margin-top:10px; font-size:15px; text-transform:uppercase; font-family:'Arial',sans-serif\"><strong>Respuesta medio de pago</strong> </p>\n" +
                "                                                                                    </td>\n" +
                "                                                                                    <td height=\"\" width=\"100%;\" style=\"padding:0 20px\"><p style=\"color:#454545; margin-top:10px; font-size:14px; font-family:'Arial',sans-serif\">Transacción aprobada </p></td>\n" +
                "                                                                                </tr>\n" +
                "                                                                                <tr>\n" +
                "                                                                                    <td height=\"\" width=\"100%;\" style=\"padding:0 20px\"><p style=\"color:#454545; margin-top:10px; font-size:15px; text-transform:uppercase; font-family:'Arial',sans-serif\"><strong>Código de transacción</strong> </p></td>\n" +
                "                                                                                    <td height=\"\" width=\"100%;\" style=\"padding:0 20px\"><p style=\"color:#454545; margin-top:10px; font-size:14px; font-family:'Arial',sans-serif\">"+numeroOperacion+"</p></td>\n" +
                "                                                                                </tr>\n" +
                "                                                                                <tr>\n" +
                "                                                                                    <td height=\"\" width=\"100%;\" style=\"padding:0 20px\"><p style=\"color:#454545; margin-top:10px; font-size:15px; text-transform:uppercase; font-family:'Arial',sans-serif\"><strong>Medio de pago</strong> </p></td>\n" +
                "                                                                                    <td height=\"\" width=\"100%;\" style=\"padding:0 20px\"><p style=\"color:#454545; margin-top:10px; font-size:14px; font-family:'Arial',sans-serif\">"+datosTarjeta.getNombresTitular()+"</p></td>\n" +
                "                                                                                </tr>\n" +
                "                                                                                <tr>\n" +
                "                                                                                    <td height=\"\" width=\"100%;\" style=\"padding:0 20px\"><p style=\"color:#454545; margin-top:10px; font-size:15px; text-transform:uppercase; font-family:'Arial',sans-serif\"><strong>Número tarjeta</strong> </p></td>\n" +
                "                                                                                    <td height=\"\" width=\"100%;\" style=\"padding:0 20px\"><p style=\"color:#454545; margin-top:10px; font-size:14px; font-family:'Arial',sans-serif\">"+datosTarjeta.getNumeroTarjeta()+" </p></td>\n" +
                "                                                                                </tr>\n" +
                "                                                                                <tr>\n" +
                "                                                                                    <td height=\"\" width=\"100%;\" style=\"padding:0 20px\"><p style=\"color:#454545; margin-top:10px; font-size:15px; text-transform:uppercase; font-family:'Arial',sans-serif\"><strong>Fecha compra</strong> </p></td>\n" +
                "                                                                                    <td height=\"\" width=\"100%;\" style=\"padding:0 20px\"><p style=\"color:#454545; margin-top:10px; font-size:14px; font-family:'Arial',sans-serif\">"+dtf5.format(LocalDateTime.now())+"</p></td>\n" +
                "                                                                                </tr>\n" +
                "                                                                                <tr>\n" +
                "                                                                                    <td height=\"\" width=\"100%;\" style=\"padding:0 20px\"><p style=\"color:#454545; margin-top:10px; font-size:15px; text-transform:uppercase; font-family:'Arial',sans-serif\"><strong>Total compra</strong> </p></td>\n" +
                "                                                                                    <td height=\"\" width=\"100%;\" style=\"padding:0 20px\"><p style=\"color:#454545; margin-top:10px; font-size:14px; font-family:'Arial',sans-serif\">S/ "+compra.getMontoTotal()+" </p></td>\n" +
                "                                                                                </tr>\n" +
                "                                                                            </tbody>\n" +
                "                                                                        </table>\n" +
                "                                                                    </td>\n" +
                "                                                                </tr>\n" +
                "                                                                <tr>\n" +
                "                                                                    <td>\n" +
                "                                                                        <h4 align=\"center\" style=\"margin-top:20px; color:#ff5860; text-transform:uppercase; font-size:13px!important\">Términos y condiciones de compra online </h4>\n" +
                "                                                                        <ul style=\"margin:0; padding:0px 40px 40px\">\n" +
                "                                                                            <li style=\"font-size:12px; margin-bottom:10px; color:#737373\">Esta entrada es tu comprobante de pago.</li>\n" +
                "                                                                            <li style=\"font-size:12px; margin-bottom:10px; color:#737373\">Para validar la entrada y combos es indispensable presentar la tarjeta y DNI con la que fue realizada la compra. Esta medida de seguridad protege su compra de estafadores y ventas no oficiales.</li>\n" +
                "                                                                            <li style=\"font-size:12px; margin-bottom:10px; color:#737373\">Para películas con clasificación mayores de 16 años es indispensable presentar el documento de identidad al momento de ingresar a la sala.</li>\n" +
                "                                                                        </ul>\n" +
                "                                                                    </td>\n" +
                "                                                                </tr>\n" +
                "                                                            </tbody>\n" +
                "                                                        </table>\n" +
                "                                                    </td>\n" +
                "                                                </tr>\n" +
                "                                            </tbody>\n" +
                "                                        </table>\n" +
                "                                    </div>\n" +
                "                                </div>\n" +
                "                            </div>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "        </div>";

        return  content;
    }





    @GetMapping("/historialPrueba")
    String historialPrueba(Model model,HttpSession session) {
        Compra compraEnProceso = (Compra) session.getAttribute("compraEnProceso");
        compraEnProceso = null;
        session.setAttribute("compraEnProceso",compraEnProceso);

        Persona persona = (Persona) session.getAttribute("usuario");

        List<Compra> listaCompras = compraRepository.historialCompras(persona.getId());
        List<Compra> listaComprasRevisadas = new ArrayList<>();
        for (Compra c : listaCompras) {
            LocalDate fechaActual = LocalDate.now();
            if (fechaActual.compareTo(c.getFuncion().getFecha()) > 0) { //Fecha ya paso, debo cambiarle a estado cancelado
                compraRepository.actualizacionEstadoCompra("Asistido",persona.getId(), c.getId());
            } else if (fechaActual.compareTo(c.getFuncion().getFecha()) == 0) { //Fecha es hoy
                LocalTime tiempoActual = LocalTime.now();
                if (tiempoActual.compareTo(c.getFuncion().getFin()) > 0) { //Ya acabo la funcion
                    compraRepository.actualizacionEstadoCompra("Asistido",persona.getId(), c.getId());
                }
            }
            listaComprasRevisadas.add(c);
        }

        List<Obragenero> listaGeneros = obraGeneroRepository.findAll();
        model.addAttribute("listaGeneros",listaGeneros);

        LinkedHashMap<Compra, String> duracionFuncioncompra = new LinkedHashMap<>();
        for (Compra c : listaCompras) {
            LocalTime inicio = c.getFuncion().getInicio();
            LocalTime fin = c.getFuncion().getFin();
            Duration duracion = Duration.between(inicio, fin);
            Long duracionHora = duracion.getSeconds()/(60*60);
            String duracionHoraStr = duracionHora.toString();
            if (duracionHora < 10) {
                duracionHoraStr = "0" + duracionHora.toString();
            }
            Long duracionMinutos = duracion.getSeconds()%60;
            String duracionMinutosStr = duracionMinutos.toString();
            if (duracionMinutos < 10) {
                duracionMinutosStr = "0" + duracionMinutos.toString();
            }
            duracionFuncioncompra.put(c, duracionHoraStr + ":" + duracionMinutosStr + "h");
        }

        model.addAttribute("historialCompras", listaComprasRevisadas);

        List<Pago> listaPagos = pagoRepository.findAll();
        LinkedHashMap<Pago, String> fechaHoraPago = new LinkedHashMap<>();
        for (Pago p : listaPagos) {
            String datetime = p.getFechapago().toString();
            String[] parts = datetime.split("T");
            String strDatetime = parts[0] + " " + parts[1];
            fechaHoraPago.put(p, strDatetime);
        }
        model.addAttribute("listaPagos", fechaHoraPago);

        //model.addAttribute("listaGeneros", compraGenero);
        model.addAttribute("duracionFuncioncompra", duracionFuncioncompra);

        //Habilitar boton de calificacion

        List<Calificaciones> listaCalificacionesUsuario = calificacionesRepository.listaCalificacionesUsuario(persona.getId());

        List<Obra> listaObrasCalificadas = new ArrayList<>();
        for (Calificaciones l : listaCalificacionesUsuario) {
            if (l.getObra() != null && !listaObrasCalificadas.contains(l.getObra())) {
                listaObrasCalificadas.add(l.getObra());
            }
        }
        List<Sede> listaSedesCalificadas = new ArrayList<>();
        for (Calificaciones l : listaCalificacionesUsuario) {
            if (l.getSede() != null && !listaSedesCalificadas.contains(l.getSede())) {
                listaSedesCalificadas.add(l.getSede());
            }
        }
        List<Persona> listaPersonasCalificadas = new ArrayList<>();
        for (Calificaciones l : listaCalificacionesUsuario) {
            if (l.getElenco() != null && !listaPersonasCalificadas.contains(l.getElenco())) {
                listaPersonasCalificadas.add(l.getElenco());
            }
        }

        LinkedHashMap<Compra, Boolean> calificacionHabilitada = new LinkedHashMap<>();
        for (Compra c : listaCompras) {

            boolean habilitado = false;
            boolean yaValidado = false;
            List<Funcionelenco> elenco = funcionElencoRepository.buscarFuncionElenco(c.getFuncion().getId());
            List<Persona> listaPersonasElenco = new ArrayList<>();
            for (Funcionelenco f : elenco) {
                listaPersonasElenco.add(f.getIdpersona());
            }
            for (Persona p : listaPersonasElenco) {
                if (!listaPersonasCalificadas.contains(p) && !yaValidado) {
                    habilitado = true;
                    yaValidado = true;
                }
            }
            if (!listaObrasCalificadas.contains(c.getFuncion().getIdobra()) && !yaValidado) {
                habilitado = true;
                yaValidado = true;
            }
            if (!listaSedesCalificadas.contains(c.getFuncion().getIdsala().getIdsede()) && !yaValidado) {
                habilitado = true;
                yaValidado = true;
            }
            calificacionHabilitada.put(c, habilitado);

        }
        model.addAttribute("calificacionHabilitada", calificacionHabilitada);
        return "usuario/carrito/historialComprasUsuario";
    }

    @GetMapping("/actualizarEstadoCompra")
    public String actualizarCompraHistorial(@RequestParam("id") Integer idCompra, HttpSession httpSession, RedirectAttributes redirectAttributes) {
        Persona persona = (Persona) httpSession.getAttribute("usuario");
        Optional<Compra> optionalCompra = compraRepository.findById(idCompra);
        if (optionalCompra.isPresent()) {
            Compra compra = optionalCompra.get();
            if (compra.getEstado().equals("Cancelado") || compra.getEstado().equals("Asistido")) {
                redirectAttributes.addFlashAttribute("noDejarCancelar", "Esta compra no puede ser cancelada");
            } else {

                LocalDate fechaIniFin = compra.getFuncion().getFecha();
                LocalTime horaIni = compra.getFuncion().getInicio();
                LocalTime horaFin = compra.getFuncion().getFin();
                LocalDateTime fechaHoraIni = LocalDateTime.of(fechaIniFin, horaIni);
                LocalDateTime fechaHoraFin = LocalDateTime.of(fechaIniFin, horaFin);
                LocalDateTime fechaHoraLimite = fechaHoraIni.minusHours(1);
                LocalDateTime now = LocalDateTime.now();
                if (now.isAfter(fechaHoraLimite) && now.isBefore(fechaHoraFin)) {
                    redirectAttributes.addFlashAttribute("noCancelarPorHora", "Ha pasado la hora límite para cancelar su compra");
                } else {
                    compraRepository.actualizacionEstadoCompra("Cancelado",persona.getId(), idCompra);
                    funcionRepository.actualizacionCantidadBoletos(optionalCompra.get().getCantidad() + funcionRepository.getById(optionalCompra.get().getFuncion().getId()).getStockentradas(), funcionRepository.getById(optionalCompra.get().getFuncion().getId()).getId());
                    pagoRepository.cancelarPago(idCompra);
                    redirectAttributes.addFlashAttribute("exitoCancelar", "Su compra ha sido cancelada exitosamente");
                }
            }
        }
        return "redirect:/historialPrueba";
    }

    @GetMapping("/calificarObra")
    public String calificarObra(@RequestParam("id") Integer idFuncion, @RequestParam("idCompra") Integer idCompra, Model model, HttpSession httpSession, RedirectAttributes redirectAttributes) {

        Optional<Compra> optionalCompra = compraRepository.findById(idCompra);
        if (optionalCompra.isPresent()) {
            Compra compra = optionalCompra.get();
            System.out.println();
            System.out.println();
            if (compra.getEstado().equals("Cancelado") || compra.getEstado().equals("Vigente")) {
                redirectAttributes.addFlashAttribute("noDejarCalificar", "Esta función no puede ser calificada");
                return "redirect:/historialPrueba";
            } else {
                //Evaluacion si el usuario en sesion ya
                Persona persona = (Persona) httpSession.getAttribute("usuario");
                List<Calificaciones> listaCalificacionesUsuario = calificacionesRepository.listaCalificacionesUsuario(persona.getId());

                List<Obra> listaObrasCalificadas = new ArrayList<>();
                for (Calificaciones c : listaCalificacionesUsuario) {
                    if (c.getObra() != null && !listaObrasCalificadas.contains(c.getObra())) {
                        listaObrasCalificadas.add(c.getObra());
                    }
                }
                List<Sede> listaSedesCalificadas = new ArrayList<>();
                for (Calificaciones c : listaCalificacionesUsuario) {
                    if (c.getSede() != null && !listaSedesCalificadas.contains(c.getSede())) {
                        listaSedesCalificadas.add(c.getSede());
                    }
                }
                List<Persona> listaPersonasCalificadas = new ArrayList<>();
                for (Calificaciones c : listaCalificacionesUsuario) {
                    if (c.getElenco() != null && !listaPersonasCalificadas.contains(c.getElenco())) {
                        System.out.println("Elenco: " + c.getElenco().getId());
                        listaPersonasCalificadas.add(c.getElenco());
                    }
                }

                boolean dejarCalificar = false;
                for (Obra o : listaObrasCalificadas) {
                    if (compra.getFuncion().getIdobra().getId() != o.getId()) {
                        dejarCalificar = true;
                    }
                }
                for (Sede s : listaSedesCalificadas) {
                    if (compra.getFuncion().getIdsala().getIdsede().getId() != s.getId()) {
                        dejarCalificar = true;
                    }
                }
                List<Funcionelenco> elencoFuncion = funcionElencoRepository.buscarFuncionElenco(compra.getFuncion().getId());
                for (Funcionelenco f : elencoFuncion) {
                    if (!listaPersonasCalificadas.contains(f.getIdpersona())) {
                        dejarCalificar = true;
                    }
                }
                if (dejarCalificar) {
                    //
                    model.addAttribute("listaObrasCalificadas", listaObrasCalificadas);
                    model.addAttribute("listaSedesCalificadas", listaSedesCalificadas);
                    model.addAttribute("listaPersonasCalificadas", listaPersonasCalificadas);

                    List<Funcionelenco> listaFuncionElenco = funcionElencoRepository.buscarFuncionElenco(idFuncion);
                    ArrayList<Persona> listaActores = new ArrayList<>();
                    ArrayList<Persona> listaDirectores = new ArrayList<>();

                    for (Funcionelenco f : listaFuncionElenco) {
                        if (f.getIdpersona().getIdrol().getId() == 5) {
                            listaActores.add(f.getIdpersona());
                        }
                        if (f.getIdpersona().getIdrol().getId() == 4) {
                            listaDirectores.add(f.getIdpersona());
                        }
                    }

                    List<Obragenero> listaObraGenero = obraGeneroRepository.findAll();

                    model.addAttribute("id", idFuncion);
                    model.addAttribute("listaObraGenero", listaObraGenero);
                    model.addAttribute("listaActores", listaActores);
                    model.addAttribute("listaDirectores", listaDirectores);
                    model.addAttribute("obra", funcionRepository.getById(idFuncion).getIdobra());
                    model.addAttribute("caratula", fotoRepository.caratulaDeObra(funcionRepository.getById(idFuncion).getIdobra().getId()));
                    model.addAttribute("fotosede", fotoRepository.caratulaDeSede(funcionRepository.getById(idFuncion).getIdsala().getIdsede().getId()));
                    model.addAttribute("sede", funcionRepository.getById(idFuncion).getIdsala().getIdsede());
                    model.addAttribute("fotosPersonas", fotoRepository.findAll());
                    return "usuario/calificacion";
                } else {
                    redirectAttributes.addFlashAttribute("noDejarCalificar", "Usted ya ha calificado esta función");
                    return "redirect:/historialPrueba";
                }
            }
        }
        return "redirect:/historialPrueba";
    }


    @PostMapping("/guardarCalificacion")
    public String guardarCalificacion(@RequestParam("obra") Integer calificacionObra,
                                      @RequestParam("sede") Integer calificacionSede,
                                      @RequestParam("actores") ArrayList<Integer> calificacionActores,
                                      @RequestParam("directores") ArrayList<Integer> calificacionDirectores,
                                      @RequestParam("id") Integer idFuncion,
                                      @RequestParam("idobra") Integer idobra,
                                      @RequestParam("idsede") Integer idsede,
                                      @RequestParam("descripcion") String descripcion,
                                      @RequestParam("descripcionsede") String descripcionsede,
                                      HttpSession httpSession,
                                      RedirectAttributes redirectAttributes){

        List<Funcionelenco> listaFuncionElenco = funcionElencoRepository.buscarFuncionElenco(idFuncion);
        System.out.println("Id funcion: " + idFuncion);
        System.out.println("Id Obra: " + idobra);
        System.out.println("Calificacion obra: " + calificacionObra);
        System.out.println("Calificacion sede: " + calificacionSede);
        for (int a : calificacionActores) {
            System.out.println("Calificacion Actor: " + a);
        }
        for (int a : calificacionDirectores) {
            System.out.println("Calificacion Director: " + a);
        }

        Persona usuario = (Persona) httpSession.getAttribute("usuario");


        //Validacion para verificar si ha completado la calificacion de la obra y sede + sus reseñas

//        if(descripcion.isBlank() || calificacionObra==0|| calificacionSede==0||descripcionsede.isBlank()){
//
//            redirectAttributes.addFlashAttribute("mensajeadvertencia", "Debe dejar una reseña y calificación tanto para la obra como la sede");
//            return "redirect:/calificarObra?id="+idFuncion;
//        }


        //guardamos la calificacion y reseña para la obra

        if(calificacionObra!=0 ){
            Calificaciones calfReseOBra= new Calificaciones();

            calfReseOBra.setCalificacion(calificacionObra);
            calfReseOBra.setComentario(descripcion);
            calfReseOBra.setEstado(1);
            calfReseOBra.setPersona(usuario);
            calfReseOBra.setObra(obraRepository.findById(idobra).get());
            calificacionesRepository.save(calfReseOBra);
        }

        //Guardar promedio calif obra en tabla obra
        Double promobra=calificacionesRepository.PromCalificacionOBra(idobra);
        Obra obraact =obraRepository.getById(idobra);
        obraact.setCalificacion(promobra);
        obraRepository.save(obraact);


        //guardamos la calificacion y reseña para la sede
        if(calificacionSede!=0 ){
            Calificaciones calfReseSede= new Calificaciones();

            calfReseSede.setCalificacion(calificacionSede);
            calfReseSede.setComentario(descripcionsede);
            calfReseSede.setEstado(1);
            calfReseSede.setPersona(usuario);
            calfReseSede.setSede(sedeRepository.findById(idsede).get());

            calificacionesRepository.save(calfReseSede);
        }

        //Guardar promedio calif sede en tabla sede
        Double promsede=calificacionesRepository.PromCalificacionSede(idsede);
        Sede sedactu =sedeRepository.getById(idsede);
        sedactu.setCalificacion(promsede);
        sedeRepository.save(sedactu);

        //listas de actores y directores de la funcion
        ArrayList<Persona> listaActores = new ArrayList<>();
        ArrayList<Persona> listaDirectores = new ArrayList<>();

        for (Funcionelenco f : listaFuncionElenco) {
            if (f.getIdpersona().getIdrol().getId() == 5) {
                listaActores.add(f.getIdpersona());
            }
            if (f.getIdpersona().getIdrol().getId() == 4) {
                listaDirectores.add(f.getIdpersona());
            }
        }

        System.out.println(calificacionActores.size());
        //guardamos la calificacion para los actores
        int i=0;
        for (Persona act : listaActores) {
            Calificaciones calificaciones=new Calificaciones();
            if(calificacionActores.get(i)!=0){
                calificaciones.setCalificacion(calificacionActores.get(i));
                calificaciones.setPersona(usuario);
                calificaciones.setEstado(1);
                calificaciones.setElenco(act);
                calificacionesRepository.save(calificaciones);
                //Guardar promedio calif actor en tabla persona
                Double actorprom=calificacionesRepository.PromCalificacionPersonaElenco(act.getId());
                Persona actoract =personaRepository.getById(act.getId());
                actoract.setCalificacion(actorprom);
                personaRepository.save(actoract);

            }
            i++;
        }





//        for (int calificacionDirector : calificacionDirectores) {
//            System.out.println("Calificacion Director: " + calificacionDirector);
//        }
        //guardamos la calificacion para los directores
        int j=0;
        for (Persona dir : listaDirectores) {
            Calificaciones calificaciones=new Calificaciones();
            if(calificacionDirectores.get(j)!=0){
                calificaciones.setCalificacion(calificacionDirectores.get(j));
                calificaciones.setPersona(usuario);
                calificaciones.setEstado(1);
                calificaciones.setElenco(dir);
                calificacionesRepository.save(calificaciones);
                //Guardar promedio calif director en tabla persona
                Double direcprom=calificacionesRepository.PromCalificacionPersonaElenco(dir.getId());
                Persona diract =personaRepository.getById(dir.getId());
                diract.setCalificacion(direcprom);
                personaRepository.save(diract);
            }
            j++;
        }



        redirectAttributes.addFlashAttribute("mensajeExito", "Calificación de la función realizada");

        return "redirect:/historialPrueba";
    }



    @GetMapping("/qr")
    public String qr(Model model, @RequestParam("codigo") String codigo){

        List<Pago> listaPagos = pagoRepository.listaPago(codigo);
        boolean mostrarPago = true;
        double pago = 0;
        for (Pago p : listaPagos) {
            pago = pago + p.getIdcompra().getMontoTotal();
            if (p.getEstado() == 0) {
                mostrarPago = false;
            }
        }
        model.addAttribute("Pago", listaPagos.get(0));
        model.addAttribute("listaPagos", listaPagos);
        model.addAttribute("mostrarPago", mostrarPago);
        model.addAttribute("total", pago);
        model.addAttribute("listaTarjetas", tarjetaRepository.findAll());
        return "usuario/qr";
    }





    /*-------------------------------------------------------------------------------------------------------------------------------------------------------*/

     /*
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



                } else {
                    redirectAttributes.addFlashAttribute("mensajeFaltaEdad", "La funcion tiene restriccion de edad");
                    return "redirect:/detallesObra?Obra=" + obra.getId();

                }
            } else {
                redirectAttributes.addFlashAttribute("mensajeNoHayStock", "Ya no hay stock disponible");
                return "redirect:/detallesObra?Obra=" + obra.getId();
            }
        }

    }*/

    /*@GetMapping("/detallesObra")
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
    @GetMapping("/compraprocess")
    public String procesoDeCompra(@ModelAttribute("datosTarjeta") DatosTarjeta datosTarjeta, HttpSession session) {
        Compra compraEnProceso = (Compra) session.getAttribute("compraEnProceso");
        if (compraEnProceso == null) {
            return "redirect:/cartelera";
        } else {
            return "usuario/pagoUsuario";
        }

    }


        FUNCION D EPRUEBA PARA RECEPCION DE VALORES DEL FORM DE COMPRA EN OBRADETALLES
        @PostMapping("/compra")
        String compra(@RequestParam("funcion") Integer funcion, @RequestParam("cantidadTotalBoletos") Integer cantidadTotalBoletos) {
        System.out.println("Id Funcion recibida: " + funcion);
        System.out.println("Cantidad boletos:" + cantidadTotalBoletos);
       return "usuario/obras/carteleraObras";
    }

    @GetMapping("/pagoPrueba")
    String pagoPrueba() { return "usuario/pagoUsuario";}

    @GetMapping("/carritoPrueba")
    String carritoPrueba() {
        return "usuario/carrito/carritoComprasUsuario";
    }*/


}
