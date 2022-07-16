package com.example.telemillonario.controller.Administrador;

import com.example.telemillonario.entity.*;
import com.example.telemillonario.repository.*;
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
import java.util.ArrayList;
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

    @Autowired
    SalaRepository salaRepository;

    @Autowired
    PersonaRepository personaRepository;

    float sedesporpagina=6;
    @GetMapping(value = {"", "/","/lista"})
    public String listaSedes(Model model, @RequestParam(value = "pag",required = false) String pag,
                            @RequestParam(value="parametro",required = false,defaultValue = "") String parametro,
                             @RequestParam(value="buscador",required = false,defaultValue = "") String buscador){
        int pagina;
        int estado=1;
        int cantSedes = sedeRepository.cantSedesTotalAdmin();
        try{
            pagina = Integer.parseInt(pag);
        }catch(Exception e) {
            pagina=0;
        }

        if (parametro.equals("")) { // verifica que no esté vacío

            List<Foto> listSedesConFoto= fotoRepository.listadoSedesAdmin(estado,(int)sedesporpagina*pagina, (int)sedesporpagina);
            model.addAttribute("listaSedes",listSedesConFoto);

        }else{
            parametro = parametro.toLowerCase();
            List<Foto> listSedesConFoto = switch (buscador) {
                case "nombre" -> fotoRepository.buscarSedePorNombre(parametro, estado, (int)sedesporpagina*pagina, (int)sedesporpagina);
                case "distrito" -> fotoRepository.buscarSedePorDistrito(parametro, estado, (int)sedesporpagina*pagina, (int)sedesporpagina);
                default -> fotoRepository.listadoSedesAdmin(estado,(int)sedesporpagina*pagina, (int)sedesporpagina);
            };
            model.addAttribute("listaSedes",listSedesConFoto);
        }

        model.addAttribute("parametro", parametro);
        model.addAttribute("buscador", buscador);

        model.addAttribute("pagActual",pagina);
        model.addAttribute("pagTotal",(int) Math.ceil(cantSedes/sedesporpagina));
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
    public String editarSede(Model model, @RequestParam("idsede") int idsede) {

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

        // Se buscan errores de validación
        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult.getFieldError());
            model.addAttribute("err","Bubo un problema en el formulario");

            model.addAttribute("listdistritos",distritoRepository.findAll());
            model.addAttribute("sede", sede);
            return "Administrador/Sede/editarSedes";
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

                        model.addAttribute("listdistritos",distritoRepository.findAll());
                        if (sede.getId() != 0){
                            model.addAttribute("imagenes",fotoRepository.buscarFotosSede(sede.getId()));
                        }
                        return "Administrador/Sede/editarSedes";
                    }
                    break;

                default:
                    model.addAttribute("err","Solo se deben de enviar imágenes");

                    model.addAttribute("listdistritos",distritoRepository.findAll());
                    if (sede.getId() != 0){
                        model.addAttribute("imagenes",fotoRepository.buscarFotosSede(sede.getId()));
                    }
                    return "Administrador/Sede/editarSedes";
            }
        }

        //-----------------------------------------------
        //         Guardado de Datos de la Sede
        //-----------------------------------------------
        try {
            sede = sedeRepository.save(sede);

        } catch (Exception e){
            System.out.println(e.getMessage());
            attr.addFlashAttribute("err","Hubo un problema al guardar los datos de la Sede");
            return "redirect:/admin/sedes/lista";
        }

        //-----------------------------------------------
        //    Validación de Fotos a Eliminar en la DB
        //-----------------------------------------------

        // Número de fotos Ya Guardadas en la DB
        int fotosGuardadas = 0;
        List<Foto> fotosParaEliminar = new ArrayList<>();

        // Se obtienen las fotos guardadas en DB
        List<Foto> fotosEnDB = fotoRepository.buscarFotosSede(sede.getId());
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

        // Verifica que la casilla de imágenes no se vaya a quedar vacía
        if (fotosGuardadas + imagenes.length == 1 && imagenes[0].getContentType().equals("application/octet-stream")) {
            model.addAttribute("err", "Se debe de tener al menos 1 imagen");

            model.addAttribute("listdistritos",distritoRepository.findAll());
            model.addAttribute("imagenes", fotoRepository.buscarFotosSede(sede.getId()));
            return "Administrador/Sede/editarSedes";
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
            attr.addFlashAttribute("msg1", "Sede Guardada Exitosamente");
            return "redirect:/admin/sedes/lista";
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
                foto.setIdsede(sede);
                foto.setNumero(fotosGuardadas + i);

                // Guardar en el Servidor
                MultipartFile imgRenombrada = fileService.formatearArchivo(img, "sede");
                if(fileService.subirArchivo(imgRenombrada)){

                    // Si se guarda exitosamente, se obtiene el url de la foto
                    foto.setRuta(fileService.obtenerUrl(imgRenombrada.getOriginalFilename()));
                } else {
                    attr.addFlashAttribute("err","No se pudo conectar con el Contenedor De Archivos");
                    return "redirect:/admin/sedes/lista";
                }

                // Guardado en la DB
                System.out.println(imgRenombrada.getOriginalFilename());
                fotoRepository.save(foto);

            } catch (Exception e){
                System.out.println(e.getMessage());
                attr.addFlashAttribute("err", "Hubo un problema al guardar las Imagenes");
                return "redirect:/admin/sedes/lista";
            }
        }
        attr.addFlashAttribute("msg1","Sede Guardada Exitosamente");
        return "redirect:/admin/sedes/lista";
    }

    @GetMapping("/borrar")
    public String borrarSede(Model model,
                                @RequestParam("idsede") int idsede,
                                RedirectAttributes attr) {

        //buscar las fotos a eliminar en el filemanager de Azure

        try{
            int a=salaRepository.valCantSal(idsede);
            int b=personaRepository.valborrSedePers(idsede);
            if( a!=0 || b!=0){
                attr.addFlashAttribute("msg", "5");
                return "redirect:/admin/sedes/lista";
            }
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
