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
    public String guardarSede(@ModelAttribute("sede") @Valid Sede sede,HttpSession session,
                              @RequestParam(value="imagenes") MultipartFile[] imagenes,BindingResult bindingResult, RedirectAttributes attr, Model model) throws IOException {

        if (bindingResult.hasErrors()) {
            model.addAttribute("listdistritos",distritoRepository.findAll());
            return "Administrador/Sede/editarSedes";
        } else {
            if (sede.getId() == 0) {
                attr.addFlashAttribute("msg", "Sede creado exitosamente");
                Persona persona = (Persona) session.getAttribute("usuario");
                //agregar imagenes

                sedeRepository.save(sede);
                //se busca el id del objeto sede creado
                Sede sedeCreada=sedeRepository.findTopByOrderByIdDesc();
                System.out.println("\nImágenes a Agregar: " + imagenes.length);
                if(!imagenes[0].getOriginalFilename().equals("application/octet-stream")){
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

                sedeRepository.save(sede);
                attr.addFlashAttribute("msg", "Sede actualizado exitosamente");
                return "redirect:/admin/sedes/lista";
            }
        }
    }

//    @GetMapping("/disponibilidad")
//    public String disponibilidadSala(@RequestParam("id") int id,@RequestParam(value = "idsede",required = false) String idsede, RedirectAttributes a) {
//        Optional<Sala> optionalSala = salaRepository.findById(id);
//        if (optionalSala.isPresent()) {
//            Sala sala = optionalSala.get();
//            if (sala.getEstado() == 1) {
//                sala.setEstado(0);
//                salaRepository.save(sala);
//            } else {
//                sala.setEstado(1);
//                salaRepository.save(sala);
//            }
//            a.addFlashAttribute("msg", "2");
//            a.addFlashAttribute("identificador", sala.getIdentificador());
//        }
//        return "redirect:/admin/salas?idsede="+idsede;
//    }

}
