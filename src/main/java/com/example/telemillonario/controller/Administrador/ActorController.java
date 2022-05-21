package com.example.telemillonario.controller.Administrador;

import com.example.telemillonario.entity.Foto;
import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.repository.FotoRepository;
import com.example.telemillonario.repository.PersonaRepository;
import com.example.telemillonario.validation.Elenco;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/actores")
public class ActorController {

    @Autowired
    PersonaRepository personaRepository;
    @Autowired
    FotoRepository fotoRepository;

    //Variables Importantes
    float actoresxpagina=8;

    @GetMapping(value = {"","/","/lista"})
    public String listarActores(Model model,
                                @RequestParam(value = "busqueda",defaultValue = "") String busqueda,
                                @RequestParam(value = "pag",defaultValue = "0") String pag){
        int pagina;
        try{
            pagina = Integer.parseInt(pag);
        }catch(Exception e) {
            pagina=0;
        }

        int cantActores = personaRepository.cantActores(busqueda.toLowerCase());

        List<Persona> listaActores = personaRepository.listarActores(busqueda.toLowerCase(), (int)actoresxpagina*pagina, (int)actoresxpagina);

        model.addAttribute("pagActual",pagina);
        model.addAttribute("pagTotal",(int) Math.ceil(cantActores/actoresxpagina));

        model.addAttribute("busqueda", busqueda);
        model.addAttribute("listaActores", listaActores);
        return "Administrador/Actor/listaActores";
    }

    @PostMapping("/buscar")
    String busqueda(@RequestParam(value="busqueda", defaultValue = "") String busqueda){

        return "redirect:/admin/actores?busqueda="+busqueda;
    }

    @GetMapping("/nuevo")
    public String formNuevoActor(@ModelAttribute("actor") Persona actor){

        return "Administrador/Actor/editarActor";
    }

    @GetMapping("/editar")
    public String formEditarActor(RedirectAttributes attr, @ModelAttribute("actor") Persona actor,
                                  @RequestParam("id") String idactor, Model model){

        try{
            int id = Integer.parseInt(idactor);

            // Se obtienen los datos y las imágenes del actor
            actor = personaRepository.findById(id).get();
            List<Foto> fotos = fotoRepository.findByIdpersonaOrderByNumero(id);

            model.addAttribute("imagenes", fotos);
            model.addAttribute("actor",actor);
            return "Administrador/Actor/editarActor";

        } catch (Exception e){
            attr.addFlashAttribute("err", "La ID del actor es inválida");
            return "redirect:/admin/actores/lista";
        }
    }

    @PostMapping("/subir")
    public String recibirImagen(@ModelAttribute("actor") @Validated(Elenco.class) Persona actor,
                                BindingResult bindingResult,
                                @RequestParam(value = "eliminar", defaultValue = "") String[] ids,
                                @RequestParam("imagenes") MultipartFile[] imagenes,
                                RedirectAttributes attr){


        if(bindingResult.hasErrors()){
            System.out.println(bindingResult.getFieldError());
            return "Administrador/Actor/editarActor";
        }

        //Editar Actor
        if(actor.getId() != null){
            System.out.println("Imágenes a Eliminar: " + ids.length);
            for(String id : ids){
                System.out.println("ID: " + id);
            }
        }
        // Nuevo Actor
        System.out.println("Imágenes a Agregar: " + imagenes.length);
        for(MultipartFile img : imagenes){
            System.out.println("Nombre: " + img.getOriginalFilename());
            System.out.println("Tipo: " + img.getContentType());
        }

        return "redirect:/admin/actores/editar?id="+actor.getId();
    }

    @GetMapping(value = "/borrar")
    public String borrarOperador(@RequestParam("id") String str_id, RedirectAttributes attr){

        try{
            int id = Integer.parseInt(str_id);

            if(personaRepository.existsById(id)){
                Optional<Persona> aux = personaRepository.findById(id);
                Persona actor = aux.get();
                actor.setEstado(0);//Borrado lógico
                personaRepository.save(actor);
                attr.addFlashAttribute("msg","El actor ha sido eliminado exitosamente");
            }else{
                attr.addFlashAttribute("err", "El operador con ID:"+id+" no se encuentra presente");
            }
            return "redirect:/admin/actores/";

        }catch (Exception e){
            attr.addFlashAttribute("err", "El ID es inválido");
            return "redirect:/admin/actores/";
        }

    }
}
