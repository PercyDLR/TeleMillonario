package com.example.telemillonario.controller.Administrador;

import com.example.telemillonario.entity.Foto;
import com.example.telemillonario.entity.Obra;
import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.entity.Sede;
import com.example.telemillonario.repository.FotoRepository;
import com.example.telemillonario.repository.FuncionRepository;
import com.example.telemillonario.repository.ObraRepository;
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
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/obras")
public class ObraAdminController {

    @Autowired
    ObraRepository obraRepository;

    @Autowired
    FotoRepository fotoRepository;

    @Autowired
    FileService fileService;

    @Autowired
    FuncionRepository funcionRepository;
    float obrasporpagina=6;

    @GetMapping(value = {"", "/","/lista"})
    public String listarObras(Model model,@RequestParam(value = "busqueda",defaultValue = "") String busqueda,
                              @RequestParam(value = "pag",defaultValue = "0") String pag, HttpSession session){

        int pagina;
        int estado=1;
        try{
            pagina = Integer.parseInt(pag);
        }catch(Exception e) {
            pagina=0;
        }


        if (busqueda.equals("")) { // verifica que no esté vacío

            List<Foto> listObraConFoto= fotoRepository.listadoObrasFotoAdmin(estado,(int)obrasporpagina*pagina, (int)obrasporpagina);
            int cantObras = obraRepository.cantObrasTotalAdmin();
            model.addAttribute("pagTotal",(int) Math.ceil(cantObras/obrasporpagina));
            model.addAttribute("listObras",listObraConFoto);
        }else{
            List<Foto> listObraConFoto= fotoRepository.buscarObrasFotoPorNombreAdmin(estado,busqueda , (int)obrasporpagina*pagina, (int)obrasporpagina);
            int cantObras = obraRepository.cantObrasTotalFiltrAdmin(estado,busqueda);
            model.addAttribute("pagTotal",(int) Math.ceil(cantObras/obrasporpagina));
            model.addAttribute("listObras",listObraConFoto);
        }

        model.addAttribute("pagActual",pagina);
        model.addAttribute("busqueda", busqueda);
        return "Administrador/Obra/listaObras";
    }

    @GetMapping(value = {"/crear"})
    public String crearSedeForm(@ModelAttribute("obra") Obra obra, Model model){


        return "Administrador/Obra/editarObra";
    }


    @GetMapping("/editar")
    public String editarObra(Model model, @RequestParam("idobra") int idobra) {

        //COMPLETAR

        Optional<Obra> optObra = obraRepository.findById(idobra);

        if (optObra.isPresent()) {
            Obra obra = optObra.get();
            model.addAttribute("obra", obra);
            List<Foto> fotos = fotoRepository.buscarFotosObra(idobra);

            model.addAttribute("imagenes", fotos);
            return "Administrador/Obra/editarObra";
        } else {
            return "redirect:/admin/obras/lista";
        }
    }

    @PostMapping("/guardar")
    public String guardarObra(@ModelAttribute("obra") @Valid Obra obra, BindingResult bindingResult,
                              HttpSession session, @RequestParam(value = "eliminar", defaultValue = "a") String[] ids,
                              @RequestParam(value="imagenes") MultipartFile[] imagenes,
                              RedirectAttributes attr, Model model) throws IOException {

        if (bindingResult.hasErrors()) {
            return "Administrador/Obra/editarObra";
        } else {
            //VALIDACIONES Fotos

            //Se verifica que se haya subido al menos una foto al crear un formulario
            if(obra.getId()==null && imagenes[0].getContentType().equals("application/octet-stream")){

                model.addAttribute("err","Se debe de tener al menos 1 imagen");
                return "Administrador/Obra/editarObra";

            }
            //Se verifica que la obra no se quede sin fotos al eliminar en el form de editar
            if(obra.getId()!=null){
                int cantidadfotos = fotoRepository.buscarFotosObra(obra.getId()).size();
                if(cantidadfotos==1 && !ids[0].equals("a")){
                    model.addAttribute("err","Se debe de tener al menos 1 imagen");
                    Optional<Obra> optObraenc = obraRepository.findById(obra.getId());
                    Obra obraencon = optObraenc.get();
                    model.addAttribute("obra", obraencon);

                    List<Foto> fotos = fotoRepository.buscarFotosObra(obra.getId());

                    model.addAttribute("imagenes", fotos);
                    return "Administrador/Sede/editarSedes";
                }
            }

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
                            if (obra.getId() != null) {
                                Optional<Obra> optObraenc = obraRepository.findById(obra.getId());
                                Obra obraencon = optObraenc.get();
                                model.addAttribute("obra", obraencon);

                                List<Foto> fotos = fotoRepository.buscarFotosObra(obra.getId());

                                model.addAttribute("imagenes", fotos);
                            }
                            return "Administrador/Sede/editarSedes";
                        }
                        break;

                    default:
                        model.addAttribute("err","Solo puede subir imagenes con formato jpg ,jpeg y png");
                        if (obra.getId() != 0) {
                            Optional<Obra> optObraenc = obraRepository.findById(obra.getId());
                            Obra obraencon = optObraenc.get();
                            model.addAttribute("obra", obraencon);

                            List<Foto> fotos = fotoRepository.buscarFotosObra(obra.getId());

                            model.addAttribute("imagenes", fotos);
                        }
                        return "Administrador/Sede/editarSedes";

                }
            }

            if (obra.getId() == null) {
                attr.addFlashAttribute("msg", "Se ha creado correctamente la obra");

                //agregar imagenes

                obraRepository.save(obra);
                //se busca el id del objeto obra creado
                Obra obraCreada=obraRepository.findTopByOrderByIdDesc();
                System.out.println("\nImágenes a Agregar: " + imagenes.length);
                if(!imagenes[0].getOriginalFilename().equals("")){
                    int i =0;
                    for(MultipartFile img : imagenes){
                        System.out.println("Nombre: " + img.getOriginalFilename());
                        System.out.println("Tipo: " + img.getContentType());
                        MultipartFile file_aux = fileService.formatearArchivo(img,"foto");
                        if(fileService.subirArchivo(file_aux)){
                            System.out.println("Archivo subido correctamente");
                            Foto foto = new Foto();
                            foto.setEstado(1);
                            foto.setIdobra(obraCreada);
                            foto.setNumero(i);
                            foto.setRuta(fileService.obtenerUrl(file_aux.getOriginalFilename()));
                            fotoRepository.save(foto);
                        }else{
                            System.out.println("El archivo"+img.getOriginalFilename()+"No se pude subir de manera correcta");
                        }
                        i++;
                    }

                }

                return "redirect:/admin/obras/lista";
            } else {

                //eliminar imagenes
                System.out.println("Imágenes a Eliminar: " + ids.length);
                if(!ids[0].equals("a")){
                    for(String id : ids){
                        System.out.println("ID: " + id);
                        int idfoto= Integer.parseInt(id);

                        Foto fot=fotoRepository.findById(idfoto).get();
                        String [] urlfoto=fot.getRuta().split("/telemillonario/");
                        String nombrefoto= urlfoto[1];
                        fileService.eliminarArchivo(nombrefoto);
                        fotoRepository.deleteById(idfoto);

                    }

                }


                //agregar imagenes
                Obra obraCreada=obraRepository.findById(obra.getId()).get();
                System.out.println("\nImágenes a Agregar: " + imagenes.length);
                if(!imagenes[0].getOriginalFilename().equals("")){
                    int i =0;
                    for(MultipartFile img : imagenes){
                        System.out.println("Nombre: " + img.getOriginalFilename());
                        System.out.println("Tipo: " + img.getContentType());
                        MultipartFile file_aux = fileService.formatearArchivo(img,"foto");
                        if(fileService.subirArchivo(file_aux)){
                            System.out.println("Archivo subido correctamente");
                            Foto foto = new Foto();
                            foto.setEstado(1);
                            foto.setIdobra(obraCreada);
                            foto.setNumero(i);
                            foto.setRuta(fileService.obtenerUrl(file_aux.getOriginalFilename()));
                            fotoRepository.save(foto);
                        }else{
                            System.out.println("El archivo"+img.getOriginalFilename()+"No se pude subir de manera correcta");
                        }
                        i++;
                    }

                }

                obraRepository.save(obra);
                attr.addFlashAttribute("msg", "Se ha editado exitosamente la obra");
                return "redirect:/admin/obras/lista";
            }
        }
    }


    @GetMapping("/borrar")
    public String borrarObra(Model model,
                             @RequestParam("idobra") int idobra,
                             RedirectAttributes attr) {

        //buscar las fotos a eliminar en el filemanager de Azure

        try{
            int a=funcionRepository.valCantFuncionConObra(idobra);
            if( a!=0 ){
                attr.addFlashAttribute("msg", "No puede eliminar una obra que este asociada a una funcion ");
                return "redirect:/admin/obras/lista";
            }
            List<Foto> fotos = fotoRepository.buscarFotosObra(idobra);;

            if(fotos.size()!=0){
                //eliminamos las fotos una por una del filemanager de Azure y de la base de datos
                for (Foto fot : fotos) {
                    String [] urlfoto=fot.getRuta().split("/telemillonario/");
                    String nombrefoto= urlfoto[1];
                    fileService.eliminarArchivo(nombrefoto);
                    fotoRepository.deleteById(fot.getId());
                }

                //eliminamos la obra de la base de datos
                obraRepository.deleteById(idobra);

                attr.addFlashAttribute("msg", "Se ha eliminado correctamente la obra");
            }else{
                attr.addFlashAttribute("msg", "No se puede borrar una obra que no existe");
            }
            return "redirect:/admin/obras/lista";
        }catch (Exception e){

            attr.addFlashAttribute("msg", "Hubo un error al borrar la obra");
            return "redirect:/admin/obras/lista";
        }



    }

}
