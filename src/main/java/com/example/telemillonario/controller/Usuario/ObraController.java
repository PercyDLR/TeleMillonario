package com.example.telemillonario.controller.Usuario;

import com.azure.core.annotation.Get;
import com.example.telemillonario.entity.Funcion;
import com.example.telemillonario.entity.Funciongenero;
import com.example.telemillonario.entity.Genero;
import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.repository.FotoRepository;
import com.example.telemillonario.repository.FuncionGeneroRepository;
import com.example.telemillonario.repository.FuncionRepository;
import com.example.telemillonario.repository.GeneroRepository;
import com.example.telemillonario.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cartelera")
public class ObraController {

    @Autowired
    FotoRepository fotoRepository;

    @Autowired
    FileService fileService;

    @Autowired
    FuncionGeneroRepository funcionGeneroRepository;

    @Autowired
    GeneroRepository generoRepository;

    @Autowired
    FuncionRepository funcionRepository;

    //Variables Importantes
    int funcionesxpagina=12;

    @GetMapping(value = "")
    public String listarFuncionesGenero(Model model,
                                        @RequestParam(value = "restriccionEdad", required = false, defaultValue = "0") Integer restriccionEdad,
                                        @RequestParam(value = "genero", required = false, defaultValue = "") Genero genero,
                                        @RequestParam(value = "busqueda", required = false, defaultValue = "") String busqueda,
                                        @RequestParam(value = "pag", required = false, defaultValue = "0") String pag){
        int pagina;
        try{
            pagina = Integer.parseInt(pag);
        }catch(Exception e) {
            pagina=0;
        }

        List<Funciongenero> listaFuncionGenero = funcionGeneroRepository.buscarFuncionGeneroPorFiltros("","","");
        HashMap<Funcion, ArrayList<Genero>> funcionGenero = new HashMap<>();
        for (Funciongenero f : listaFuncionGenero) {
            if (!funcionGenero.containsKey(f.getIdfuncion())) {
                funcionGenero.put(f.getIdfuncion(), new ArrayList<>());
            }
            if (!funcionGenero.get(f.getIdfuncion()).contains(f.getIdgenero())) {
                ArrayList<Genero> listaAGuardar = funcionGenero.get(f.getIdfuncion());
                listaAGuardar.add(f.getIdgenero());
                funcionGenero.put(f.getIdfuncion(), listaAGuardar);
            }
        }

        //System.out.println("Lista Funcion Genero");
        //for (Map.Entry<Funcion, ArrayList<Genero>> h : funcionGenero.entrySet()) {

        //    System.out.println(h.getKey().getId());
        //    for (Genero g : h.getValue()) {
        //        System.out.println(g.getId());
        //    }
        //    System.out.println("=====================");
        //}

        HashMap<String, Integer> listaNombres = new HashMap<>();
        for (Map.Entry<Funcion, ArrayList<Genero>> h : funcionGenero.entrySet()) {
            Funcion funcionAEvaluar = h.getKey();
            if (!listaNombres.containsKey(funcionAEvaluar.getNombre())) {
                listaNombres.put(funcionAEvaluar.getNombre(),0);
            }
        }
        //System.out.println("Lista de nombres de funciones");
        //for (Map.Entry<String, Integer> h : listaNombres.entrySet()) {

        //    System.out.println(h.getKey());
        //    System.out.println(h.getValue());
        //    System.out.println("=====================");
        //}

        ArrayList<Funcion> listaFuncionesAEliminar = new ArrayList<>();
        for (Map.Entry<Funcion, ArrayList<Genero>> h : funcionGenero.entrySet()) {
            Funcion funcionAEvaluar = h.getKey();
            for (Map.Entry<String, Integer> j : listaNombres.entrySet()) {
                String nombre = j.getKey();
                if (funcionAEvaluar.getNombre().equals(nombre)) {
                    if (j.getValue() == 0) {
                        listaNombres.put(nombre,1);
                    } else {
                        listaFuncionesAEliminar.add(h.getKey());
                    }
                }
            }
        }
        for (Funcion a : listaFuncionesAEliminar) {
            funcionGenero.remove(a);
        }

        //System.out.println("Lista Funcion Genero actualizada");
        //for (Map.Entry<Funcion, ArrayList<Genero>> h : funcionGenero.entrySet()) {

        //    System.out.println(h.getKey().getId());
        //    for (Genero g : h.getValue()) {
        //        System.out.println(g.getId());
        //    }
        //    System.out.println("=====================");
        //}

        //Una vez obtengo la lista de obras no repetidas, requiero indicar aquellas que se enviaran
        int tamanhoLista = funcionGenero.size();
        HashMap<Funcion, ArrayList<Genero>> listaFuncionesGeneroAEnviar = new HashMap<>();
        int i = pagina;
        for (Map.Entry<Funcion, ArrayList<Genero>> h : funcionGenero.entrySet()) {
            if (i < pagina + funcionesxpagina && i <= tamanhoLista) {
                listaFuncionesGeneroAEnviar.put(h.getKey(),h.getValue());
            }
            i = i + 1;
        }

        System.out.println("Lista Funcion Genero actualizada");
        System.out.println(listaFuncionesGeneroAEnviar.size());
        for (Map.Entry<Funcion, ArrayList<Genero>> h : listaFuncionesGeneroAEnviar.entrySet()) {

            System.out.println(h.getKey().getId());
            for (Genero g : h.getValue()) {
                System.out.println(g.getId());
            }
            System.out.println("=====================");
        }

        model.addAttribute("genero", genero);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("restriccionEdad",restriccionEdad);

        model.addAttribute("pagActual",pagina);
        model.addAttribute("pagTotal",(int) Math.ceil(listaFuncionGenero.size()/funcionesxpagina));
        model.addAttribute("listaFuncionesGenero", listaFuncionesGeneroAEnviar);
        model.addAttribute("generos", generoRepository.findAll());
        return "usuario/obras/carteleraObras";
    }

    @PostMapping("/BusquedaYFiltros")
    String busqueda(@RequestParam(value = "restriccionEdad", defaultValue = "0") Integer restriccionEdad,
                    @RequestParam(value = "genero", defaultValue = "") Genero genero,
                    @RequestParam(value = "busqueda", defaultValue = "") String busqueda,
                    @RequestParam(value = "pag",defaultValue = "0") String pag,
                    RedirectAttributes attr){

        return "redirect:/cartelera?restriccionEdad="+restriccionEdad+"&genero="+genero.getId()+"&busqueda="+busqueda+"&pag="+pag;
    }

}
