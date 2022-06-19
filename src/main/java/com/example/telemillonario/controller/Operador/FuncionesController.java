package com.example.telemillonario.controller.Operador;

import com.ctc.wstx.shaded.msv_core.util.Uri;
import com.example.telemillonario.dto.BalanceDto;
import com.example.telemillonario.dto.EstadisticaFuncionDto;
import com.example.telemillonario.entity.*;
import com.example.telemillonario.repository.*;
import com.example.telemillonario.service.FileService;
import com.example.telemillonario.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Path;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Controller
@RequestMapping("/operador/funciones")
public class FuncionesController {

    @Autowired
    PersonaRepository personaRepository;

    @Autowired
    SalaRepository salaRepository;

    @Autowired
    FuncionRepository funcionRepository;

    @Autowired
    FuncionElencoRepository funcionElencoRepository;

    @Autowired
    FileService fileService;

    @Autowired
    FotoRepository fotoRepository;

    @Autowired
    GeneroRepository generoRepository;

    @Autowired
    ObraGeneroRepository obraGeneroRepository;

    @Autowired
    ObraRepository obraRepository;

    @Autowired
    ReporteService reporteService;

    float funcionesporpagina = 6;

    @GetMapping(value = {"", "/", "/lista"})
    public String listadoFunciones(Model model, @RequestParam(value = "pag", required = false) String pag, HttpSession session) {

        int pagina;

        try {
            pagina = Integer.parseInt(pag);
        } catch (Exception e) {
            pagina = 0;
        }

        Persona persona = (Persona) session.getAttribute("usuario");

        List<Funcion> listFunciones = funcionRepository.buscarFuncionesPorSede(persona.getIdsede().getId(), (int) funcionesporpagina * pagina, (int) funcionesporpagina);
        List<Foto> listFotosObra = fotoRepository.buscarFotoObrasPorSede(persona.getIdsede().getId());

        HashMap<Funcion, Foto> funcionesConFoto = new HashMap<>();

        for (Funcion funcion : listFunciones) {
            for (Foto foto : listFotosObra) {
                if (foto.getIdobra() == funcion.getIdobra()) {
                    funcionesConFoto.put(funcion, foto);
                    break;
                }
            }
        }

        int cantFunc = fotoRepository.contarFunciones(persona.getIdsede().getId());

        model.addAttribute("funcionesConFoto", funcionesConFoto);

        model.addAttribute("pagActual", pagina);
        model.addAttribute("pagTotal", (int) Math.ceil(cantFunc / funcionesporpagina));
        return "Operador/index";
    }

    @GetMapping(value = {"/crear"})
    public String programarFuncionesForm(@ModelAttribute("funcion") Funcion funcion, Model model, HttpSession session) {

        // Listas para los selectores
        model.addAttribute("listObras", obraRepository.findAll());
        model.addAttribute("listActores", personaRepository.listarActores("", 0, 10000000));
        model.addAttribute("listDirectores", personaRepository.listarDirectores());
        model.addAttribute("listGeneros", generoRepository.findAll());
        model.addAttribute("fechaactual", LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));

        // Listas vacias para evitar errores
        ArrayList<Integer> listaVacia = new ArrayList<>();
        model.addAttribute("generosFuncion", listaVacia);
        model.addAttribute("actoresFuncion", listaVacia);
        model.addAttribute("directoresFuncion", listaVacia);

        //listado de salas para la Sede del Operador
        Persona persona = (Persona) session.getAttribute("usuario");
        model.addAttribute("listaSalasporSede", salaRepository.buscarSalasTotal(persona.getIdsede().getId(), 1));
        return "Operador/crearFuncion";
    }

    private void retornarValores(Model model, Funcion funcion, Persona persona) {

        // Variables para hacer más entendible el código
        List<Obra> listObras = obraRepository.findAll();
        List<Persona> listActores = personaRepository.listarActores("", 0, 100000);
        List<Persona> listDirectores = personaRepository.listarDirectores();
        List<Sala> listaSalasporSede = salaRepository.buscarSalasTotal(persona.getIdsede().getId(), 1);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        // Se regresan nuevamente los elementos de las listas
        model.addAttribute("funcion", funcion);
        model.addAttribute("listObras", listObras);
        model.addAttribute("listActores", listActores);
        model.addAttribute("listDirectores", listDirectores);
        model.addAttribute("fechaactual", now);
        model.addAttribute("listaSalasporSede", listaSalasporSede);
    }

    private void retornarValoresYSelect(Model model, Funcion funcion, Persona persona,
                                        String[] idactor, String[] iddirector,
                                        String duracion, String fechamasinicio) {

        this.retornarValores(model, funcion, persona);

        // Se regresan elementos ya seleccionados
        model.addAttribute("actoresFuncion", idactor);
        model.addAttribute("directoresFuncion", iddirector);
        model.addAttribute("duracion", duracion);
        model.addAttribute("fechamasinicio", fechamasinicio);
    }

    @GetMapping(value = {"/editar"})
    public String editarFuncionesForm(@ModelAttribute("funcion") Funcion funcion,
                                      @RequestParam("idfuncion") String idStr,
                                      Model model, HttpSession session,
                                      RedirectAttributes attr) {

        // Se verifica que el ID sea un número
        int idfuncion = 0;
        try {
            idfuncion = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            attr.addFlashAttribute("err", "El ID ingresado es inválido");
            return "redirect:/operador/funciones/lista";
        }

        // Se verifica si el ID de la Función corresponde a una real
        Optional<Funcion> funcionEnDB = funcionRepository.findById(idfuncion);
        Persona persona = (Persona) session.getAttribute("usuario");

        // Retorna el formulario
        if (funcionEnDB.isPresent()) {

            // Datos de la funcion
            funcion = funcionEnDB.get();
            long duracion = funcion.getInicio().until(funcion.getFin(), ChronoUnit.MINUTES);
            String fechamasinicio = funcion.getFecha().toString() + "T" + funcion.getInicio().toString();

            // Listas para los select
            retornarValores(model, funcion, persona);

            // Lista de los elementos YA seleccionados
            model.addAttribute("actoresFuncion", personaRepository.actoresPorFuncion(idfuncion));
            model.addAttribute("directoresFuncion", personaRepository.directoresPorFuncion(idfuncion));

            // Tiempo
            model.addAttribute("duracion", duracion);
            model.addAttribute("fechamasinicio", fechamasinicio);

            return "Operador/crearFuncion";
        } else {
            attr.addFlashAttribute("err", "La función que busca no existe");
            return "redirect:/operador/funciones/lista";
        }

    }

    @PostMapping("/guardar")
    public String guardarFuncion(@ModelAttribute("funcion") @Valid Funcion funcion, BindingResult bindingResult,
                                 Model model, HttpSession session, RedirectAttributes attr,
                                 @RequestParam(value = "idobra") String idObraStr,
                                 @RequestParam(value = "fechamasinicio") String fechamasinicio,
                                 @RequestParam(value = "duracion") String duracion,
                                 @RequestParam(value = "idactor") String[] idactor,
                                 @RequestParam(value = "iddirector") String[] iddirector) {

        Persona persona = (Persona) session.getAttribute("usuario");
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        //-----------------------------------------------
        //    Validación de Datos Ingresados
        //-----------------------------------------------
        int duracionInt = 0;
        try {
            duracionInt = Integer.parseInt(duracion);
        } catch (NumberFormatException e) {
        }

        if (bindingResult.hasErrors() || duracionInt <= 0) {

            retornarValoresYSelect(model, funcion, persona, idactor, iddirector, duracion, fechamasinicio);

            if (duracionInt <= 0) {
                model.addAttribute("msgduracion", "La duración no es válida");
            }

            return "Operador/crearFuncion";
        }

        //-----------------------------------------------
        //          Validación de la Obra
        //-----------------------------------------------
        Obra obraFuncion;
        try {
            int idObra = Integer.parseInt(idObraStr);
            Optional<Obra> obra = obraRepository.findById(idObra);

            if (!obra.isPresent()) {
                throw new NumberFormatException();
            }
            obraFuncion = obra.get();

        } catch (NumberFormatException e) {

            // Retorna los valores ingresados
            retornarValoresYSelect(model, funcion, persona, idactor, iddirector, duracion, fechamasinicio);

            model.addAttribute("msgObra", "La obra ingresada no es válida");

            return "Operador/crearFuncion";
        }

        //-----------------------------------------------
        //          Validación de la Fecha
        //-----------------------------------------------

        LocalDateTime fecha;
        try {
            fecha = LocalDateTime.parse(fechamasinicio);

            if (now.isAfter(fecha)) {
                throw new Exception();
            }
        } catch (Exception e) {

            // Retorna los valores ingresados
            retornarValoresYSelect(model, funcion, persona, idactor, iddirector, duracion, fechamasinicio);

            model.addAttribute("msgfecha", "La fecha no es válida");

            return "Operador/crearFuncion";
        }

        //-----------------------------------------------
        //          Guardar datos de la Función
        //-----------------------------------------------

        //fecha
        LocalDate dia = fecha.toLocalDate();
        //hora inicio
        LocalTime hora = fecha.toLocalTime();

        // Se guarda la fecha
        funcion.setEstado(1);
        funcion.setFecha(dia);
        funcion.setInicio(hora);
        funcion.setFin(hora.plusMinutes(duracionInt));
        funcion.setIdobra(obraFuncion);

        // Se guardan los cambios a la funcion
        funcion = funcionRepository.save(funcion);

        //-----------------------------------------------
        //      Guardar datos del Elenco y Género
        //-----------------------------------------------

        // Se obtiene el elenco de la función
        List<Funcionelenco> elencoEnDB = funcionElencoRepository.buscarFuncionElenco(funcion.getId());
        List<Integer> elencoSeleccionado = new ArrayList<>();

        // Se comparan los datos de elenco ingresados con los de la DB
        int maxSeleccionados = Math.max(idactor.length, iddirector.length);

        for (int ii = 0; ii < maxSeleccionados; ii++) {

            boolean actorCoincide = false;
            boolean directorCoincide = false;

            for (int jj = 0; jj < elencoEnDB.size(); jj++) {

                // Valida los miembros del elenco seleccionados
                if (ii < idactor.length && idactor[ii].equals(elencoEnDB.get(jj).getIdpersona().getId().toString())) {
                    elencoSeleccionado.add(elencoEnDB.get(jj).getIdpersona().getId());
                    actorCoincide = true;

                } else if (ii < iddirector.length && iddirector[ii].equals(elencoEnDB.get(jj).getIdpersona().getId().toString())) {
                    elencoSeleccionado.add(elencoEnDB.get(jj).getIdpersona().getId());
                    directorCoincide = true;
                }
            }

            // Si un miembro del elenco no está en DB, se agrega
            if (ii < iddirector.length && !directorCoincide) {
                Funcionelenco funcelen = new Funcionelenco();
                int idsdictint = Integer.parseInt(iddirector[ii]);
                funcelen.setIdpersona(personaRepository.findById(idsdictint).get());
                funcelen.setIdfuncion(funcion);
                funcelen.setEstado(1);
                funcionElencoRepository.save(funcelen);

            } else if (ii < idactor.length && !actorCoincide) {
                Funcionelenco funcelen = new Funcionelenco();
                int idsdictint = Integer.parseInt(idactor[ii]);
                funcelen.setIdpersona(personaRepository.findById(idsdictint).get());
                funcelen.setIdfuncion(funcion);
                funcelen.setEstado(1);
                funcionElencoRepository.save(funcelen);
            }
        }

        // Si un miembro del elenco ya no ha sido seleccionado, se elimina
        for (Funcionelenco fe : elencoEnDB) {
            int id = fe.getIdpersona().getId();
            if (!elencoSeleccionado.contains(id)) {
                funcionElencoRepository.deleteById(fe.getId());
            }
        }

        attr.addFlashAttribute("msg", "Función Guardada Exitosamente");
        return "redirect:/operador/funciones";
    }


    @PostMapping("/buscar")
    public String buscarFuncion(Model model, @RequestParam("parametro") String parametro, RedirectAttributes attr, @RequestParam(value = "pag", required = false) String pag, HttpSession session) {

        try {
            if (parametro.equals("")) { // verifica que no esté vacío
                attr.addFlashAttribute("msg", "La búsqueda no debe estar vacía.");
                return "redirect:/operador/funciones";
            } else {
                model.addAttribute("parametro", parametro);
                parametro = parametro.toLowerCase();

                int pagina;

                try {
                    pagina = Integer.parseInt(pag);
                } catch (Exception e) {
                    pagina = 0;
                }

                Persona persona = (Persona) session.getAttribute("usuario");

                List<Foto> listfuncfoto = fotoRepository.buscarFotoFuncionesPorNombre(persona.getIdsede().getId(), parametro, (int) funcionesporpagina * pagina, (int) funcionesporpagina);
                List<Foto> listfunctotal = fotoRepository.buscarFuncionesParaContarPorNombre(persona.getIdsede().getId(), parametro);
                int cantFunc = listfunctotal.size();

                model.addAttribute("listfunc", listfuncfoto);

                model.addAttribute("pagActual", pagina);
                model.addAttribute("pagTotal", (int) Math.ceil(cantFunc / funcionesporpagina));

                return "Operador/index";
            }
        } catch (Exception e) {
            attr.addFlashAttribute("msg", "La búsqueda no debe contener caracteres extraños.");
            return "redirect:/operador/funciones";
        }
    }


    @GetMapping("/borrar")
    public String borrarFuncion(@RequestParam("idfuncion") String idFuncionStr,
                                Model model, RedirectAttributes attr) {

        try {
            int idFuncion = Integer.parseInt(idFuncionStr);

            Optional<Funcion> funcion = funcionRepository.findById(idFuncion);

            if (!funcion.isPresent()) {
                throw new NumberFormatException();
            }

            Funcion funcionABorrar = funcion.get();
            funcionABorrar.setEstado(0);
            funcionRepository.save(funcionABorrar);

            attr.addFlashAttribute("msg1", "Funcion borrada exitosamente");

        } catch (NumberFormatException e) {
            attr.addFlashAttribute("msg", "El ID de la función es inválido");
        }
        return "redirect:/operador/funciones";
    }

    @GetMapping(value = "/reportes")
    public String obtenerEstadisticas(@RequestParam("periodicidad") Optional<String> opt_periodicidad, @RequestParam("periodo") Optional<String> opt_periodo, HttpSession session, Model model, RedirectAttributes attr) {
        Persona persona = (Persona) session.getAttribute("usuario");
        //Se obtiene la sede desde donde se realizarán todas las consultas para el reporte
        int sede = persona.getIdsede().getId();
        System.out.println("Id sede" + sede);
        //suponiendo que no se han enviado la periodicidad y el periodo
        if (!opt_periodo.isPresent() && !opt_periodicidad.isPresent()) {
            model.addAttribute("funcionMasVista", funcionRepository.obtenerFuncionMasVistaxSede(sede));
            model.addAttribute("funcionMenosVista", funcionRepository.obtenerFuncionMenosVistaxSede(sede));
            model.addAttribute("funcionMejorCalificada", funcionRepository.obtenerFuncionMejorCalificadaxSede(sede));
            model.addAttribute("funcionesPorcentajeAsistencia", funcionRepository.obtenerFuncionesxAsistenciaxSede(sede));
            model.addAttribute("actoresMejorCalificados", funcionRepository.obtenerActoresMejorCalificadosxSede(sede));
            model.addAttribute("directoresMejorCalificados", funcionRepository.obtenerDirectoresMejorCalificadosxSede(sede));
        } else {
            if (opt_periodicidad.get().equalsIgnoreCase("Mensual")) {
                Optional<EstadisticaFuncionDto> funcionMasVistaxMes = null;
                Optional<EstadisticaFuncionDto> funcionMenosVistaxMes = null;
                Optional<EstadisticaFuncionDto> funcionMejorCalificadaxMes = null;
                Optional<List<EstadisticaFuncionDto>> funcionesVistasxMes = null;
                switch (opt_periodo.get()) {
                    case "Enero":
                        funcionMasVistaxMes = funcionRepository.obtenerFuncionMasVistaxMesxSede(sede, 1);
                        funcionMenosVistaxMes = funcionRepository.obtenerFuncionMenosVistaxMesxSede(sede, 1);
                        funcionMejorCalificadaxMes = funcionRepository.obtenerFuncionMejorCalificadaxMesxSede(sede, 1);
                        funcionesVistasxMes = funcionRepository.obtenerFuncionesMejorCalificadasxMesxSede(sede, 1);
                        break;
                    case "Febrero":
                        funcionMasVistaxMes = funcionRepository.obtenerFuncionMasVistaxMesxSede(sede, 2);
                        funcionMenosVistaxMes = funcionRepository.obtenerFuncionMenosVistaxMesxSede(sede, 2);
                        funcionMejorCalificadaxMes = funcionRepository.obtenerFuncionMejorCalificadaxMesxSede(sede, 2);
                        funcionesVistasxMes = funcionRepository.obtenerFuncionesMejorCalificadasxMesxSede(sede, 2);
                        break;
                    case "Marzo":
                        funcionMasVistaxMes = funcionRepository.obtenerFuncionMasVistaxMesxSede(sede, 3);
                        funcionMenosVistaxMes = funcionRepository.obtenerFuncionMenosVistaxMesxSede(sede, 3);
                        funcionMejorCalificadaxMes = funcionRepository.obtenerFuncionMejorCalificadaxMesxSede(sede, 3);
                        funcionesVistasxMes = funcionRepository.obtenerFuncionesMejorCalificadasxMesxSede(sede, 3);
                        break;
                    case "Abril":
                        funcionMasVistaxMes = funcionRepository.obtenerFuncionMasVistaxMesxSede(sede, 4);
                        funcionMenosVistaxMes = funcionRepository.obtenerFuncionMenosVistaxMesxSede(sede, 4);
                        funcionMejorCalificadaxMes = funcionRepository.obtenerFuncionMejorCalificadaxMesxSede(sede, 4);
                        funcionesVistasxMes = funcionRepository.obtenerFuncionesMejorCalificadasxMesxSede(sede, 4);
                        break;
                    case "Mayo":
                        funcionMasVistaxMes = funcionRepository.obtenerFuncionMasVistaxMesxSede(sede, 5);
                        funcionMenosVistaxMes = funcionRepository.obtenerFuncionMenosVistaxMesxSede(sede, 5);
                        funcionMejorCalificadaxMes = funcionRepository.obtenerFuncionMejorCalificadaxMesxSede(sede, 5);
                        funcionesVistasxMes = funcionRepository.obtenerFuncionesMejorCalificadasxMesxSede(sede, 5);
                        break;
                    case "Junio":
                        funcionMasVistaxMes = funcionRepository.obtenerFuncionMasVistaxMesxSede(sede, 6);
                        funcionMenosVistaxMes = funcionRepository.obtenerFuncionMenosVistaxMesxSede(sede, 6);
                        funcionMejorCalificadaxMes = funcionRepository.obtenerFuncionMejorCalificadaxMesxSede(sede, 6);
                        funcionesVistasxMes = funcionRepository.obtenerFuncionesMejorCalificadasxMesxSede(sede, 6);
                        break;
                    case "Julio":
                        funcionMasVistaxMes = funcionRepository.obtenerFuncionMasVistaxMesxSede(sede, 7);
                        funcionMenosVistaxMes = funcionRepository.obtenerFuncionMenosVistaxMesxSede(sede, 7);
                        funcionMejorCalificadaxMes = funcionRepository.obtenerFuncionMejorCalificadaxMesxSede(sede, 7);
                        funcionesVistasxMes = funcionRepository.obtenerFuncionesMejorCalificadasxMesxSede(sede, 7);
                        break;
                    case "Agosto":
                        funcionMasVistaxMes = funcionRepository.obtenerFuncionMasVistaxMesxSede(sede, 8);
                        funcionMenosVistaxMes = funcionRepository.obtenerFuncionMenosVistaxMesxSede(sede, 8);
                        funcionMejorCalificadaxMes = funcionRepository.obtenerFuncionMejorCalificadaxMesxSede(sede, 8);
                        funcionesVistasxMes = funcionRepository.obtenerFuncionesMejorCalificadasxMesxSede(sede, 8);
                        break;
                    case "Setiembre":
                        funcionMasVistaxMes = funcionRepository.obtenerFuncionMasVistaxMesxSede(sede, 9);
                        funcionMenosVistaxMes = funcionRepository.obtenerFuncionMenosVistaxMesxSede(sede, 9);
                        funcionMejorCalificadaxMes = funcionRepository.obtenerFuncionMejorCalificadaxMesxSede(sede, 9);
                        funcionesVistasxMes = funcionRepository.obtenerFuncionesMejorCalificadasxMesxSede(sede, 9);
                        break;
                    case "Octubre":
                        funcionMasVistaxMes = funcionRepository.obtenerFuncionMasVistaxMesxSede(sede, 10);
                        funcionMenosVistaxMes = funcionRepository.obtenerFuncionMenosVistaxMesxSede(sede, 10);
                        funcionMejorCalificadaxMes = funcionRepository.obtenerFuncionMejorCalificadaxMesxSede(sede, 10);
                        funcionesVistasxMes = funcionRepository.obtenerFuncionesMejorCalificadasxMesxSede(sede, 10);
                        break;
                    case "Noviembre":
                        funcionMasVistaxMes = funcionRepository.obtenerFuncionMasVistaxMesxSede(sede, 11);
                        funcionMenosVistaxMes = funcionRepository.obtenerFuncionMenosVistaxMesxSede(sede, 11);
                        funcionMejorCalificadaxMes = funcionRepository.obtenerFuncionMejorCalificadaxMesxSede(sede, 11);
                        funcionesVistasxMes = funcionRepository.obtenerFuncionesMejorCalificadasxMesxSede(sede, 11);
                        break;
                    case "Diciembre":
                        funcionMasVistaxMes = funcionRepository.obtenerFuncionMasVistaxMesxSede(sede, 12);
                        funcionMenosVistaxMes = funcionRepository.obtenerFuncionMenosVistaxMesxSede(sede, 12);
                        funcionMejorCalificadaxMes = funcionRepository.obtenerFuncionMejorCalificadaxMesxSede(sede, 12);
                        funcionesVistasxMes = funcionRepository.obtenerFuncionesMejorCalificadasxMesxSede(sede, 12);
                        break;
                    default:
                        System.out.println("Ha ocurrido un problema");
                        attr.addFlashAttribute("msg", "Se envió un mes inválido");
                        return "redirect:/operador/funciones/reportes";
                }
                if (funcionMasVistaxMes.isPresent() && funcionMenosVistaxMes.isPresent() && funcionMejorCalificadaxMes.isPresent() && funcionesVistasxMes.isPresent()) {
                    //si se encontró todo lo solicitado segun mes
                    model.addAttribute("funcionMasVista", funcionMasVistaxMes.get());
                    model.addAttribute("funcionMenosVista", funcionMenosVistaxMes.get());
                    model.addAttribute("funcionMejorCalificada", funcionMejorCalificadaxMes.get());
                    model.addAttribute("funcionesPorcentajeAsistencia", funcionesVistasxMes.get());
                    model.addAttribute("actoresMejorCalificados", funcionRepository.obtenerActoresMejorCalificadosxSede(sede));
                    model.addAttribute("directoresMejorCalificados", funcionRepository.obtenerDirectoresMejorCalificadosxSede(sede));
                } else {
                    //En caso no haya encontrado nada
                    attr.addFlashAttribute("msg", "No se encontraron estadisticas de las funciones para el mes solicitado");
                    return "redirect:/operador/funciones/reportes";
                }
            } else if (opt_periodicidad.get().equalsIgnoreCase("Anual")) {
                try {
                    int anio = Integer.parseInt(opt_periodo.get());
                    Optional<EstadisticaFuncionDto> funcionMasVistaxAnio = funcionRepository.obtenerFuncionMasVistaxAnioxSede(sede, anio);
                    Optional<EstadisticaFuncionDto> funcionMenosVistaxAnio = funcionRepository.obtenerFuncionMenosVistaxAñoxSede(sede, anio);
                    Optional<EstadisticaFuncionDto> funcionMejorCalificadaxAnio = funcionRepository.obtenerFuncionMejorCalificadaxAnioxSede(sede, anio);
                    Optional<List<EstadisticaFuncionDto>> funcionesVistasxAnio = funcionRepository.obtenerFuncionesMejorCalificadasxAnioxSede(sede, anio);
                    if (funcionesVistasxAnio.isPresent() && funcionMasVistaxAnio.isPresent() && funcionMejorCalificadaxAnio.isPresent() && funcionMenosVistaxAnio.isPresent()) {
                        //si se encontró todo lo solicitado segun año
                        model.addAttribute("funcionMasVista", funcionMasVistaxAnio.get());
                        model.addAttribute("funcionMenosVista", funcionMenosVistaxAnio.get());
                        model.addAttribute("funcionMejorCalificada", funcionMejorCalificadaxAnio.get());
                        model.addAttribute("funcionesPorcentajeAsistencia", funcionesVistasxAnio.get());
                        model.addAttribute("actoresMejorCalificados", funcionRepository.obtenerActoresMejorCalificadosxSede(sede));
                        model.addAttribute("directoresMejorCalificados", funcionRepository.obtenerDirectoresMejorCalificadosxSede(sede));
                    } else {
                        //en caso no se encontro nada
                        attr.addFlashAttribute("msg", "No se encontraron estadisticas de las funciones para el año solicitado");
                        return "redirect:/operador/funciones/reportes";
                    }
                } catch (Exception e) {
                    //en caso haya un error en parseo
                    attr.addFlashAttribute("msg", "Se envió un año con formato incorrecto");
                    return "redirect:/operador/funciones/reportes";
                }
            } else {
                //Se envio una periodicidad inválida
                attr.addFlashAttribute("msg", "Se envió una periodicad inválida");
                return "redirect:/operador/funciones/reportes";
            }
        }
        return "/Operador/reportes";
    }

    @GetMapping(value = "/exportar",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InputStreamResource> generarReporte(@RequestParam("periodicidad") Optional<String> opt_periodicidad, @RequestParam("periodo") Optional<String> opt_periodo, HttpSession session) throws IOException {
        Persona persona = (Persona) session.getAttribute("usuario");
        //Se obtiene la sede desde donde se realizarán todas las consultas para el reporte
        int sede = persona.getIdsede().getId();
        String name = "reporte_"+LocalDate.now().toString()+".xlsx";
        File file = null;
        if (!opt_periodo.isPresent() && !opt_periodicidad.isPresent()) {
            //en el supuesto en el que no se tenga nada
            List<BalanceDto> balancexSede = funcionRepository.obtenerBalancexSede(sede);
            file = reporteService.generarReporte(opt_periodicidad,opt_periodo,balancexSede,name);
        } else {
            if (opt_periodicidad.get().equalsIgnoreCase("Mensual")) {
                Optional<List<BalanceDto>> balancexSedexMes = null;
                switch (opt_periodo.get()) {
                    case "Enero":
                        balancexSedexMes = funcionRepository.obtenerBalancexSedexMes(sede,1);
                        break;
                    case "Febrero":
                        balancexSedexMes = funcionRepository.obtenerBalancexSedexMes(sede,2);
                        break;
                    case "Marzo":
                        balancexSedexMes = funcionRepository.obtenerBalancexSedexMes(sede,3);
                        break;
                    case "Abril":
                        balancexSedexMes = funcionRepository.obtenerBalancexSedexMes(sede,4);
                        break;
                    case "Mayo":
                        balancexSedexMes = funcionRepository.obtenerBalancexSedexMes(sede,5);
                        break;
                    case "Junio":
                        balancexSedexMes = funcionRepository.obtenerBalancexSedexMes(sede,6);
                        break;
                    case "Julio":
                        balancexSedexMes = funcionRepository.obtenerBalancexSedexMes(sede,7);
                        break;
                    case "Agosto":
                        balancexSedexMes = funcionRepository.obtenerBalancexSedexMes(sede,8);
                        break;
                    case "Setiembre":
                        balancexSedexMes = funcionRepository.obtenerBalancexSedexMes(sede,9);
                        break;
                    case "Octubre":
                        balancexSedexMes = funcionRepository.obtenerBalancexSedexMes(sede,10);
                        break;
                    case "Noviembre":
                        balancexSedexMes = funcionRepository.obtenerBalancexSedexMes(sede,11);
                        break;
                    case "Diciembre":
                        balancexSedexMes = funcionRepository.obtenerBalancexSedexMes(sede,12);
                        break;
                    default:
                        System.out.println("Ha ocurrido un problema");
                }
                if (balancexSedexMes.isPresent() && balancexSedexMes!=null) {
                    //se ha encontrado algo para poder elaborar el reporte
                     file = reporteService.generarReporte(opt_periodicidad,opt_periodo,balancexSedexMes.get(),name);
                } else {
                    //se enviará una lista vacia para elaborar el reporte
                    List<BalanceDto> balancexMesxSede = new ArrayList<>();
                     file = reporteService.generarReporte(opt_periodicidad,opt_periodo,balancexMesxSede,name);
                }
            } else if (opt_periodicidad.get().equalsIgnoreCase("Anual")) {
                try {
                    int anio = Integer.parseInt(opt_periodo.get());
                    Optional<List<BalanceDto>> balancexAnioxSede = funcionRepository.obtenerBalancexSedexAnio(sede,anio);
                    if (balancexAnioxSede.isPresent()) {
                         file = reporteService.generarReporte(opt_periodicidad,opt_periodo,balancexAnioxSede.get(),name);
                    } else {
                        //se enviará una lista vacía para eleborar el reporte
                        List<BalanceDto> balancexSedexAnio = new ArrayList<>();
                         file = reporteService.generarReporte(opt_periodicidad,opt_periodo,balancexSedexAnio,name);
                    }
                } catch (Exception e) {
                    //se enviará una lista vacía para eleborar el reporte
                    List<BalanceDto> balancexAnioxSedeAux = new ArrayList<>();
                     file = reporteService.generarReporte(opt_periodicidad,opt_periodo,balancexAnioxSedeAux,name);
                }
            } else {
                //se envio una periodicidad invalida
                List<BalanceDto> balancexSedeAux = new ArrayList<>();
                 file = reporteService.generarReporte(opt_periodicidad,opt_periodo,balancexSedeAux,name);
            }
        }
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + name + "\"")
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}


