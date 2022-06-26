package com.example.telemillonario.controller.Operador;



import com.example.telemillonario.entity.Calificaciones;
import com.example.telemillonario.entity.Foto;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;


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


        //Envio de Reseñas de la obra con nombre de la persona + calificacion+foto del usuario

        LinkedHashMap<Calificaciones,String> reseniasconurlfotousuario= new LinkedHashMap<>();
        List<Calificaciones> ListResenias = calificacionesRepository.buscarReseñasSede(operador.getIdsede().getId());
        for (Calificaciones calf :ListResenias){
            List<Foto> fotosEnDB = fotoRepository.findByIdpersonaOrderByNumero(calf.getPersona().getId());
            if(fotosEnDB.size()>0){
                reseniasconurlfotousuario.put(calf,fotosEnDB.get(0).getRuta());
            }else{
                reseniasconurlfotousuario.put(calf,"/img/user.png");
            }
        }
        model.addAttribute("reseniasconfoto",reseniasconurlfotousuario);


        return "Operador/reseniasSede";
    }



    @GetMapping("/borrar")
    public String borrarResenia(Model model,
                             @RequestParam("id") int idresenia,
                             RedirectAttributes attr) {

        //borrado logico

        try{
            Optional<Calificaciones> CalifBusc = calificacionesRepository.findById(idresenia);

            if(CalifBusc.isPresent()){

                Calificaciones CalifBorrar = CalifBusc.get();
                CalifBorrar.setEstado(0);
                calificacionesRepository.save(CalifBorrar);
                attr.addFlashAttribute("msg", "Se ha borrado exitosamente la reseña con su calificación");
            }else{

                attr.addFlashAttribute("msg", "No se encontró la reseña a borrar");
            }

            return "redirect:/operador/resenias/sede";
        }catch (Exception e){

            attr.addFlashAttribute("msg", "Ocurrió un error");
            return "redirect:/operador/resenias/sede";
        }



    }

}
