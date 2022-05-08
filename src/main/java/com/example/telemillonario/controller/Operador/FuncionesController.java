package com.example.telemillonario.controller.Operador;

import com.example.telemillonario.entity.Funcion;
import com.example.telemillonario.entity.Funcionelenco;
import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.repository.FuncionElencoRepository;
import com.example.telemillonario.repository.FuncionRepository;
import com.example.telemillonario.repository.PersonaRepository;
import com.example.telemillonario.repository.SalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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

    @GetMapping(value = {"", "/","/lista"})
    public String listadoFunciones(){

        return "Operador/index";
    }

    @GetMapping(value = {"/crear"})
    public String programarFuncionesForm(@ModelAttribute("funcion")Funcion funcion, Model model, HttpSession session){

        model.addAttribute("listActores",personaRepository.listarActores());
        model.addAttribute("listDirectores",personaRepository.listarDirectores());
        Persona persona = (Persona) session.getAttribute("usuario");
        //listado de salas por sede disponibles
        model.addAttribute("listaSalasporSede",salaRepository.buscarSalasTotal(persona.getIdsede().getId(),1));
        return "Operador/crearFuncion";
    }

    @PostMapping("/guardar")
    public String guardarFuncion(@ModelAttribute("funcion") @Valid Funcion funcion, BindingResult bindingResult,Model model, HttpSession session,
                                 @RequestParam(value="fechamasinicio") String fechamasinicio,
                                 @RequestParam(value="duracion") String duracion,
                                 @RequestParam(value="idactor") String idactor,
                                 @RequestParam(value="iddirector") String iddirector) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("listActores",personaRepository.listarActores());
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

            String[] idsact = idactor.split(",");

            for (int i=0;i<idsact.length;i++){
                Funcion func=funcionRepository.findTopByOrderByIdDesc();
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
                Funcion func1=funcionRepository.findTopByOrderByIdDesc();
                Funcionelenco funcelen = new Funcionelenco();
                int idsdictint=Integer.parseInt(iddir[i]);
                funcelen.setIdpersona(personaRepository.findById(idsdictint).get());
                funcelen.setIdfuncion(func1);
                //estado habilitado
                funcelen.setEstado(1);
                funcionElencoRepository.save(funcelen);
            }

//            employee.setHireDate(new Date());
//
//            employeesRepository.save(employee);
//            attr.addFlashAttribute("msg", "Empleado actualizado exitosamente");
            return "redirect:/funciones";
        }


    }




}
