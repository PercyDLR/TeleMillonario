package com.example.telemillonario.controller.Usuario;

import com.example.telemillonario.entity.Foto;
import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.repository.FotoRepository;
import com.example.telemillonario.repository.PersonaRepository;
import com.example.telemillonario.service.FileService;
import com.example.telemillonario.validation.Perfil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class UsuarioController {

    @Autowired
    PersonaRepository personaRepository;

    @Autowired
    FileService fileService;

    @Autowired
    FotoRepository fotoRepository;

    @GetMapping("")
    public String paginaPrincipal(){
        return "vistaPrincipal";
    }

    @GetMapping("/perfil")
    public String verPerfilUsuario(@ModelAttribute("usuario") Persona usuario, Model model, HttpSession session) {
        usuario = (Persona) session.getAttribute("usuario");
        model.addAttribute("usuario",usuario);
        return "usuario/verPerfil";
    }

    @GetMapping("/perfil/editar")
    public String editarPerfilUsuario(@ModelAttribute("usuario") Persona usuario, Model model, HttpSession session) {
        usuario = (Persona) session.getAttribute("usuario");
        model.addAttribute("usuario",usuario);
        return "usuario/editarPerfil";
    }

    @PostMapping("/perfil/guardar")
    public String guardarPerfilUsuario(@ModelAttribute("usuario") @Valid Persona usuario,
                                       @RequestParam("imagen") MultipartFile img,
                                       BindingResult bindingResult, Model model,
                                       RedirectAttributes a, HttpSession session) {
        //@Validated(Perfil.class)

        Persona usuarioSesion = (Persona) session.getAttribute("usuario");
        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult.getFieldError());
            return "usuario/editarPerfil";
        } else {

            if (usuario.getId().equals(usuarioSesion.getId())) {

                // Validación Fechas
                LocalDate fechaActual = LocalDate.now();
                LocalDate fechaRegistrada = usuario.getNacimiento();
                Period period = Period.between(fechaRegistrada, fechaActual);

                System.out.print(period.getYears() + " years,");
                System.out.print(period.getMonths() + " months,");
                System.out.print(period.getDays() + " days");

                if (period.getYears() < 0 || period.getMonths() < 0 || (period.getDays() <= 0 && (period.getYears() < 0 || period.getMonths() < 0))) {
                    model.addAttribute("errDate", -1);
                    return "usuario/editarPerfil";
                }

                // Sobreescribe los nuevos valores
                usuarioSesion.setNacimiento(usuario.getNacimiento());
                usuarioSesion.setDireccion(usuario.getDireccion());
                usuarioSesion.setTelefono(usuario.getTelefono());

                // Validar imágen
                switch(img.getContentType()){

                    case "image/jpeg":
                    case "image/png":
                    case "application/octet-stream":
                        break;
                    default:
                        model.addAttribute("err","Solo se deben de enviar imágenes");
                        return "usuario/editarPerfil";
                }

                if (img.getOriginalFilename().length() != 0) {
                    try {
                        List<Foto> fotosEnDB = fotoRepository.findByIdpersonaOrderByNumero(usuario.getId());
                        System.out.println("Error en list foto");

                        // Vacía las fotos en DB
                        if (fotosEnDB.size() > 0){
                            for (Foto foto : fotosEnDB){
                                fotoRepository.delete(foto);
                            }
                        }
                        System.out.println("Error en vacia las fotos en DB");

                        Foto fotoPerfil = new Foto();

                        // Guardado en el Contenedor de archivos
                        MultipartFile newImg = fileService.formatearArchivo(img, "usuario");
                        System.out.println("Error en guardado en contenedor de archivos");

                        if (fileService.subirArchivo(newImg)){
                            fotoPerfil.setRuta(fileService.obtenerUrl(newImg.getOriginalFilename()));
                        }
                        System.out.println("Error en el ultimo if");

                        // Guardado de la Foto en DB
                        fotoPerfil.setNumero(0);
                        fotoPerfil.setIdpersona(usuario.getId());
                        fotoPerfil.setEstado(1);

                        fotoRepository.save(fotoPerfil);
                        session.setAttribute("fotoPerfil",fotoPerfil.getRuta());

                    } catch (Exception e){
                        e.getMessage();
                        System.out.println(e.getMessage());
                        a.addFlashAttribute("msg",-2);
                        return "redirect:/perfil";
                    }
                }

                //Actualiza la DB
                personaRepository.save(usuarioSesion);

                //Actualiza la Sesión
                session.setAttribute("usuario",usuarioSesion);

                a.addFlashAttribute("msg", 0);
            } else { //No debe dejar guardar el usuario editado
                a.addFlashAttribute("msg", -1);
            }

            return "redirect:/perfil";
        }

    }

}
