package com.example.telemillonario.controller.Administrador;

import com.example.telemillonario.entity.*;
import com.example.telemillonario.repository.DistritoRepository;
import com.example.telemillonario.repository.FotoRepository;
import com.example.telemillonario.repository.SedeRepository;
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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping( value = {"/admin","/admin/sedes"})
public class SedeController {
    @Autowired
    FotoRepository fotoRepository;

    @Autowired
    DistritoRepository distritoRepository;

    @Autowired
    FileService fileService;

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


    @GetMapping(value = {"/crear"})
    public String crearSedeForm(@ModelAttribute("sede") Sede sede, Model model, HttpSession session){


        model.addAttribute("listdistritos",distritoRepository.findAll());
        return "Administrador/Sede/editarSedes";
    }



    @GetMapping("/editar")
    public String editarEmployee(Model model, @RequestParam("idsede") int idsede) {

        //COMPLETAR

        Optional<Sede> optSede = sedeRepository.findById(idsede);

        if (optSede.isPresent()) {
            Sede sede = optSede.get();
            model.addAttribute("sede", sede);
            model.addAttribute("listdistritos",distritoRepository.findAll());
            List<Foto> fotos = fotoRepository.buscarFotosSede(idsede);

            model.addAttribute("imagenes", fotos);
            return "Administrador/Sede/editarSedes";
        } else {
            return "redirect:/admin/sedes/lista";
        }
    }


    @PostMapping("/guardar")
    public String guardarSede(@ModelAttribute("sede") @Valid Sede sede,BindingResult bindingResult,
                              HttpSession session, @RequestParam(value = "eliminar", defaultValue = "a") String[] ids,
                              @RequestParam(value="imagenes") MultipartFile[] imagenes,
                              RedirectAttributes attr, Model model) throws IOException {

        if (bindingResult.hasErrors()) {
            model.addAttribute("listdistritos",distritoRepository.findAll());
            return "Administrador/Sede/editarSedes";
        } else {
            //VALIDACIONES Fotos

            //Se verifica que se haya subido al menos una foto al crear un formulario
            if(sede.getId()==0 && imagenes[0].getContentType().equals("application/octet-stream")){

                model.addAttribute("err","Se debe de tener al menos 1 imagen");
                model.addAttribute("listdistritos",distritoRepository.findAll());
                return "Administrador/Sede/editarSedes";

            }
            //Se verifica que la sede no se quede sin fotos al eliminar en el form de editar
            int cantidadfotos = fotoRepository.buscarFotosSede(sede.getId()).size();
            if(cantidadfotos==1 && !ids[0].equals("a")){
                model.addAttribute("err","Se debe de tener al menos 1 imagen");
                model.addAttribute("listdistritos",distritoRepository.findAll());
                Optional<Sede> optSedeEncon = sedeRepository.findById(sede.getId());
                Sede sedeencon = optSedeEncon.get();
                model.addAttribute("sede", sedeencon);

                List<Foto> fotos = fotoRepository.buscarFotosSede(sede.getId());

                model.addAttribute("imagenes", fotos);
                return "Administrador/Sede/editarSedes";
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
                            model.addAttribute("listdistritos",distritoRepository.findAll());
                            if (sede.getId() != 0) {
                                Optional<Sede> optSedeEncon = sedeRepository.findById(sede.getId());
                                Sede sedeencon = optSedeEncon.get();
                                model.addAttribute("sede", sedeencon);

                                List<Foto> fotos = fotoRepository.buscarFotosSede(sede.getId());

                                model.addAttribute("imagenes", fotos);
                            }
                            return "Administrador/Sede/editarSedes";
                        }
                        break;

                    default:
                        model.addAttribute("err","Solo puede subir imagenes con formato jpg ,jpeg y png");
                        model.addAttribute("listdistritos",distritoRepository.findAll());
                        if (sede.getId() != 0) {
                            Optional<Sede> optSedeEncon = sedeRepository.findById(sede.getId());
                            Sede sedeencon = optSedeEncon.get();
                            model.addAttribute("sede", sedeencon);

                            List<Foto> fotos = fotoRepository.buscarFotosSede(sede.getId());

                            model.addAttribute("imagenes", fotos);
                        }
                        return "Administrador/Sede/editarSedes";

                }
            }


            Persona persona = (Persona) session.getAttribute("usuario");
            if (sede.getId() == 0) {
                attr.addFlashAttribute("msg", "0");

                //agregar imagenes

                sedeRepository.save(sede);
                //se busca el id del objeto sede creado
                Sede sedeCreada=sedeRepository.findTopByOrderByIdDesc();
                System.out.println("\nImágenes a Agregar: " + imagenes.length);
                if(!imagenes[0].getOriginalFilename().equals("")){
                    int i =1;
                    for(MultipartFile img : imagenes){
                        System.out.println("Nombre: " + img.getOriginalFilename());
                        System.out.println("Tipo: " + img.getContentType());
                        MultipartFile file_aux = fileService.formatearArchivo(img,"foto");
                        if(fileService.subirArchivo(file_aux)){
                            System.out.println("Archivo subido correctamente");
                            Foto foto = new Foto();
                            foto.setEstado(1);
                            foto.setIdpersona(persona.getId());
                            foto.setSede(sedeCreada);
                            foto.setNumero(i);
                            foto.setRuta(fileService.obtenerUrl(file_aux.getOriginalFilename()));
                            fotoRepository.save(foto);
                        }else{
                            System.out.println("El archivo"+img.getOriginalFilename()+"No se pude subir de manera correcta");
                        }
                        i++;
                    }

                }

                return "redirect:/admin/sedes/lista";
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
                Sede sedeCreada=sedeRepository.findById(sede.getId()).get();
                System.out.println("\nImágenes a Agregar: " + imagenes.length);
                if(!imagenes[0].getOriginalFilename().equals("")){
                    int i =1;
                    for(MultipartFile img : imagenes){
                        System.out.println("Nombre: " + img.getOriginalFilename());
                        System.out.println("Tipo: " + img.getContentType());
                        MultipartFile file_aux = fileService.formatearArchivo(img,"foto");
                        if(fileService.subirArchivo(file_aux)){
                            System.out.println("Archivo subido correctamente");
                            Foto foto = new Foto();
                            foto.setEstado(1);
                            foto.setIdpersona(persona.getId());
                            foto.setSede(sedeCreada);
                            foto.setNumero(i);
                            foto.setRuta(fileService.obtenerUrl(file_aux.getOriginalFilename()));
                            fotoRepository.save(foto);
                        }else{
                            System.out.println("El archivo"+img.getOriginalFilename()+"No se pude subir de manera correcta");
                        }
                        i++;
                    }

                }

                sedeRepository.save(sede);
                attr.addFlashAttribute("msg", "1");
                return "redirect:/admin/sedes/lista";
            }
        }
    }

    @GetMapping("/borrar")
    public String borrarSede(Model model,
                                @RequestParam("idsede") int idsede,
                                RedirectAttributes attr) {

        //buscar las fotos a eliminar en el filemanager de Azure

        try{
            List<Foto> fotos = fotoRepository.buscarFotosSede(idsede);

            if(fotos.size()!=0){
                //eliminamos las fotos una por una del filemanager de Azure y de la base de datos
                for (Foto fot : fotos) {
                    String [] urlfoto=fot.getRuta().split("/telemillonario/");
                    String nombrefoto= urlfoto[1];
                    fileService.eliminarArchivo(nombrefoto);
                    fotoRepository.deleteById(fot.getId());
                }

                //eliminamos la funcion de la base de datos
                sedeRepository.deleteById(idsede);

                attr.addFlashAttribute("msg", "2");
            }else{
                attr.addFlashAttribute("msg", "3");
            }
            return "redirect:/admin/sedes/lista";
        }catch (Exception e){

            attr.addFlashAttribute("msg", "4");
            return "redirect:/admin/sedes/lista";
        }



    }

}
