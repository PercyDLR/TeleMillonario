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
    CompraRepository compraRepository;

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

        LinkedHashMap<Funcion, Foto> funcionesConFoto = new LinkedHashMap<>();

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
            //Verificamos si la fecha de la funcion por lo menos es el dia actual para poder editar
            LocalDate Today = LocalDate.now();
            if (funcion.getFecha().compareTo(Today) < 0){
                attr.addFlashAttribute("err","No se puede editar una función cuya fecha ya paso");
                return "redirect:/operador/funciones";
            }
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
        //    Validación de funcion con misma hora inicio en una sala
        //-----------------------------------------------
        Persona usuarioSesion = (Persona) session.getAttribute("usuario");
        int idsede=usuarioSesion.getIdsede().getId();
        //hora inicio
        LocalTime horavalidar = fecha.toLocalTime();

        List<Funcion> funcionesSala = funcionRepository.buscarFuncionesPorSedeySala(idsede,funcion.getIdsala().getId());
        for (Funcion func: funcionesSala){
            int value= horavalidar.compareTo(func.getInicio());

            LocalTime horafinfuncion =func.getFin();
            //validamos si la hora de la funcion esta dentro del rango de duracion de una funcion existente en una sala
            if (value>0){

                int value1= horavalidar.compareTo(func.getFin());

                if(value1==-1){
                    // Retorna los valores ingresados
                    retornarValoresYSelect(model, funcion, persona, idactor, iddirector, duracion, fechamasinicio);

                    model.addAttribute("msgfecha", "La hora de inicio no puede estar dentro del horario de una funcion en la sala"+funcion.getIdsala().getNumero());

                    return "Operador/crearFuncion";
                }

            }

            if(funcion.getId()==null){
                //validamos si las horas son iguales
                if (value==0){
                    // Retorna los valores ingresados
                    retornarValoresYSelect(model, funcion, persona, idactor, iddirector, duracion, fechamasinicio);

                    model.addAttribute("msgfecha", "Ya existe una obra con la misma hora de inicio en la sala "+funcion.getIdsala().getNumero());

                    return "Operador/crearFuncion";
                }
            }


        }



        //-----------------------------------------------
        //          Guardar datos de la Función
        //-----------------------------------------------

        //fecha
        LocalDate dia = fecha.toLocalDate();
        //hora inicio
        LocalTime hora = fecha.toLocalTime();

        // Se establece la cantidad de asistentes de la función
        if (funcion.getId() != null){
            Funcion funcionEnDB = funcionRepository.findById(funcion.getId()).get();
            funcion.setCantidadasistentes(funcionEnDB.getCantidadasistentes());
        } else{
            funcion.setCantidadasistentes(0);
        }

        System.out.println(funcion.getCantidadasistentes());

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
            LocalDate Today = LocalDate.now();
            List<Compra> listCompra = compraRepository.buscarCompraPorFuncion(funcionABorrar.getId());
            System.out.println(listCompra.isEmpty());
            if (funcionABorrar.getFecha().compareTo(Today) < 0 || !listCompra.isEmpty()){
                attr.addFlashAttribute("err","No se puede borrar una función cuya fecha ya paso o ya se adquirieron boletos");
                return "redirect:/operador/funciones";
            }
            funcionABorrar.setEstado(0);
            funcionRepository.save(funcionABorrar);

            attr.addFlashAttribute("msg", "Funcion borrada exitosamente");

        } catch (NumberFormatException e) {
            attr.addFlashAttribute("msg", "El ID de la función es inválido");
        }

        return "redirect:/operador/funciones";
    }
}


