package com.example.telemillonario.controller.Administrador;

import com.example.telemillonario.entity.Foto;
import com.example.telemillonario.entity.Sala;
import com.example.telemillonario.repository.FotoRepository;
import com.example.telemillonario.repository.SedeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping( value = {"/admin","/admin/sedes"})
public class SedeController {
    @Autowired
    FotoRepository fotoRepository;

    @Autowired
    SedeRepository sedeRepository;
    float sedesporpagina=15;
    @GetMapping(value = {"", "/","/lista"})
    public String listaSedes(Model model, @RequestParam(value = "pag",required = false) String pag,
                            @RequestParam(value="parametro",required = false,defaultValue = "") String parametro,
                             @RequestParam(value="buscador",required = false,defaultValue = "") String buscador){
        int pagina;
        int estado=1;
        int cantSalas = sedeRepository.buscarSedesTotal(estado);
        try{
            pagina = Integer.parseInt(pag);
        }catch(Exception e) {
            pagina=0;
        }

        if (parametro.equals("")) { // verifica que no esté vacío

            List<Foto> listSedesConFoto= fotoRepository.listadoSedes(estado);
            model.addAttribute("listaSedes",listSedesConFoto);

        }else{
            parametro = parametro.toLowerCase();
            List<Foto> listSedesConFoto = switch (buscador) {
                case "nombre" -> fotoRepository.buscarSedePorNombre(parametro, estado, (int)sedesporpagina*pagina, (int)sedesporpagina);
                case "distrito" -> fotoRepository.buscarSedePorDistrito(parametro, estado, (int)sedesporpagina*pagina, (int)sedesporpagina);
                default -> fotoRepository.listadoSedes(estado);
            };
            model.addAttribute("listaSedes",listSedesConFoto);
        }

        model.addAttribute("parametro", parametro);
        model.addAttribute("buscador", buscador);

        model.addAttribute("pagActual",pagina);
        model.addAttribute("pagTotal",(int) Math.ceil(cantSalas/sedesporpagina));
        return "Administrador/Sede/listaSedes";
    }


    @PostMapping(value = {"/buscar"})
    public String buscarSede(@RequestParam("parametro") String parametro, @RequestParam("buscador") String buscador,
                             @RequestParam(value = "pag",required = false) String pag, RedirectAttributes attr){

        return "redirect:/admin/sedes?parametro="+parametro+"&buscador="+buscador+"&pag="+pag;
    }

}
