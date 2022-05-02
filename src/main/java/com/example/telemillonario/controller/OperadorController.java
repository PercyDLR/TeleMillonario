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
@RequestMapping("/Operadores")
public class OperadorController {

    @Autowired
    RolRepository rolRepository;
    @Autowired
    PersonaRepository personaRepository;
    @Autowired
    SedeRepository sedeRepository;

    @GetMapping("")
    public String listarOperadores(Model model) {
        List<Persona> operadores = personaRepository.listarOperadores();
        //considerando una cantidad de Operador inicial base de 5
        int cantidad_por_pagina = 5;//modificar en caso se requiera variar la cantidad de operadores por pagina
        if(operadores.size()%cantidad_por_pagina==0){
            //quiere decir que tengo un multiplo
            model.addAttribute("paginacion",(operadores.size())/cantidad_por_pagina);
        }else{
            //quiere decir que tengo un número No multiplo de la cantidad de elementos por página
            model.addAttribute("paginacion",(operadores.size()/cantidad_por_pagina)+1);
        }
        model.addAttribute("listaOperadores", operadores);
        //Usar en caso se requiera
        //model.addAttribute("listaOperadores",operadores.subList(1,cantidad_por_pagina));
        return "Administrador/Operador/listaOperadores";
    }

    @GetMapping(value = {"/listar","/listar/{num}"})
    public String listarPorPagina(@PathVariable("num") Optional<String> num,Model model,RedirectAttributes attr){
        if(num.isPresent()){
            try{
                //verificar si el numero se encuentra dentro del rango de la paginacion
                int cantidad_por_pagina = 5;
                int num_paginas;
                int num_pagina = Integer.parseInt(num.get());
                if(personaRepository.listarOperadores().size()%cantidad_por_pagina==0){
                    num_paginas = personaRepository.listarOperadores().size()/cantidad_por_pagina;
                }else{
                    num_paginas = (personaRepository.listarOperadores().size()/cantidad_por_pagina) + 1;
                }
                //una vez obtenidos los numeros
                if(num_pagina>0&&num_pagina<=num_paginas){
                    List<Persona> operadores = personaRepository.listarOperadores();
                    List<Persona> operadores_filtrados = operadores.subList((cantidad_por_pagina*(num_pagina-1)+1),(cantidad_por_pagina*(num_pagina-1)+cantidad_por_pagina+1));
                    model.addAttribute("listaOperadores",operadores_filtrados);
                    model.addAttribute("paginacion",num_paginas);
                    return "Administrador/Operador/listaOperadores";
                }else{
                    attr.addFlashAttribute("msg","Debe ingresar un numero de página entre"+1+" y "+num_paginas);
                    return "redirect:/Operadores";
                }
            }catch (Exception e){
                attr.addFlashAttribute("msg","Debe ingresar un numero para la página");
                return "redirect:/Operadores";
            }
        }else{
            attr.addFlashAttribute("msg","Debe Ingresar un valor de paginación");
            return "redirect:/Operadores";
        }
    }

    @GetMapping(value = "/crear")
    public String crearOperador(Model model,@ModelAttribute("operador") Persona operador) {
        model.addAttribute("listaSedes", sedeRepository.findByEstado(1));
        return "Administrador/Operador/agregarOperadores";
    }

    @PostMapping(value = "/buscar")
    public String filtrarOperador(Model model,@RequestParam("filtro") Optional<String> filtro,@RequestParam("nombre") Optional<String> nombre,RedirectAttributes attr){
        if(!filtro.isPresent()&&!nombre.isPresent()){
            return "redirect:/Operadores/";
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
            return "Administrador/Operador/listaOperadores";
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
                    return "Administrador/Operador/editarOperadores";
                } else {
                    attr.addFlashAttribute("msg", "No existe el operador con el ID" + id);
                    return "redirect:/Operadores/";
                }
            } catch (Exception e) {
                attr.addFlashAttribute("msg", "Envió un ID inválido");
                return "redirect:/Operadores/";
            }
        } else {
            attr.addFlashAttribute("msg", "Se envió el ID vacío");
            return "redirect:/Operadores/";
        }
    }

    @PostMapping(value = "/guardar")
    public String guardarOperador(@ModelAttribute("operador") @Valid Persona operador, BindingResult bindingResult, RedirectAttributes attr) {
        try {
            int id = operador.getId();
            if (personaRepository.existsById(id)) {
                if(bindingResult.hasErrors()||operador.getIdsede()==null){
                    if(operador.getIdsede()==null){
                        attr.addFlashAttribute("msg","La sede del operador no puede quedar vacía");
                    }
                    return "Administrador/Operador/editarOperadores";
                }else{
                    Optional<Persona> aux = personaRepository.findById(id);
                    Persona op = aux.get();
                    //basado en lo siguiente
                    //DNI / Nombre / Apellido son campos no editables
                    op.setIdsede(operador.getIdsede());
                    attr.addFlashAttribute("msg","Se actualizó el operador de manera exitosa");
                    return "redirect:/Operadores/";
                }
            } else {
                if(bindingResult.hasErrors()||operador.getIdsede()==null){
                    if(operador.getIdsede()==null){
                        attr.addFlashAttribute("msg","La sede del operador no puede quedar vacía");
                    }
                    return "Administrador/Operador/editarOperadores";
                }else{
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
                    return "redirect:/Operadores/";
                }
            }
        } catch (Exception e) {
            attr.addFlashAttribute("msg", "Envió un ID inválido");
            return "Administrador/Operador/editarOperadores";//solo será valido cuando se encuentre en el formulario de editar
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
                    return "redirect:/Operadores/";
                }else{
                    attr.addFlashAttribute("msg", "El operador con ID:"+id+" no se encuentra presente");
                    return "redirect:/Operadores/";
                }
            }catch (Exception e){
                attr.addFlashAttribute("msg", "Envió un ID inválido");
                return "redirect:/Operadores/";
            }
        }else{
            attr.addFlashAttribute("msg", "Se envió el ID vacío");
            return "redirect:/Operadores/";
        }
    }

}