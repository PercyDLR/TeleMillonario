package com.example.telemillonario.controller.Administrador;


import com.example.telemillonario.entity.Sala;
import com.example.telemillonario.entity.Sede;
import com.example.telemillonario.repository.DistritoRepository;
import com.example.telemillonario.repository.SalaRepository;
import com.example.telemillonario.repository.SedeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.spring5.processor.SpringTextareaFieldTagProcessor;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/salas")
public class SalaController {

    @Autowired
    SalaRepository salaRepository;
    @Autowired
    SedeRepository sedeRepository;
    @Autowired
    DistritoRepository distritoRepository;

    // Variables Importantes (Afuera para más orden)
//    int idsede=7;
    float salasporpagina=5;

    @GetMapping(value = {"", "/","/lista"})
    public String listado(@RequestParam(value="parametro",required = false,defaultValue = "") String parametro,
                           @RequestParam(value="buscador",required = false,defaultValue = "") String buscador,
                           @RequestParam(value="ord",required = false,defaultValue = "") String ord,
                           @RequestParam(value = "pag",required = false) String pag,
                            @RequestParam(value = "idsede",required = false) String idsede,
                           Model model){
        try{
           int sedeid=Integer.parseInt(idsede);
            model.addAttribute("sede", sedeRepository.findById(sedeid).get());
            int numero = !parametro.isBlank() ? Integer.parseInt(parametro) : 0;
            int estado = buscador.equals("disponible") ? 1 : buscador.equals("nodisponible") ? 0 : 2;

            int pagina;
            try{
                pagina = Integer.parseInt(pag);
            }catch(Exception e) {
                pagina=0;
            }
            int cantSalas = salaRepository.cantSalas(sedeid,numero,estado);

            List<Sala> listaSalas = switch (ord) {
                case "mayor" -> salaRepository.buscarSalasDesc(sedeid, numero, estado, (int)salasporpagina*pagina, (int)salasporpagina);
                case "menor" -> salaRepository.buscarSalasAsc(sedeid, numero, estado, (int)salasporpagina*pagina, (int)salasporpagina);
                default -> salaRepository.buscarSalas(sedeid, numero, estado, (int)salasporpagina*pagina, (int)salasporpagina);
            };

            model.addAttribute("parametro", parametro);
            model.addAttribute("buscador", buscador);
            model.addAttribute("ord",ord);

            model.addAttribute("pagActual",pagina);
            model.addAttribute("pagTotal",(int) Math.ceil(cantSalas/salasporpagina));
            model.addAttribute("idsede",sedeid);
            model.addAttribute("listSalas", listaSalas);
            return "Administrador/Sala/listaSalas";
        }catch (NumberFormatException nfe){
            model.addAttribute("status",404);
            model.addAttribute("error","");
            return "error";
        }




    }

    @PostMapping("/filtrar")
    String busqueda(@RequestParam(value="parametro",defaultValue = "") String parametro,
                    @RequestParam(value="buscador", defaultValue = "") String buscador,
                    @RequestParam(value="ord", defaultValue = "") String ord,
                    @RequestParam(value = "pag",required = false) String pag,
                    @RequestParam(value = "idsede",required = false) String idsede,
                    RedirectAttributes attr){

        return "redirect:/admin/salas?parametro="+parametro+"&buscador="+buscador+"&ord="+ord+"&idsede="+idsede+"&pag="+pag;
    }

    @GetMapping("/nuevaSala")
    public String crearSala(@ModelAttribute("sala") Sala sala, Model model, @RequestParam("idsede") int idsede){
        model.addAttribute("sede", sedeRepository.findById(idsede).get());
        return "Administrador/Sala/editarSalas";
    }

    @GetMapping("/editarSalas")
    public String editarSala(@RequestParam("id") int id, Model model, @ModelAttribute("sala") Sala sala, RedirectAttributes a) {

        Optional<Sala> optionalSala = salaRepository.findById(id);
        if(optionalSala.isEmpty()) {
            a.addFlashAttribute("msg","-1");
            return "redirect:/admin/salas";
        } else {
            model.addAttribute("sala",optionalSala.get());
            model.addAttribute("sede", optionalSala.get().getIdsede());
            return "Administrador/Sala/editarSalas";
        }
    }

    @PostMapping("/guardar")
    public String guardarSala(@ModelAttribute("sala") @Valid Sala sala, BindingResult bindingResult, RedirectAttributes a, Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("sede", sala.getIdsede());
            return "Administrador/Sala/editarSalas";
        } else {
            Optional<Sala> optSala = salaRepository.findById(sala.getId());
            if(!optSala.isPresent()){
                //Crea automaticamente el numero de la sala
                List<Sala> listaSalas = salaRepository.buscarSalas(sala.getIdsede().getId(),0,2,0,100000);
                int b = 0;
                for (Sala s : listaSalas) {
                    if(s.getNumero() > b) {
                        b = s.getNumero();
                    }
                }
                sala.setNumero(b+1);

                //Crea automaticamente el identificador de la sala
                String[] palabras = sala.getIdsede().getNombre().split(" ");
                String identificador = this.genIdentificador(palabras,b+1);
                sala.setIdentificador(identificador);

                a.addFlashAttribute("msg","0");
            } else {
                String[] palabras = sala.getIdsede().getNombre().split(" ");
                String identificador = this.genIdentificador(palabras,optSala.get().getNumero());
                sala.setIdentificador(identificador);
                a.addFlashAttribute("msg","1");
            }
            a.addFlashAttribute("identificador", sala.getIdentificador());
            salaRepository.save(sala);
            return "redirect:/admin/salas?idsede="+sala.getIdsede().getId();
        }
    }

    @GetMapping("/disponibilidad")
    public String disponibilidadSala(@RequestParam("id") int id,@RequestParam(value = "idsede",required = false) String idsede, RedirectAttributes a) {
        Optional<Sala> optionalSala = salaRepository.findById(id);
        if (optionalSala.isPresent()) {
            Sala sala = optionalSala.get();
            if (sala.getEstado() == 1) {
                sala.setEstado(0);
                salaRepository.save(sala);
            } else {
                sala.setEstado(1);
                salaRepository.save(sala);
            }
            a.addFlashAttribute("msg", "2");
            a.addFlashAttribute("identificador", sala.getIdentificador());
        }
        return "redirect:/admin/salas?idsede="+idsede;
    }

    private String genIdentificador(String[] palabras, int numeroSala){
        String identificador="";

        for(String palabra : palabras){
            // Ignoraq monosilabos
            if(palabra.length() > 2) {
                identificador = identificador.concat(palabra.substring(0, 1));
            }
        }
        identificador = identificador.concat(String.format("%03d",numeroSala));

        System.out.println("Identificador: " + identificador);
        return identificador;
    }
}
