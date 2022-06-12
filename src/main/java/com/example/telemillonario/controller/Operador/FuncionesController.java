package com.example.telemillonario.controller.Operador;

import com.example.telemillonario.entity.*;
import com.example.telemillonario.repository.*;
import com.example.telemillonario.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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
    float funcionesporpagina=6;

    @Autowired
    GeneroRepository generoRepository;

    @Autowired
    ObraGeneroRepository obraGeneroRepository;

    @Autowired
    ObraRepository obraRepository;

    @GetMapping(value = {"", "/","/lista"})
    public String listadoFunciones(Model model,@RequestParam(value = "pag",required = false) String pag,HttpSession session){
        int estado=1;
        int pagina;
        //tamaña de cada 'row' en vista
        int size=3;
        try{
            pagina = Integer.parseInt(pag);
        }catch(Exception e) {
            pagina=0;
        }

        Persona persona=(Persona) session.getAttribute("usuario");

        List<Foto> listfuncfoto= fotoRepository.buscarFotoFunciones(persona.getIdsede().getId(),(int)funcionesporpagina*pagina, (int)funcionesporpagina);
        int cantFunc= fotoRepository.contarFunciones(persona.getIdsede().getId());

        model.addAttribute("listfunc",listfuncfoto);

        model.addAttribute("pagActual",pagina);
        model.addAttribute("pagTotal",(int) Math.ceil(cantFunc/funcionesporpagina));
        return "Operador/index";
    }

    @GetMapping(value = {"/crear"})
    public String programarFuncionesForm(@ModelAttribute("funcion")Funcion funcion, Model model, HttpSession session){

        // Listas para los selectores
        model.addAttribute("listActores",personaRepository.listarActores("",0,10000000));
        model.addAttribute("listDirectores",personaRepository.listarDirectores());
        model.addAttribute("listGeneros",generoRepository.findAll());
        model.addAttribute("fechaactual",LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));

        // Listas vacias para evitar errores
        ArrayList<Integer> listaVacia = new ArrayList<>();
        model.addAttribute("generosFuncion",listaVacia);
        model.addAttribute("actoresFuncion",listaVacia);
        model.addAttribute("directoresFuncion",listaVacia);

        //listado de salas para la Sede del Operador
        Persona persona = (Persona) session.getAttribute("usuario");
        model.addAttribute("listaSalasporSede", salaRepository.buscarSalasTotal(persona.getIdsede().getId(),1));
        return "Operador/crearFuncion";
    }

    private void retornarValores(Model model, Funcion funcion, Persona persona) {

        // Variables para hacer más entendible el código
        List<Persona> listActores = personaRepository.listarActores("",0,100000);
        List<Persona> listDirectores = personaRepository.listarDirectores();
        List<Genero> listGeneros = generoRepository.findAll();
        List<Foto> fotosEnDB = fotoRepository.buscarFotosFuncion(funcion.getId());
        List<Sala> listaSalasporSede = salaRepository.buscarSalasTotal(persona.getIdsede().getId(),1);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        // Se regresan nuevamente los elementos de las listas
        model.addAttribute("funcion",funcion);
        model.addAttribute("listActores",listActores);
        model.addAttribute("listDirectores",listDirectores);
        model.addAttribute("listGeneros",listGeneros);
        model.addAttribute("fechaactual",now);
        model.addAttribute("imagenes", fotosEnDB);
        model.addAttribute("listaSalasporSede",listaSalasporSede);
    }

    private void retornarValoresYSelect(Model model, Funcion funcion, Persona persona,
                                        String[] idactor, String[] iddirector,
                                        String duracion, String fechamasinicio) {

        this.retornarValores(model,funcion,persona);

        // Se regresan elementos ya seleccionados
        model.addAttribute("actoresFuncion",idactor);
        model.addAttribute("directoresFuncion",iddirector);
        model.addAttribute("duracion",duracion);
        model.addAttribute("fechamasinicio",fechamasinicio);
    }

    @GetMapping(value = {"/editar"})
    public String editarFuncionesForm(@ModelAttribute("funcion") Funcion funcion,
                                      @RequestParam("idfuncion") String idStr,
                                      Model model, HttpSession session,
                                      RedirectAttributes attr){

        // Se verifica que el ID sea un número
        int idfuncion = 0;
        try{
            idfuncion = Integer.parseInt(idStr);
        } catch (NumberFormatException e){
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
            long duracion = funcion.getInicio().until(funcion.getFin(),ChronoUnit.MINUTES);
            String fechamasinicio = funcion.getFecha().toString() + "T" + funcion.getInicio().toString();

            // Listas para los select
            retornarValores(model,funcion,persona);

            // Lista de los elementos YA seleccionados
            model.addAttribute("actoresFuncion",personaRepository.actoresPorFuncion(idfuncion));
            model.addAttribute("directoresFuncion",personaRepository.directoresPorFuncion(idfuncion));
            model.addAttribute("generosFuncion",generoRepository.generosPorFuncion(idfuncion));

            // Tiempo
            model.addAttribute("duracion",duracion);
            model.addAttribute("fechamasinicio",fechamasinicio);

            return "Operador/crearFuncion";
        } else {
            attr.addFlashAttribute("err", "La función que busca no existe");
            return "redirect:/operador/funciones/lista";
        }

    }


    @PostMapping("/guardar")
    public String guardarFuncion(@ModelAttribute("funcion") @Valid Funcion funcion, BindingResult bindingResult,
                                 Model model, HttpSession session, RedirectAttributes attr,
                                 @RequestParam(value="idobra") String idObraStr,
                                 @RequestParam(value="fechamasinicio") String fechamasinicio,
                                 @RequestParam(value="duracion") String duracion,
                                 @RequestParam(value="idactor") String[] idactor,
                                 @RequestParam(value="iddirector") String[] iddirector) {

        Persona persona = (Persona) session.getAttribute("usuario");
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        //-----------------------------------------------
        //    Validación de Datos Ingresados
        //-----------------------------------------------
        int duracionInt = 0;
        try{
            duracionInt = Integer.parseInt(duracion);
        } catch(NumberFormatException e){}

        if (bindingResult.hasErrors() || duracionInt <= 0) {

            retornarValoresYSelect(model,funcion,persona,idactor,iddirector,duracion,fechamasinicio);

            if(duracionInt <= 0) {
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

            if(!obra.isPresent()){
                throw new NumberFormatException();
            }
            obraFuncion = obra.get();

        } catch (NumberFormatException e){

            // Retorna los valores ingresados
            retornarValoresYSelect(model,funcion,persona,idactor,iddirector,duracion,fechamasinicio);

            model.addAttribute("msgObra", "La obra ingresada no es válida");

            return "Operador/crearFuncion";
        }

        //-----------------------------------------------
        //          Validación de la Fecha
        //-----------------------------------------------

        LocalDateTime fecha;
        try{
            fecha = LocalDateTime.parse(fechamasinicio);

            if(now.isAfter(fecha)){
                throw new Exception();
            }
        } catch (Exception e){

            // Retorna los valores ingresados
            retornarValoresYSelect(model,funcion,persona,idactor,iddirector,duracion,fechamasinicio);

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
        int maxSeleccionados = Math.max(idactor.length,iddirector.length) ;

        for(int ii=0; ii < maxSeleccionados; ii++ ){

            boolean actorCoincide = false;
            boolean directorCoincide = false;

            for(int jj=0; jj < elencoEnDB.size(); jj++){

                // Valida los miembros del elenco seleccionados
                if(ii < idactor.length && idactor[ii].equals(elencoEnDB.get(jj).getIdpersona().getId().toString())){
                    elencoSeleccionado.add(elencoEnDB.get(jj).getIdpersona().getId());
                    actorCoincide = true;

                } else if(ii < iddirector.length && iddirector[ii].equals(elencoEnDB.get(jj).getIdpersona().getId().toString())){
                    elencoSeleccionado.add(elencoEnDB.get(jj).getIdpersona().getId());
                    directorCoincide = true;
                }
            }

            // Si un miembro del elenco no está en DB, se agrega
            if(ii<iddirector.length && !directorCoincide){
                Funcionelenco funcelen = new Funcionelenco();
                int idsdictint = Integer.parseInt(iddirector[ii]);
                funcelen.setIdpersona(personaRepository.findById(idsdictint).get());
                funcelen.setIdfuncion(funcion);
                funcelen.setEstado(1);
                funcionElencoRepository.save(funcelen);

            } else if(ii<idactor.length && !actorCoincide){
                Funcionelenco funcelen = new Funcionelenco();
                int idsdictint = Integer.parseInt(idactor[ii]);
                funcelen.setIdpersona(personaRepository.findById(idsdictint).get());
                funcelen.setIdfuncion(funcion);
                funcelen.setEstado(1);
                funcionElencoRepository.save(funcelen);
            }
        }

        // Si un miembro del elenco ya no ha sido seleccionado, se elimina
        for(Funcionelenco fe : elencoEnDB) {
            int id = fe.getIdpersona().getId();
            if (!elencoSeleccionado.contains(id)){
                funcionElencoRepository.deleteById(fe.getId());
            }
        }

        attr.addFlashAttribute("msg","Función Guardada Exitosamente");
        return "redirect:/operador/funciones";
    }


    @PostMapping("/buscar")
    public String buscarFuncion (Model model,@RequestParam("parametro") String parametro,RedirectAttributes attr,@RequestParam(value = "pag",required = false) String pag,HttpSession session){

        try {
            if (parametro.equals("")) { // verifica que no esté vacío
                attr.addFlashAttribute("msg", "La búsqueda no debe estar vacía.");
                return "redirect:/operador/funciones";
            } else {
                model.addAttribute("parametro", parametro);
                parametro = parametro.toLowerCase();

                int pagina;

                try{
                    pagina = Integer.parseInt(pag);
                }catch(Exception e) {
                    pagina=0;
                }

                Persona persona=(Persona) session.getAttribute("usuario");

                List<Foto> listfuncfoto= fotoRepository.buscarFotoFuncionesPorNombre(persona.getIdsede().getId(),parametro,(int)funcionesporpagina*pagina, (int)funcionesporpagina);
                List<Foto> listfunctotal= fotoRepository.buscarFuncionesParaContarPorNombre(persona.getIdsede().getId(),parametro);
                int cantFunc= listfunctotal.size();

                model.addAttribute("listfunc",listfuncfoto);

                model.addAttribute("pagActual",pagina);
                model.addAttribute("pagTotal",(int) Math.ceil(cantFunc/funcionesporpagina));

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

        try{
            int idFuncion = Integer.parseInt(idFuncionStr);

            Optional<Funcion> funcion = funcionRepository.findById(idFuncion);

            if(!funcion.isPresent()){
                throw new NumberFormatException();
            }

            Funcion funcionABorrar = funcion.get();
            funcionABorrar.setEstado(0);
            funcionRepository.save(funcionABorrar);

            attr.addFlashAttribute("msg1", "Funcion borrada exitosamente");

        }catch (NumberFormatException e){
            attr.addFlashAttribute("msg", "El ID de la función es inválido");
        }
        return "redirect:/operador/funciones";
    }
}
