package com.example.telemillonario.controller;

import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.entity.Rol;
import com.example.telemillonario.repository.PersonaRepository;
import com.example.telemillonario.repository.RolRepository;
import com.example.telemillonario.repository.SedeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/operador")
public class OperadorController {

    @Autowired
    RolRepository rolRepository;
    @Autowired
    PersonaRepository personaRepository;
    @Autowired
    SedeRepository sedeRepository;

    @GetMapping(value = {"", "/", "lista"})
    public String listarOperadores(Model model) {
        List<Persona> operadores = personaRepository.listarOperadores();
        model.addAttribute("listaOperadores", operadores);
        return "/Operador/listaOperadores";
    }

    @GetMapping(value = "/crear")
    public String crearOperador(Model model,@ModelAttribute("operador") Persona operador) {
        model.addAttribute("listaSedes", sedeRepository.findByEstado(1));
        return "/Operador/agregarOperadores";
    }

    @PostMapping(value = "/buscar")
    public String filtrarOperador(Model model,@RequestParam("filtro") Optional<String> filtro,@RequestParam("nombre") Optional<String> nombre,RedirectAttributes attr){
        if(!filtro.isPresent()&&!nombre.isPresent()){
            return "redirect:/operador/lista";
        }else{
            //algo está buscando
            if(!filtro.isPresent()&&nombre.isPresent()){
                //busqueda solo por nombre
                attr.addFlashAttribute("msg","resultado filtrado por nombre");
                model.addAttribute("listaOperadores",personaRepository.listarOperadoresPorNombre(nombre.get()));
            }
            if(!nombre.isPresent()&&filtro.isPresent()){
                //busqueda solo por el filtro
                attr.addFlashAttribute("msg","resultado filtrado por "+filtro.get());
                model.addAttribute("listaOperadores",personaRepository.listarOperadoresPorFiltro(filtro.get()));
            }
            if(nombre.isPresent()&&filtro.isPresent()){
                //busqueda por filtro y por nombre
                attr.addFlashAttribute("msg"," resultado filtrado por"+filtro.get()+" y por nombre");
                model.addAttribute("listOperadores",personaRepository.listarOperadoresPorFiltroyNombre(nombre.get(),filtro.get()));
            }
            return "Operador/listaOperadores";
        }
    }

    @GetMapping(value = "/editar")
    public String editarOperador(Model model, @RequestParam("id") Optional<String> optid, RedirectAttributes attr, @ModelAttribute("operador") Persona operador_b) {
        if (optid.isPresent()) {
            try {
                int id = Integer.parseInt(optid.get());
                if (personaRepository.existsById(id)) {
                    Optional<Persona> operador = personaRepository.findById(id);
                    model.addAttribute("listaSedes", sedeRepository.findByEstado(1));
                    model.addAttribute("operador", operador.get());
                    return "/Operador/editarOperadores";
                } else {
                    attr.addFlashAttribute("msg", "No existe el operador con el ID" + id);
                    return "redirect:/operador/lista";
                }
            } catch (Exception e) {
                attr.addFlashAttribute("msg", "Envió un ID inválido");
                return "redirect:/operador/lista";
            }
        } else {
            attr.addFlashAttribute("msg", "Se envió el ID vacío");
            return "redirect:/operador/lista";
        }
    }

    @PostMapping(value = "/guardar")
    public String guardarOperador(@ModelAttribute("operador") @Valid Persona operador, BindingResult bindingResult, RedirectAttributes attr) {
        try {
            int id = operador.getId();
            if (personaRepository.existsById(id)) {
                if(bindingResult.hasErrors()||operador.getIdsede()==null){
                    attr.addFlashAttribute("msg","La sede del operador no puede quedar vacía");
                    return "/Operador/editarOperadores";
                }else{
                    Optional<Persona> aux = personaRepository.findById(id);
                    Persona op = aux.get();
                    //basado en lo siguiente
                    //DNI / Nombre / Apellido son campos no editables
                    op.setIdsede(operador.getIdsede());
                    attr.addFlashAttribute("msg","Se actualizó el operador de manera exitosa");
                    return "redirect:/operador/lista";
                }
            } else {
                //configuración en activo
                operador.setEstado(1);
                //Creación de rol
                Rol rol = new Rol();
                rol.setId(3);
                rol.setEstado(1);//Opcional
                rol.setNombre("Operador");//Opcional
                //asignación de rol
                operador.setIdrol(rol);
                personaRepository.save(operador);
                attr.addFlashAttribute("msg", "Se creo de el operador de manera exitosa");
                return "redirect:/operador/lista";
            }
        } catch (Exception e) {
            attr.addFlashAttribute("msg", "Envió un ID inválido");
            return "/Operador/editarOperadores";//solo será valido cuando se encuentre en el formulario de editar
        }
    }

    @PostMapping(value = "/borrar")
    public String borrarOperador(@RequestParam("id") Optional<String> optid, RedirectAttributes attr){
        if(optid.isPresent()) {
            try{
                int id = Integer.parseInt(optid.get());
                if(personaRepository.existsById(id)){
                    Optional<Persona> aux = personaRepository.findById(id);
                    Persona operador = aux.get();
                    operador.setEstado(0);//Borrado lógico
                    personaRepository.save(operador);
                    attr.addFlashAttribute("msg","El operador ha sido borrado exitosamente");
                    return "redirect:/operador/lista";
                }else{
                    attr.addFlashAttribute("msg", "El operador con ID:"+id+" no se encuentra presente");
                    return "redirect:/operador/lista";
                }
            }catch (Exception e){
                attr.addFlashAttribute("msg", "Envió un ID inválido");
                return "redirect:/operador/lista";
            }
        }else{
            attr.addFlashAttribute("msg", "Se envió el ID vacío");
            return "redirect:/operador/lista";
        }
    }

}
