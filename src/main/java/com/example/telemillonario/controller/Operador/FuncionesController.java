package com.example.telemillonario.controller.Operador;

import com.example.telemillonario.entity.*;
import com.example.telemillonario.repository.*;
import com.example.telemillonario.service.FileService;
import com.nimbusds.oauth2.sdk.id.Actor;
import org.apache.tomcat.jni.Local;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    FuncionGeneroRepository funcionGeneroRepository;

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
        List<Foto> listfunctotal= fotoRepository.buscarFuncionesParaContar(persona.getIdsede().getId());
        int cantFunc= listfunctotal.size();

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

            // Listas para los select
            retornarValores(model,funcion,persona);

            // Lista de los elementos YA seleccionados
            model.addAttribute("actoresFuncion",personaRepository.actoresPorFuncion(idfuncion));
            model.addAttribute("directoresFuncion",personaRepository.directoresPorFuncion(idfuncion));
            model.addAttribute("generosFuncion",generoRepository.generosPorFuncion(idfuncion));

            // Tiempo
            long duracion = funcion.getInicio().until(funcion.getFin(),ChronoUnit.MINUTES);
            model.addAttribute("duracion",duracion);

            String fechamasinicio = funcion.getFecha().toString() + "T" + funcion.getInicio().toString();
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
                                 @RequestParam(value="fechamasinicio") String fechamasinicio,
                                 @RequestParam(value="duracion") String duracion,
                                 @RequestParam(value="idactor") String[] idactor,
                                 @RequestParam(value="iddirector") String[] iddirector,
                                 @RequestParam(value="idgenero") String[] idgenero,
                                 @RequestParam(value="eliminar", defaultValue="") String[] ids,
                                 @RequestParam(value="imagenes") MultipartFile[] imagenes) throws IOException {

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

            retornarValores(model,funcion,persona);

            model.addAttribute("fechamasinicio",fechamasinicio);
            model.addAttribute("duracion",duracion);

            if(duracionInt <= 0) {
                model.addAttribute("msgduracion", "La duración no es válida");
            }

            // También los elementos seleccionados
            model.addAttribute("actoresFuncion",idactor);
            model.addAttribute("directoresFuncion",iddirector);
            model.addAttribute("generosFuncion",idgenero);

            return "Operador/crearFuncion";
        }

        //-----------------------------------------------
        //      Validación de las Imágenes enviadas
        //-----------------------------------------------
        long tamanho = 0;

        for (MultipartFile img : imagenes){

            // Se verifica que los archivos enviados sean imágenes
            switch(img.getContentType()){

                case "application/octet-stream":
                case "image/jpeg":
                case "image/png":
                    tamanho += img.getSize();

                    // Se verifica que el tamaño no sea superior a 20 MB
                    if (tamanho > 1048576*20){
                        model.addAttribute("err","Se superó la capacidad de imagen máxima de 20MB");

                        // Listas para los select
                        retornarValores(model,funcion,persona);

                        // Se regresan elementos ya seleccionados
                        model.addAttribute("actoresFuncion",idactor);
                        model.addAttribute("directoresFuncion",iddirector);
                        model.addAttribute("generosFuncion",idgenero);
                        model.addAttribute("duracion",duracion);
                        model.addAttribute("fechamasinicio",fechamasinicio);

                        return "Operador/crearFuncion";
                    }
                    break;

                default:
                    model.addAttribute("err","Solo se deben de enviar imágenes");

                    // Listas para los select
                    retornarValores(model,funcion,persona);

                    // Se regresan elementos ya seleccionados
                    model.addAttribute("actoresFuncion",idactor);
                    model.addAttribute("directoresFuncion",iddirector);
                    model.addAttribute("generosFuncion",idgenero);
                    model.addAttribute("duracion",duracion);
                    model.addAttribute("fechamasinicio",fechamasinicio);

                    return "Operador/crearFuncion";
            }
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

            // Listas para los select
            retornarValores(model,funcion,persona);

            // Se regresan elementos ya seleccionados
            model.addAttribute("actoresFuncion",idactor);
            model.addAttribute("directoresFuncion",iddirector);
            model.addAttribute("generosFuncion",idgenero);
            model.addAttribute("duracion",duracion);
            model.addAttribute("fechamasinicio",fechamasinicio);

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

        // Se guardan los cambios a la funcion
        funcion = funcionRepository.save(funcion);

        //-----------------------------------------------
        //      Guardar datos del Elenco y Género
        //-----------------------------------------------

        // Se obtiene el elenco de la función
        List<Funcionelenco> elencoEnDB = funcionElencoRepository.buscarFuncionElenco(funcion.getId());
        List<Integer> elencoSeleccionado = new ArrayList<>();

        // Se obtienen los géneros de la Función
        List<Funciongenero> generosEnDB = funcionGeneroRepository.buscarFuncionGenero(funcion.getId());
        List<Integer> generosSeleccionados = new ArrayList<>();

        // Se comparan los datos de elenco ingresados con los de la DB
        int maxSeleccionados = Math.max(Math.max(idactor.length,iddirector.length),idgenero.length) ;
        int maxEnDB = Math.max(elencoEnDB.size(), generosEnDB.size()) ;

        for(int ii=0; ii < maxSeleccionados; ii++ ){

            boolean generoCoincide = false;
            boolean elencoCoincide = false;

            for(int jj=0; jj < maxEnDB; jj++){

                // Valida los generos seleccionados
                if(ii<idgenero.length && jj<generosEnDB.size() && idgenero[ii].equals(generosEnDB.get(jj).toString())){
                    generosSeleccionados.add(generosEnDB.get(jj).getIdgenero().getId());
                    generoCoincide = true;
                }

                // Valida los miembros del elenco seleccionados
                if(ii < idactor.length && jj<elencoEnDB.size() && idactor[ii].equals(elencoEnDB.get(jj).getIdpersona().getId().toString())){
                    elencoSeleccionado.add(elencoEnDB.get(jj).getIdpersona().getId());
                    elencoCoincide = true;

                } else if(ii < iddirector.length && jj<elencoEnDB.size() && iddirector[ii].equals(elencoEnDB.get(jj).getIdpersona().getId().toString())){
                    elencoSeleccionado.add(elencoEnDB.get(jj).getIdpersona().getId());
                    elencoCoincide = true;
                }
            }

            // Si un miembro del elenco no está en DB, se agrega
            if(!elencoCoincide){
                Funcionelenco funcelen = new Funcionelenco();
                int idsdictint = Integer.parseInt(iddirector[ii]);
                funcelen.setIdpersona(personaRepository.findById(idsdictint).get());
                funcelen.setIdfuncion(funcion);
                funcelen.setEstado(1);
                funcionElencoRepository.save(funcelen);
            }
            // Si un género no está en DB, se agrega
            if(!generoCoincide){
                Funciongenero funcgen = new Funciongenero();
                int idgen = Integer.parseInt(idgenero[ii]);
                funcgen.setIdgenero(generoRepository.findById(idgen).get());
                funcgen.setIdfuncion(funcion);
                funcgen.setEstado(1);
                funcionGeneroRepository.save(funcgen);
            }
        }

        // Si un miembro del elenco ya no ha sido seleccionado, se elimina
        for(Funcionelenco fe : elencoEnDB) {
            int id = fe.getIdpersona().getId();
            if (!elencoSeleccionado.contains(id)){
                funcionElencoRepository.deleteById(fe.getId());
            }
        }
        // Si un miembro del elenco ya no ha sido seleccionado, se elimina
        for(Funciongenero fg : generosEnDB) {
            int id = fg.getIdgenero().getId();
            if (!generosSeleccionados.contains(id)){
                funcionGeneroRepository.deleteById(fg.getId());
            }
        }

        //-----------------------------------------------
        //    Validación de Fotos a Eliminar en la DB
        //-----------------------------------------------

        // Número de fotos Ya Guardadas en la DB
        List<Foto> fotosParaEliminar = new ArrayList<>();

        // Se obtienen las fotos guardadas en DB
        List<Foto> fotosEnDB = fotoRepository.buscarFotosFuncion(funcion.getId());
        int fotosGuardadas = fotosEnDB.size();

        for(int i = 0; i < fotosEnDB.size(); i++){
            boolean fotoBorrada = false;

            Foto fotoEnDB = fotosEnDB.get(i);

            for(int j = 0; j < ids.length; j++) {
                try{
                    int id = Integer.parseInt(ids[j]);

                    // Se compara el ID de la foto en DB con el de la foto que se quiere remover
                    if (fotoEnDB.getId() == id){

                        fotosParaEliminar.add(fotoEnDB);

                        fotosGuardadas--;
                        fotoBorrada = true;
                        break;
                    }

                } catch (Exception e){
                    System.out.println(e.getMessage());
                    break;
                }
            }
            // Cuando se elimina una foto todos los números posteriores se corren por 1
            int fotosEliminadas = fotosEnDB.size() - fotosGuardadas;

            if(!fotoBorrada && fotosEliminadas > 0){
                fotoEnDB.setNumero(fotoEnDB.getNumero()-fotosEliminadas);
                fotoRepository.save(fotoEnDB);
            }
        }

        // Verifica que la casilla de imágenes no se vaya a quedar vacía
        if (fotosGuardadas + imagenes.length == 1 && imagenes[0].getContentType().equals("application/octet-stream")) {
            model.addAttribute("err", "Se debe de tener al menos 1 imagen");

            retornarValores(model,funcion,persona);

            // Se regresan elementos ya seleccionados
            model.addAttribute("actoresFuncion",idactor);
            model.addAttribute("directoresFuncion",iddirector);
            model.addAttribute("generosFuncion",idgenero);
            model.addAttribute("duracion",duracion);
            model.addAttribute("fechamasinicio",fechamasinicio);

            return "Operador/crearFuncion";
        }

        // Elimina las fotos
        for (Foto foto : fotosParaEliminar){
            // Borrado de la DB
            fotoRepository.delete(foto);

            // Borrado del fileService
            String ruta = foto.getRuta();
            String nombreFoto = ruta.substring(ruta.lastIndexOf('/') + 1);

            fileService.eliminarArchivo(nombreFoto);
        }

        // Regresa si no se han agregado más fotos
        if(imagenes[0].getContentType().equals("application/octet-stream")) {
            attr.addFlashAttribute("msg", "Director Guardado Exitosamente");
            return "redirect:/operador/funciones";
        }

        //-----------------------------------------------
        //            Agregar Fotos a la DB
        //-----------------------------------------------

        // Se guarda imagen por imagen (en ese orden se les dará número)
        for(int i = 0; i< imagenes.length; i++){
            MultipartFile img = imagenes[i];
            try {
                // Se almacenan los datos en un objeto Foto
                Foto foto = new Foto();
                foto.setEstado(1);
                foto.setFuncion(funcion);
                foto.setNumero(fotosGuardadas + i);

                // Guardar en el Servidor
                MultipartFile imgRenombrada = fileService.formatearArchivo(img, "funcion");
                if(fileService.subirArchivo(imgRenombrada)){

                    // Si se guarda exitosamente, se obtiene el url de la foto
                    foto.setRuta(fileService.obtenerUrl(imgRenombrada.getOriginalFilename()));
                } else {
                    attr.addFlashAttribute("err","No se pudo conectar con el Contenedor De Archivos");
                    return "redirect:/operador/funciones";
                }

                // Guardado en la DB
                System.out.println(imgRenombrada.getOriginalFilename());
                fotoRepository.save(foto);

            } catch (Exception e){
                System.out.println(e.getMessage());
                attr.addFlashAttribute("err", "Hubo un problema al guardar las Imagenes");
                return "redirect:/operador/funciones";
            }
        }
        attr.addFlashAttribute("msg","Actor Guardado Exitosamente");
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
    public String borrarFuncion(Model model,
                                @RequestParam("idfuncion") int idfuncion,
                                 RedirectAttributes attr) {

        //buscar las fotos a eliminar en el filemanager de Azure

        try{
            List<Foto> listfuncfoto=fotoRepository.buscarFotosFuncion(idfuncion);

            if(listfuncfoto.size()!=0){
                //eliminamos las fotos una por una del filemanager de Azure y de la base de datos
                for (Foto fot : listfuncfoto) {
                    String [] urlfoto=fot.getRuta().split("/telemillonario/");
                    String nombrefoto= urlfoto[1];
                    fileService.eliminarArchivo(nombrefoto);
                    fotoRepository.deleteById(fot.getId());
                }

                //eliminamos en la tabla funcion elenco
                List<Funcionelenco> listFunElenc =funcionElencoRepository.buscarFuncionElenco(idfuncion);
                for (Funcionelenco funelen : listFunElenc){
                    funcionElencoRepository.deleteById(funelen.getId());
                }

                //eliminamos en la tabla funcion genero
                List<Funciongenero> listFuncGen =funcionGeneroRepository.buscarFuncionGenero(idfuncion);
                for (Funciongenero fungen : listFuncGen){
                    funcionGeneroRepository.deleteById(fungen.getId());
                }

                //eliminamos la funcion de la base de datos
                funcionRepository.deleteById(idfuncion);

                attr.addFlashAttribute("msg1", "Funcion borrada exitosamente");
            }else{
                attr.addFlashAttribute("msg", "No se puede borrar una funcion que no existe");
            }
            return "redirect:/operador/funciones";
        }catch (Exception e){

            attr.addFlashAttribute("msg", "Hubo un error al borrar una funcion");
            return "redirect:/operador/funciones";
        }



    }

}
