package com.example.telemillonario.controller.Operador;

import com.example.telemillonario.entity.Foto;
import com.example.telemillonario.entity.Funcion;
import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.entity.Sala;
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
import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.List;

@Controller
@RequestMapping("/operador")
public class OperadController {
    @Autowired
    FotoRepository fotoRepository;

    @Autowired
    FileService fileService;

    @Autowired
    PersonaRepository personaRepository;


    @Autowired
    SedeRepository sedeRepository;

    @Autowired
    SalaRepository salaRepository;

    @Autowired
    FuncionRepository funcionRepository;

    @GetMapping("/perfil")
    public String verPerfilUsuario(@ModelAttribute("usuario") Persona usuario, Model model, HttpSession session) {
        usuario = (Persona) session.getAttribute("usuario");
        model.addAttribute("usuario", usuario);
        return "Operador/verPerfilOperador";
    }

    @GetMapping("/perfil/editar")
    public String editarPerfilUsuario(@ModelAttribute("usuario") Persona usuario, Model model, HttpSession session) {
        usuario = (Persona) session.getAttribute("usuario");
        model.addAttribute("usuario", usuario);
        return "Operador/editarPerfilOperador";
    }

    @PostMapping("/perfil/guardar")
    public String guardarPerfilUsuario(@ModelAttribute("usuario") @Valid Persona usuario,
                                       @RequestParam("imagen") MultipartFile img, Model model,
                                       RedirectAttributes a, HttpSession session) {


        Persona usuarioSesion = (Persona) session.getAttribute("usuario");



        if (usuario.getId().equals(usuarioSesion.getId())) {



            usuarioSesion.setTelefono(usuario.getTelefono());

            // Validar imágen
            switch (img.getContentType()) {

                case "image/jpeg":
                case "image/png":
                case "application/octet-stream":
                    break;
                default:
                    usuario.setNombres(usuarioSesion.getNombres());
                    usuario.setApellidos(usuarioSesion.getApellidos());
                    usuario.setDni(usuarioSesion.getDni());
                    usuario.setCorreo(usuarioSesion.getCorreo());

                    model.addAttribute("err", "Solo se deben de enviar imágenes");
                    model.addAttribute("imagenes", fotoRepository.findByIdpersonaOrderByNumero(usuarioSesion.getId()));
                    return "Operador/editarPerfilOperador";
            }

            if (img.getOriginalFilename().length() != 0) {
                try {
                    List<Foto> fotosEnDB = fotoRepository.findByIdpersonaOrderByNumero(usuario.getId());
                    // Borado de fotos
                    if (fotosEnDB.size() > 0) {
                        for (Foto foto : fotosEnDB) {
                            // Vacía las fotos en DB
                            fotoRepository.delete(foto);

                            // Borrado del fileService
                            String ruta = foto.getRuta();
                            String nombreFoto = ruta.substring(ruta.lastIndexOf('/') + 1);
                            //System.out.println("Nombre de la foto a eliminar: " + nombreFoto);
                            fileService.eliminarArchivo(nombreFoto);
                        }
                    }

                    Foto fotoPerfil = new Foto();

                    // Guardado en el Contenedor de archivos
                    MultipartFile newImg = fileService.formatearArchivo(img, "usuario");

                    if (fileService.subirArchivo(newImg)) {
                        fotoPerfil.setRuta(fileService.obtenerUrl(newImg.getOriginalFilename()));
                    }

                    // Guardado de la Foto en DB
                    fotoPerfil.setNumero(0);
                    fotoPerfil.setIdpersona(usuario);
                    fotoPerfil.setEstado(1);

                    fotoRepository.save(fotoPerfil);
                    session.setAttribute("fotoPerfil", fotoPerfil.getRuta());

                } catch (Exception e) {
                    e.getMessage();
                    System.out.println(e.getMessage());
                    a.addFlashAttribute("msg", -2);
                    return "redirect:/operador/perfil";
                }
            }

            //Actualiza la DB
            personaRepository.save(usuarioSesion);

            //Actualiza la Sesión
            session.setAttribute("usuario", usuarioSesion);

            a.addFlashAttribute("msg", 0);
        } else { //No debe dejar guardar el usuario editado
            a.addFlashAttribute("msg", -1);
        }

        return "redirect:/operador/perfil";


    }

    float salasporpagina = 6;
    @GetMapping("/salas")
    public String funcionesSalas(Model model,@RequestParam(value = "pag", required = false) String pag, HttpSession session) {

        int pagina;

        try {
            pagina = Integer.parseInt(pag);
        } catch (Exception e) {
            pagina = 0;
        }

        Persona usuarioSesion = (Persona) session.getAttribute("usuario");
        int idsede=usuarioSesion.getIdsede().getId();

        LinkedHashMap<Sala, List<Funcion>> salasConFunciones = new LinkedHashMap<>();
        List<Sala> listSalas = salaRepository.buscarSalasSedexpag(idsede, 1,(int) salasporpagina * pagina, (int) salasporpagina);

        for (Sala sala: listSalas ){
            List<Funcion> funcionesSala = funcionRepository.buscarFuncionesPorSedeySala(idsede, sala.getId());
            salasConFunciones.put(sala,funcionesSala);
        }


        int cantSalas=salaRepository.buscarSalasTotal(idsede,1).size();

        LocalDate fechahoy = LocalDate.now();
        model.addAttribute("fechactual",fechahoy);
        model.addAttribute("listadosalasconfunc",salasConFunciones);
        model.addAttribute("pagActual", pagina);
        model.addAttribute("pagTotal", (int) Math.ceil(cantSalas / salasporpagina));

        return "Operador/vistaSalas";
    }


}
