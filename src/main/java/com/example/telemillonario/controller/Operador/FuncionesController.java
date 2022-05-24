package com.example.telemillonario.controller.Operador;

import com.example.telemillonario.entity.*;
import com.example.telemillonario.repository.*;
import com.example.telemillonario.service.FileService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
//        List<Funcion> listfunc= funcionRepository.buscarFuncion(estado,(int)funcionesporpagina*pagina, (int)funcionesporpagina);
        // crea 2 listas vacias
        List<Foto> listfunc1 = new ArrayList<Foto>();
        List<Foto> listfunc2 = new ArrayList<Foto>();
        if(listfuncfoto.size()>3){
            // Se agrega las 3 primeras funciones
            for (int i = 0; i < size; i++) {
                listfunc1.add(listfuncfoto.get(i));
            }

            if(listfuncfoto.size()>3 && listfuncfoto.size() <6){
                for (int i = size; i < listfuncfoto.size(); i++) {
                    listfunc2.add(listfuncfoto.get(i));
                }

            }else{
                //Se agrega las 3 funciones restantes
                for (int i = size; i < size*2; i++) {
                    listfunc2.add(listfuncfoto.get(i));
                }

            }
        }else{
            // Se agrega las funciones
            for (int i = 0; i < listfuncfoto.size(); i++) {
                listfunc1.add(listfuncfoto.get(i));
            }

        }

        List<Foto> listfunctotal= fotoRepository.buscarFuncionesParaContar(persona.getIdsede().getId());

        int cantFunc= listfunctotal.size();

        model.addAttribute("listfunc1",listfunc1);
        model.addAttribute("listfunc2",listfunc2);

        model.addAttribute("pagActual",pagina);
        model.addAttribute("pagTotal",(int) Math.ceil(cantFunc/funcionesporpagina));
        return "Operador/index";
    }

    @GetMapping(value = {"/crear"})
    public String programarFuncionesForm(@ModelAttribute("funcion")Funcion funcion, Model model, HttpSession session){

        model.addAttribute("listActores",personaRepository.listarActores("",0,10000000));
        model.addAttribute("listDirectores",personaRepository.listarDirectores());
        model.addAttribute("listGeneros",generoRepository.findAll());
        model.addAttribute("fechaactual",LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
//        System.out.println(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        Persona persona = (Persona) session.getAttribute("usuario");
        //listado de salas por sede disponibles
        model.addAttribute("listaSalasporSede",salaRepository.buscarSalasTotal(persona.getIdsede().getId(),1));
        return "Operador/crearFuncion";
    }

    @GetMapping(value = {"/editar"})
    public String editarFuncionesForm(Model model,@RequestParam("idfuncion") int idfuncion, HttpSession session){

        Optional<Funcion> optFuncionEncontr = funcionRepository.findById(idfuncion);
        Persona persona=(Persona) session.getAttribute("usuario");

        if (optFuncionEncontr.isPresent()) {
            Funcion funcencon = optFuncionEncontr.get();
            model.addAttribute("funcion", funcencon);
            model.addAttribute("listActores",personaRepository.listarActores("",0,10000000));
            model.addAttribute("listDirectores",personaRepository.listarDirectores());
            model.addAttribute("listGeneros",generoRepository.findAll());
            model.addAttribute("fechaactual",LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));

            String[] horafinstr = funcencon.getFin().toString().split(":");
            String horafinstr1 = horafinstr[0];
            String minfinstr2 = horafinstr[1];
            int horafinint = Integer.parseInt(horafinstr1);
            System.out.println(horafinint*60);
            int minfinint = Integer.parseInt(minfinstr2);
            int minhorafinint =horafinint*60;


            int mintotfin=minhorafinint+minfinint;

            String[] horaininstr = funcencon.getInicio().toString().split(":");
            String horaininstr1 = horaininstr[0];
            String horaininstr2 = horaininstr[1];
            int horainiint = Integer.parseInt(horaininstr1);
            int mininiint = Integer.parseInt(horaininstr2);
            int minhorainiint =horainiint*60;

            int mintotini=minhorainiint+mininiint;

            model.addAttribute("duracion",mintotfin-mintotini);

            List<Foto> fotos = fotoRepository.buscarFotosFuncion(idfuncion);

            model.addAttribute("imagenes", fotos);

//            System.out.println(funcencon.getFin()-funcencon.getInicio());

            //listado de salas por sede disponibles
            model.addAttribute("listaSalasporSede",salaRepository.buscarSalasTotal(persona.getIdsede().getId(),1));
            return "Operador/crearFuncion";
        } else {
            return "redirect:/operador/funciones/lista";
        }

    }


    @PostMapping("/guardar")
    public String guardarFuncion(@ModelAttribute("funcion") @Valid Funcion funcion, BindingResult bindingResult,Model model, HttpSession session,
                                 @RequestParam(value="fechamasinicio") String fechamasinicio,
                                 @RequestParam(value="duracion") String duracion,
                                 @RequestParam(value="idactor") String idactor,
                                 @RequestParam(value="iddirector") String iddirector,
                                 @RequestParam(value="idgenero") String idgenero,
                                 @RequestParam(value="imagenes") MultipartFile[] imagenes) throws IOException {

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        if (bindingResult.hasErrors()) {
            model.addAttribute("listActores",personaRepository.listarActores("",0,100000));
            model.addAttribute("listDirectores",personaRepository.listarDirectores());
            model.addAttribute("listGeneros",generoRepository.findAll());
            model.addAttribute("duracion",duracion);
            model.addAttribute("fechaactual",now);
            model.addAttribute("fechamasinicio",fechamasinicio);
            Persona persona = (Persona) session.getAttribute("usuario");
            //listado de salas por sede disponibles
            model.addAttribute("listaSalasporSede",salaRepository.buscarSalasTotal(persona.getIdsede().getId(),1));
            return "Operador/crearFuncion";
        } else {

            if (fechamasinicio.equals("")){
                model.addAttribute("listActores",personaRepository.listarActores("",0,100000));
                model.addAttribute("listDirectores",personaRepository.listarDirectores());
                model.addAttribute("listGeneros",generoRepository.findAll());
                model.addAttribute("duracion",duracion);
                model.addAttribute("fechaactual",now);
                model.addAttribute("msgfecha","Debe ingresar una fecha");
                model.addAttribute("fechamasinicio",fechamasinicio);
                model.addAttribute("val",2);
                Persona persona = (Persona) session.getAttribute("usuario");
                //listado de salas por sede disponibles
                model.addAttribute("listaSalasporSede",salaRepository.buscarSalasTotal(persona.getIdsede().getId(),1));
                return "Operador/crearFuncion";

            }

            LocalDateTime fecha = LocalDateTime
                    .parse(fechamasinicio);
            if (now.isAfter(fecha)){
                model.addAttribute("listActores",personaRepository.listarActores("",0,100000));
                model.addAttribute("listDirectores",personaRepository.listarDirectores());
                model.addAttribute("listGeneros",generoRepository.findAll());
                model.addAttribute("duracion",duracion);
                model.addAttribute("fechaactual",now);
                model.addAttribute("msgfecha","La fecha ingresada no debe ser menor a la actual");
                model.addAttribute("fechamasinicio",fechamasinicio);
                model.addAttribute("val",2);
                Persona persona = (Persona) session.getAttribute("usuario");
                //listado de salas por sede disponibles
                model.addAttribute("listaSalasporSede",salaRepository.buscarSalasTotal(persona.getIdsede().getId(),1));
                return "Operador/crearFuncion";

            }


            if (duracion.equals("")){
                model.addAttribute("listActores",personaRepository.listarActores("",0,100000));
                model.addAttribute("listDirectores",personaRepository.listarDirectores());
                model.addAttribute("listGeneros",generoRepository.findAll());
                model.addAttribute("duracion",duracion);
                model.addAttribute("fechaactual",now);
                model.addAttribute("msgduracion","Debe ingresar una duracion");
                model.addAttribute("fechamasinicio",fechamasinicio);
                Persona persona = (Persona) session.getAttribute("usuario");
                //listado de salas por sede disponibles
                model.addAttribute("listaSalasporSede",salaRepository.buscarSalasTotal(persona.getIdsede().getId(),1));
                return "Operador/crearFuncion";

            }









            if(funcion.getId()==0){

                //separamos el formato de la vista
                String[] pipipi = fechamasinicio.split("T");
                String pipipi1 = pipipi[0];
                String pipipi2 = pipipi[1]+ ":00";

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("HH:mm:00");
                //fecha
                LocalDate datetime = LocalDate.parse(pipipi1, formatter);
                //hora inicio
                LocalTime datetime1 = LocalTime.parse(pipipi2, formatter1);

                //Estado 1 =habilitado
                funcion.setEstado(1);
                funcion.setFecha(datetime);
                funcion.setInicio(datetime1);
                Long durac=Long.parseLong(duracion);
                System.out.println(datetime1);

                funcion.setFin(datetime1.plusMinutes(durac));



                funcionRepository.save(funcion);

                //guardar elenco(actores y directores)
                Funcion func=funcionRepository.findTopByOrderByIdDesc();
                String[] idsact = idactor.split(",");

                for (int i=0;i<idsact.length;i++){
                    Funcionelenco funcelen = new Funcionelenco();
                    int idsactint=Integer.parseInt(idsact[i]);
                    funcelen.setIdpersona(personaRepository.findById(idsactint).get());
                    funcelen.setIdfuncion(func);
                    //estado habilitado
                    funcelen.setEstado(1);
                    funcionElencoRepository.save(funcelen);
                }

                String[] iddir = iddirector.split(",");

                for (int i=0;i<iddir.length;i++){

                    Funcionelenco funcelen = new Funcionelenco();
                    int idsdictint=Integer.parseInt(iddir[i]);
                    funcelen.setIdpersona(personaRepository.findById(idsdictint).get());
                    funcelen.setIdfuncion(func);
                    //estado habilitado
                    funcelen.setEstado(1);
                    funcionElencoRepository.save(funcelen);
                }

                //guardar genero
                String[] idgen = idgenero.split(",");
                for (int i=0;i<idgen.length;i++){
                    Funciongenero fungen = new Funciongenero();
                    int idgenint=Integer.parseInt(idgen[i]);
                    fungen.setIdgenero(generoRepository.findById(idgenint).get());
                    fungen.setIdfuncion(func);
                    //estado habilitado
                    fungen.setEstado(1);
                    funcionGeneroRepository.save(fungen);
                }

                Persona persona=(Persona) session.getAttribute("usuario");
                System.out.println("\nImágenes a Agregar: " + imagenes.length);
                if(!imagenes[0].getOriginalFilename().equals("")){
                    int i =1;
                    for(MultipartFile img : imagenes){
                        System.out.println("Nombre: " + img.getOriginalFilename());
                        System.out.println("Nombre: " + img.getOriginalFilename().length());
                        System.out.println("Tipo: " + img.getContentType());
                        MultipartFile file_aux = fileService.formatearArchivo(img,"foto");
                        if(fileService.subirArchivo(file_aux)){
                            System.out.println("Archivo subido correctamente");
                            Foto foto = new Foto();
                            foto.setEstado(1);
                            foto.setFuncion(func);

                            foto.setIdpersona(persona.getId());
                            foto.setIdsede(persona.getIdsede().getId());
                            foto.setNumero(i);
                            foto.setRuta(fileService.obtenerUrl(file_aux.getOriginalFilename()));
                            fotoRepository.save(foto);
                        }else{
                            System.out.println("El archivo"+img.getOriginalFilename()+"No se pude subir de manera correcta");
                        }
                        i++;
                    }

                }

            }else{
                Funcion func=funcionRepository.findById(funcion.getId()).get();
                //separamos el formato de la vista
                String[] pipipi = fechamasinicio.split("T");
                String pipipi1 = pipipi[0];
                String pipipi2 = pipipi[1]+ ":00";

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("HH:mm:00");
                //fecha
                LocalDate datetime = LocalDate.parse(pipipi1, formatter);
                //hora inicio
                LocalTime datetime1 = LocalTime.parse(pipipi2, formatter1);

                //Estado 1 =habilitado
                func.setEstado(1);
                func.setFecha(datetime);
                func.setInicio(datetime1);
                Long durac=Long.parseLong(duracion);
                System.out.println(datetime1);

                func.setFin(datetime1.plusMinutes(durac));
                func.setRestriccionedad(funcion.getRestriccionedad());
                func.setPrecioentrada(funcion.getPrecioentrada());
                func.setStockentradas(funcion.getStockentradas());
                func.setNombre(funcion.getNombre());
                func.setDescripcion(funcion.getDescripcion());

                funcionRepository.save(func);

                //guardar elenco(actores y directores)
                String[] idsact = idactor.split(",");

                List<Funcionelenco> listactydire =funcionElencoRepository.buscarFuncionElenco(funcion.getId());

                List <Integer> idsactydir= new ArrayList<>();
                for (Funcionelenco elenco : listactydire){
                    idsactydir.add(elenco.getId());
                }

                funcionElencoRepository.deleteAllById(idsactydir);

                for (int i=0;i<idsact.length;i++){
                    Funcionelenco funcelen = new Funcionelenco();

                    int idsactint=Integer.parseInt(idsact[i]);
                    funcelen.setIdpersona(personaRepository.findById(idsactint).get());
                    funcelen.setIdfuncion(func);
                    //estado habilitado
                    funcelen.setEstado(1);
                    funcionElencoRepository.save(funcelen);
                }

                String[] iddir = iddirector.split(",");

                for (int i=0;i<iddir.length;i++){

                    Funcionelenco funcelen = new Funcionelenco();
                    int idsdictint=Integer.parseInt(iddir[i]);
                    funcelen.setIdpersona(personaRepository.findById(idsdictint).get());
                    funcelen.setIdfuncion(func);
                    //estado habilitado
                    funcelen.setEstado(1);
                    funcionElencoRepository.save(funcelen);
                }


                List<Funciongenero> listfungen =funcionGeneroRepository.buscarFuncionGenero(funcion.getId());
                List <Integer> listidgene= new ArrayList<>();
                for (Funciongenero generos : listfungen){
                    listidgene.add(generos.getId());
                }

                funcionGeneroRepository.deleteAllById(listidgene);


                //guardar genero
                String[] idgen = idgenero.split(",");
                for (int i=0;i<idgen.length;i++){
                    Funciongenero fungen = new Funciongenero();
                    int idgenint=Integer.parseInt(idgen[i]);
                    fungen.setIdgenero(generoRepository.findById(idgenint).get());
                    fungen.setIdfuncion(func);
                    //estado habilitado
                    fungen.setEstado(1);
                    funcionGeneroRepository.save(fungen);
                }


                Persona persona=(Persona) session.getAttribute("usuario");
                System.out.println("\nImágenes a Agregar: " + imagenes.length);
                if(!imagenes[0].getOriginalFilename().equals("")){
                    int i =1;
                    for(MultipartFile img : imagenes){
                        System.out.println("Nombre: " + img.getOriginalFilename());
                        System.out.println("Tipo: " + img.getContentType());
                        MultipartFile file_aux = fileService.formatearArchivo(img,"foto");
                        if(fileService.subirArchivo(file_aux)){
                            System.out.println("Archivo subido correctamente");
                            Foto foto = new Foto();
                            foto.setEstado(1);
                            foto.setFuncion(func);

                            foto.setIdpersona(persona.getId());
                            foto.setIdsede(persona.getIdsede().getId());
                            foto.setNumero(i);
                            foto.setRuta(fileService.obtenerUrl(file_aux.getOriginalFilename()));
                            fotoRepository.save(foto);
                        }else{
                            System.out.println("El archivo"+img.getOriginalFilename()+"No se pude subir de manera correcta");
                        }
                        i++;
                    }

                }


            }





            return "redirect:/operador/funciones";
        }




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
//        List<Funcion> listfunc= funcionRepository.buscarFuncion(estado,(int)funcionesporpagina*pagina, (int)funcionesporpagina);
                // crea 2 listas vacias
                List<Foto> listfunc1 = new ArrayList<Foto>();
                List<Foto> listfunc2 = new ArrayList<Foto>();


                if(listfuncfoto.size()>3){
                    // Se agrega las 3 primeras funciones
                    for (int i = 0; i < size; i++) {
                        listfunc1.add(listfuncfoto.get(i));
                    }

                    if(listfuncfoto.size()>3 && listfuncfoto.size() <6){
                        for (int i = size; i < listfuncfoto.size(); i++) {
                            listfunc2.add(listfuncfoto.get(i));
                        }

                    }else{
                        //Se agrega las 3 funciones restantes
                        for (int i = size; i < size*2; i++) {
                            listfunc2.add(listfuncfoto.get(i));
                        }

                    }
                }else{
                    // Se agrega las funciones
                    for (int i = 0; i < listfuncfoto.size(); i++) {
                        listfunc1.add(listfuncfoto.get(i));
                    }

                }



                List<Foto> listfunctotal= fotoRepository.buscarFuncionesParaContarPorNombre(persona.getIdsede().getId(),parametro);

                int cantFunc= listfunctotal.size();

                model.addAttribute("listfunc1",listfunc1);
                model.addAttribute("listfunc2",listfunc2);

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
