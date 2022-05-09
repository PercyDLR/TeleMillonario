package com.example.telemillonario.controller.Operador;

import com.example.telemillonario.entity.Foto;
import com.example.telemillonario.entity.Funcion;
import com.example.telemillonario.entity.Funcionelenco;
import com.example.telemillonario.entity.Persona;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/funciones")
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
        // Se agrega las 3 primeras funciones
        for (int i = 0; i < size; i++) {
            listfunc1.add(listfuncfoto.get(i));
        }
        //Se agrega las 3 funciones restantes
        for (int i = size; i < size*2; i++) {
            listfunc2.add(listfuncfoto.get(i));
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
            return "redirect:/funciones/lista";
        }

    }


    @PostMapping("/guardar")
    public String guardarFuncion(@ModelAttribute("funcion") @Valid Funcion funcion, BindingResult bindingResult,Model model, HttpSession session,
                                 @RequestParam(value="fechamasinicio") String fechamasinicio,
                                 @RequestParam(value="duracion") String duracion,
                                 @RequestParam(value="idactor") String idactor,
                                 @RequestParam(value="iddirector") String iddirector,
                                 @RequestParam(value="imagenes") MultipartFile[] imagenes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("listActores",personaRepository.listarActores("",0,100000));
            model.addAttribute("listDirectores",personaRepository.listarDirectores());
            Persona persona = (Persona) session.getAttribute("usuario");
            //listado de salas por sede disponibles
            model.addAttribute("listaSalasporSede",salaRepository.buscarSalasTotal(persona.getIdsede().getId(),1));
            return "Operador/crearFuncion";
        } else {


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


            Persona persona=(Persona) session.getAttribute("usuario");
            System.out.println("\nImágenes a Agregar: " + imagenes.length);
            int i =1;
            for(MultipartFile img : imagenes){
                System.out.println("Nombre: " + img.getOriginalFilename());
                System.out.println("Tipo: " + img.getContentType());
                if(fileService.subirArchivo(img)){
                    System.out.println("Archivo subido correctamente");
                    Foto foto = new Foto();
                    foto.setEstado(1);
                    foto.setFuncion(func);

                    foto.setIdpersona(persona.getId());
                    foto.setIdsede(persona.getIdsede().getId());
                    foto.setNumero(i);
                    foto.setRuta(fileService.obtenerUrl(img.getOriginalFilename()));
                    fotoRepository.save(foto);
                }else{
                    System.out.println("El archivo"+img.getOriginalFilename()+"No se pude subir de manera correcta");
                }
                i++;
            }



            return "redirect:/funciones";
        }




    }




}
