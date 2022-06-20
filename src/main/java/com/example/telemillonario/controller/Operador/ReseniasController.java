package com.example.telemillonario.controller.Operador;



import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.repository.CalificacionesRepository;
import com.example.telemillonario.repository.FotoRepository;
import com.example.telemillonario.repository.SedeRepository;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CalcChainDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;


@Controller
@RequestMapping("/operador/resenias")
public class ReseniasController {


    @Autowired
    CalificacionesRepository calificacionesRepository;

    @Autowired
    FotoRepository fotoRepository;

    @Autowired
    SedeRepository sedeRepository;
    @GetMapping("")
    public String vistaResenias(Model model, HttpSession session){

        Persona operador = (Persona) session.getAttribute("usuario");
        //Envio de la calificacion promedio de la sede
        model.addAttribute("califpromsede",calificacionesRepository.PromCalificacionSede(operador.getIdsede().getId()));
        //Envio de foto
        model.addAttribute("fotosede",fotoRepository.fotoSede(operador.getIdsede().getId()));
        //Envio de cantidad de reseñas positivas(calificacion mayor igual a 3)
        model.addAttribute("cantreseposi",calificacionesRepository.CantResePosi(operador.getIdsede().getId()));

        //Envio de cantidad de reseñas negativas(calificacion menor a 3)
        model.addAttribute("cantresenega",calificacionesRepository.CantReseNega(operador.getIdsede().getId()));

        return "Operador/vistaResenias";
    }

    @GetMapping("/sede")
    public String vistaReseniasSede(Model model, HttpSession session){

        Persona operador = (Persona) session.getAttribute("usuario");
        model.addAttribute("sede",sedeRepository.getById(operador.getIdsede().getId()));
        //Envio de foto
        model.addAttribute("fotosede",fotoRepository.fotoSede(operador.getIdsede().getId()));
        //Envio de la calificacion promedio de la sede
        model.addAttribute("califpromsede",calificacionesRepository.PromCalificacionSede(operador.getIdsede().getId()));
        //Envio de Reseñas de la sede con nombre de la persona + calificacion

        model.addAttribute("ListResenias",calificacionesRepository.buscarReseñasSede(operador.getIdsede().getId()));

        return "Operador/reseniasSede";
    }



}
