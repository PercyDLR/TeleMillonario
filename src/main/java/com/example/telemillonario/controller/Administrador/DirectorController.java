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
@RequestMapping("/admin/directores")
public class DirectorController {

    @Autowired
    PersonaRepository personaRepository;
    @Autowired
    FotoRepository fotoRepository;

    @Autowired
    FileService fileService;

    //Variables Importantes
    float directoresxpagina=8;

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

        int cantDirectores = personaRepository.cantDirectores(busqueda.toLowerCase());

        List<Persona> listaDirectores = personaRepository.listarDirectoresFiltrado(busqueda.toLowerCase(), (int)directoresxpagina*pagina, (int)directoresxpagina);

        model.addAttribute("pagActual",pagina);
        model.addAttribute("pagTotal",(int) Math.ceil(cantDirectores/directoresxpagina));

        model.addAttribute("busqueda", busqueda);
        model.addAttribute("listaDirectores", listaDirectores);
        return "Administrador/Director/listaDirectores";
    }

    @PostMapping("/buscar")
    String busqueda(@RequestParam(value="busqueda", defaultValue = "") String busqueda){
        return "redirect:/admin/directores?busqueda="+busqueda;
    }

    @GetMapping("/nuevo")
    public String formNuevoActor(@ModelAttribute("director") Persona actor){
        return "Administrador/Director/editarDirector";
    }

    @GetMapping("/editar")
    public String formEditarActor(RedirectAttributes attr, @ModelAttribute("director") Persona director,
                                  @RequestParam("id") String iddirector, Model model){

        try{
            int id = Integer.parseInt(iddirector);

            // Se obtienen los datos y las imágenes del actor
            director = personaRepository.findById(id).get();
            List<Foto> fotos = fotoRepository.findByIdpersonaOrderByNumero(id);

            model.addAttribute("imagenes", fotos);
            model.addAttribute("director",director);
            return "Administrador/Director/editarDirector";

        } catch (Exception e){
            attr.addFlashAttribute("err", "La ID del actor es inválida");
            return "redirect:/admin/directores/lista";
        }
    }

    @PostMapping("/subir")
    public String recibirImagen(@ModelAttribute("director") @Validated(Elenco.class) Persona director,
                                BindingResult bindingResult,
                                @RequestParam(value = "eliminar", defaultValue = "") String[] ids,
                                @RequestParam("imagenes") MultipartFile[] imagenes,
                                RedirectAttributes attr){


        // Se buscan errores de validación
        if(bindingResult.hasErrors()){
            System.out.println(bindingResult.getFieldError());
            return "Administrador/Director/editarDirector";
        }

        //-----------------------------------------------
        //      Validación de los Datos enviados
        //-----------------------------------------------
        long tamanho = 0;

        for (MultipartFile img : imagenes){

            // Se verifica que los archivos enviados sean imágenes
            switch(img.getContentType()){

                case "application/octet-stream":
                    attr.addFlashAttribute("err","Debe ingresar al menos 1 imagen");
                    return "redirect:/admin/directores";

                case "image/jpeg":
                case "image/png":
                    tamanho += img.getSize();

                    // Se verifica que el tamaño no sea superior a 20 MB
                    if (tamanho > 1048576*20){
                        attr.addFlashAttribute("err","Se superó la capacidad de imagen máxima de 20MB");
                        return "redirect:/admin/directores";
                    }
                    break;

                default:
                    attr.addFlashAttribute("err","Solo se deben de enviar imágenes");
                    return "redirect:/admin/directores";
            }
        }

        //-----------------------------------------------
        //         Guardado de Datos del Actor
        //-----------------------------------------------

        try {
            // Primero se guardan los datos del actor
            Rol rol = new Rol();
            rol.setId(4);
            director.setIdrol(rol);
            director.setEstado(1);

            personaRepository.save(director);

            // Se obtienen el ID del actor recién creado
            director = personaRepository.findFirstByNombresOrderByIdDesc(director.getNombres());
        } catch (Exception e){
            System.out.println(e.getMessage());
            attr.addFlashAttribute("err","Hubo un problema al guardar los datos de Director");
            return "redirect:/admin/directores";
        }
        // Número de fotos Ya Guardadas en la DB
        int fotosGuardadas = 0;

        //-----------------------------------------------
        //            Eliminar Foto de la DB
        //-----------------------------------------------
        if(director.getId() != null){
            System.out.println("Imágenes a Eliminar: " + ids.length);

            // Se obtienen las fotos guardadas en DB
            List<Foto> fotosEnDB = fotoRepository.findByIdpersonaOrderByNumero(director.getId());
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
                foto.setIdpersona(director.getId());
                foto.setNumero(fotosGuardadas + i);

                // Guardar en el Sevidor
                MultipartFile imgRenombrada = fileService.formatearArchivo(img, "director");
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

                if(imagenes[0].getContentType().equals("application/octet-stream")) {
                    attr.addFlashAttribute("msg", "Director Guardado Exitosamente");
                } else{
                    attr.addFlashAttribute("err", "Hubo un problema al guardar las Imagenes");
                }

                return "redirect:/admin/directores";
            }

            System.out.println("Nombre: " + img.getOriginalFilename());
            System.out.println("Tipo: " + img.getContentType());
        }

        attr.addFlashAttribute("msg","Director Guardado Exitosamente");
        return "redirect:/admin/directores";
    }

    @GetMapping(value = "/borrar")
    public String borrarOperador(@RequestParam("id") String str_id, RedirectAttributes attr){

        try{
            int id = Integer.parseInt(str_id);

            if(personaRepository.existsById(id)){
                Optional<Persona> aux = personaRepository.findById(id);
                Persona director = aux.get();
                director.setEstado(0);//Borrado lógico
                personaRepository.save(director);
                attr.addFlashAttribute("msg","El director ha sido eliminado exitosamente");
            }else{
                attr.addFlashAttribute("err", "El director con ID: "+id+" no existe");
            }
            return "redirect:/admin/directores/";

        }catch (Exception e){
            attr.addFlashAttribute("err", "El ID es inválido");
            return "redirect:/admin/directores/";
        }

    }
}
