package com.example.telemillonario.controller.Administrador;

import com.example.telemillonario.entity.Foto;
import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.entity.Rol;
import com.example.telemillonario.repository.FotoRepository;
import com.example.telemillonario.repository.PersonaRepository;
import com.example.telemillonario.service.FileService;
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

    @Autowired
    FileService fileService;

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


        // Se buscan errores de validación
        if(bindingResult.hasErrors()){
            System.out.println(bindingResult.getFieldError());
            return "Administrador/Actor/editarActor";
        }

        // Primero se guardan los datos del actor
        Rol rol = new Rol();
        rol.setId(5);
        actor.setIdrol(rol);
        actor.setEstado(1);

        personaRepository.save(actor);

        // Se obtienen el ID del actor recién creado
        actor = personaRepository.findByNombres(actor.getNombres());
        // Número de fotos Ya Guardadas en la DB
        int fotosGuardadas = 0;

        //-----------------------------------------------
        //            Eliminar Foto de la DB
        //-----------------------------------------------
        if(actor.getId() != null){
            System.out.println("Imágenes a Eliminar: " + ids.length);

            // Se obtienen las fotos guardadas en DB
            List<Foto> fotosEnDB = fotoRepository.findByIdpersonaOrderByNumero(actor.getId());
            fotosGuardadas = fotosEnDB.size();
            boolean fotoEliminada = false;

            for(int i = 0; i < fotosEnDB.size(); i++){
                for(int j = 0; j < ids.length; j++) {
                    try{
                        int id = Integer.parseInt(ids[j]);
                        Foto fotoEnDB = fotosEnDB.get(i);

                        // Se compara el ID de la foto en DB con el de la foto que se quiere remover
                        if (fotoEnDB.getId() == id){

                            // Borrado de la DB
                            fotoRepository.delete(fotoEnDB);

                            // Borrado del fileService
                            String ruta = fotosEnDB.get(i).getRuta();
                            String nombreFoto = ruta.substring(ruta.lastIndexOf('/') + 1);
                            System.out.println("Nombre de la foto a eliminar: " + nombreFoto);
                            fileService.eliminarArchivo(nombreFoto);

                            fotoEliminada = true;
                            fotosGuardadas--;
                            break;
                        }
                        // Cuando se elimina una foto todos los números posteriores se corren por 1
                        if(fotoEliminada){
                            fotoEnDB.setNumero(fotoEnDB.getNumero()-1);
                            fotoRepository.save(fotoEnDB);
                        }


                    } catch (Exception e){
                        System.out.println(e.getMessage());
                        break;
                    }
                }
            }
        }

        //-----------------------------------------------
        //            Agregar Foto de la DB
        //-----------------------------------------------

        System.out.println("Imágenes a Agregar: " + imagenes.length);

        // Se guarda imagen por imagen (en ese orden se les dará número)
        for(int i = 0; i< imagenes.length; i++){
            MultipartFile img = imagenes[i];
            try {
                // Se almacenan los datos en un objeto Foto
                Foto foto = new Foto();
                foto.setEstado(1);
                foto.setIdpersona(actor.getId());
                foto.setNumero(fotosGuardadas + i);

                // Guardar en el Sevidor
                MultipartFile imgRenombrada = fileService.formatearArchivo(img, "actor");
                if(fileService.subirArchivo(imgRenombrada)){

                    // Si se guarda exitosamente, se obtiene el url de la foto
                    foto.setRuta(fileService.obtenerUrl(imgRenombrada.getOriginalFilename()));
                } else {
                    System.out.println("No se pudo guardar la foto!!!!!");
                }

                // Guardado en la DB
                System.out.println(imgRenombrada.getOriginalFilename());
                fotoRepository.save(foto);

            } catch (Exception e){
                System.out.println(e.getMessage());
            }

            System.out.println("Nombre: " + img.getOriginalFilename());
            System.out.println("Tipo: " + img.getContentType());
        }

        return "redirect:/admin/actores";
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
