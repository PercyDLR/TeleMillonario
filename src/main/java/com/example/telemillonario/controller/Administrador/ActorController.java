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

import java.util.ArrayList;
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
                                BindingResult bindingResult, Model model,
                                @RequestParam(value = "eliminar", defaultValue = "") String[] ids,
                                @RequestParam("imagenes") MultipartFile[] imagenes,
                                RedirectAttributes attr){


        // Se buscan errores de validación
        if(bindingResult.hasErrors()){
            System.out.println(bindingResult.getFieldError());
            return "Administrador/Actor/editarActor";
        }

        //-----------------------------------------------
        //      Validación de los Datos enviados
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
                        if (actor.getId() != null){
                            model.addAttribute("imagenes",fotoRepository.findByIdpersonaOrderByNumero(actor.getId()));
                        }
                        return "Administrador/Actor/editarActor";
                    }
                    break;

                default:
                    model.addAttribute("err","Solo se deben de enviar imágenes");
                    if (actor.getId() != null){
                        model.addAttribute("imagenes",fotoRepository.findByIdpersonaOrderByNumero(actor.getId()));
                    }
                    return "Administrador/Actor/editarActor";
            }
        }

        //-----------------------------------------------
        //         Guardado de Datos del Actor
        //-----------------------------------------------
        try {
            Rol rol = new Rol();
            rol.setId(5);
            actor.setIdrol(rol);
            actor.setEstado(1);

            actor = personaRepository.save(actor);

        } catch (Exception e){
            System.out.println(e.getMessage());
            attr.addFlashAttribute("err","Hubo un problema al guardar los datos de Actor");
            return "redirect:/admin/actores";
        }

        //-----------------------------------------------
        //    Validación de Fotos a Eliminar en la DB
        //-----------------------------------------------

        // Número de fotos Ya Guardadas en la DB
        int fotosGuardadas = 0;
        List<Foto> fotosParaEliminar = new ArrayList<>();

        // Se obtienen las fotos guardadas en DB
        List<Foto> fotosEnDB = fotoRepository.findByIdpersonaOrderByNumero(actor.getId());
        fotosGuardadas = fotosEnDB.size();

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

        //System.out.println("Imágenes a Agregar: " + imagenes.length);

        // Verifica que la casilla de imágenes no se vaya a quedar vacía
        if (fotosGuardadas + imagenes.length == 1 && imagenes[0].getContentType().equals("application/octet-stream")) {
            model.addAttribute("err", "Se debe de tener al menos 1 imagen");
            model.addAttribute("imagenes", fotoRepository.findByIdpersonaOrderByNumero(actor.getId()));
            return "Administrador/Actor/editarActor";
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
            return "redirect:/admin/actores";
        }

        //-----------------------------------------------
        //            Agregar Foto a la DB
        //-----------------------------------------------

        // Se guarda imagen por imagen (en ese orden se les dará número)
        for(int i = 0; i< imagenes.length; i++){
            MultipartFile img = imagenes[i];
            try {
                // Se almacenan los datos en un objeto Foto
                Foto foto = new Foto();
                foto.setEstado(1);
                foto.setIdpersona(actor.getId());
                foto.setNumero(fotosGuardadas + i);

                // Guardar en el Servidor
                MultipartFile imgRenombrada = fileService.formatearArchivo(img, "actor");
                if(fileService.subirArchivo(imgRenombrada)){

                    // Si se guarda exitosamente, se obtiene el url de la foto
                    foto.setRuta(fileService.obtenerUrl(imgRenombrada.getOriginalFilename()));
                } else {
                    attr.addFlashAttribute("err","No se pudo conectar con el Contenedor De Archivos");
                    return "redirect:/admin/actores";
                }

                // Guardado en la DB
                System.out.println(imgRenombrada.getOriginalFilename());
                fotoRepository.save(foto);

            } catch (Exception e){
                System.out.println(e.getMessage());
                attr.addFlashAttribute("err", "Hubo un problema al guardar las Imagenes");
                return "redirect:/admin/actores";
            }
        }
        attr.addFlashAttribute("msg","Actor Guardado Exitosamente");
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
                attr.addFlashAttribute("err", "El actor con ID: "+id+" no se encuentra presente");
            }
            return "redirect:/admin/actores/";

        }catch (Exception e){
            attr.addFlashAttribute("err", "El ID es inválido");
            return "redirect:/admin/actores/";
        }

    }
}
