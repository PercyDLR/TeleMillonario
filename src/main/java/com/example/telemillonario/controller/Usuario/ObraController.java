package com.example.telemillonario.controller.Usuario;

import com.azure.core.annotation.Get;
import com.example.telemillonario.entity.*;
import com.example.telemillonario.repository.*;
import com.example.telemillonario.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

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

    @Autowired
    FuncionElencoRepository funcionElencoRepository;

    //Variables Importantes
    int funcionesxpagina = 12;

    @GetMapping(value = "")
    public String listarFuncionesGenero(Model model,
                                        @RequestParam(value = "restriccionEdad", required = false, defaultValue = "") String restriccionEdad,
                                        @RequestParam(value = "genero", required = false, defaultValue = "") String genero,
                                        @RequestParam(value = "busqueda", required = false, defaultValue = "") String busqueda,
                                        @RequestParam(value = "pag", required = false, defaultValue = "0") String pag) {

        int pagina;
        try {
            pagina = Integer.parseInt(pag);
        } catch (Exception e) {
            pagina = 0;
        }

        List<Funciongenero> listaFuncionGenero = funcionGeneroRepository.buscarFuncionGeneroPorFiltros(restriccionEdad, genero, busqueda);

        //System.out.println("Lista funcionesGenero que retorna el query");
        //for (Funciongenero f : listaFuncionGenero) {
        //    System.out.println(f.getIdfuncion().getNombre());
        //    System.out.println(f.getIdgenero().getNombre());
        //    System.out.println("=============================");
        //} //BIEN

        LinkedHashMap<Funcion, ArrayList<Genero>> funcionGenero = new LinkedHashMap<>();
        for (Funciongenero f : listaFuncionGenero) {
            System.out.println("Turno de: " + f.getIdfuncion().getNombre());
            System.out.println("Guardo: " + !funcionGenero.containsKey(f.getIdfuncion()));
            if (!funcionGenero.containsKey(f.getIdfuncion())) {
                System.out.println("He guardado: " + f.getIdfuncion().getNombre());
                funcionGenero.put(f.getIdfuncion(), new ArrayList<>());
            }
            if (!funcionGenero.get(f.getIdfuncion()).contains(f.getIdgenero())) {
                ArrayList<Genero> listaAGuardar = funcionGenero.get(f.getIdfuncion());
                listaAGuardar.add(f.getIdgenero());
                funcionGenero.put(f.getIdfuncion(), listaAGuardar);
            }
        }

        System.out.println("Tamanio hashmap funcionGenero primer vistazo: " + funcionGenero.size());
//        System.out.println("Lista Funcion Genero con obras unicas");
//        for (Map.Entry<Funcion, ArrayList<Genero>> h : funcionGenero.entrySet()) {
//
//            System.out.println(h.getKey().getNombre());
//            for (Genero g : h.getValue()) {
//                System.out.println(g.getNombre());
//            }
//            System.out.println("=====================");
//        }

        LinkedHashMap<String, Integer> listaNombres = new LinkedHashMap<>();
        for (Map.Entry<Funcion, ArrayList<Genero>> h : funcionGenero.entrySet()) {
            Funcion funcionAEvaluar = h.getKey();
            if (!listaNombres.containsKey(funcionAEvaluar.getNombre())) {
                listaNombres.put(funcionAEvaluar.getNombre(), 0);
            }
        }
//        System.out.println("Lista de nombres de funciones");
//        for (Map.Entry<String, Integer> h : listaNombres.entrySet()) {
//
//            System.out.println(h.getKey());
//            System.out.println(h.getValue());
//            System.out.println("=====================");
//        }

//        ArrayList<Funcion> listaFuncionesAEliminar = new ArrayList<>();
//        for (Map.Entry<Funcion, ArrayList<Genero>> h : funcionGenero.entrySet()) {
//            Funcion funcionAEvaluar = h.getKey();
//            for (Map.Entry<String, Integer> j : listaNombres.entrySet()) {
//                String nombre = j.getKey();
//                if (funcionAEvaluar.getNombre().equals(nombre)) {
//                    if (j.getValue() == 0) {
//                        listaNombres.put(nombre, 1);
//                    } else {
//                        listaFuncionesAEliminar.add(h.getKey());
//                    }
//                }
//            }
//        }
//        for (Funcion a : listaFuncionesAEliminar) {
//            funcionGenero.remove(a);
//        }

        System.out.println("Tamanio hashmap funcionGenero segundo vistazo tras eliminacion: " + funcionGenero.size());
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
        System.out.println("Tamanio de la lista: " + tamanhoLista);
        LinkedHashMap<Funcion, ArrayList<Genero>> listaFuncionesGeneroAEnviar = new LinkedHashMap<>();
        int i = 0;
        for (Map.Entry<Funcion, ArrayList<Genero>> h : funcionGenero.entrySet()) {
            if (i >= pagina * funcionesxpagina && i < pagina * funcionesxpagina + funcionesxpagina && i < tamanhoLista) {
                listaFuncionesGeneroAEnviar.put(h.getKey(), h.getValue());
            }
            i = i + 1;
        }

        ArrayList<Foto> listaCaratulas = new ArrayList<>();
        for (Map.Entry<Funcion, ArrayList<Genero>> h : listaFuncionesGeneroAEnviar.entrySet()) {
            listaCaratulas.add(fotoRepository.caratulaDeObra(h.getKey().getId()));
        }

        //System.out.println("Lista Funcion Genero actualizada");
        System.out.println("Pagina " + pagina);
        System.out.println("Tamanio lista: " + listaFuncionesGeneroAEnviar.size());
        //for (Map.Entry<Funcion, ArrayList<Genero>> h : listaFuncionesGeneroAEnviar.entrySet()) {

        //    System.out.println(h.getKey().getId());
        //    for (Genero g : h.getValue()) {
        //        System.out.println(g.getId());
        //    }
        //    System.out.println("=====================");
        //}

        int generoId;
        String generoIdStr = "";
        try {
            generoId = Integer.parseInt(genero);
            model.addAttribute("genero", generoId);
        } catch (Exception e) {
            model.addAttribute("genero", generoIdStr);
        }


        model.addAttribute("busqueda", busqueda);
        model.addAttribute("restriccionEdad", restriccionEdad);

        model.addAttribute("pagActual", pagina);
        model.addAttribute("pagTotal", (int) Math.ceil(listaFuncionGenero.size() / funcionesxpagina));
        model.addAttribute("listaFuncionesGenero", listaFuncionesGeneroAEnviar);
        model.addAttribute("listaCaratulas", listaCaratulas);
        model.addAttribute("generos", generoRepository.findAll());
        return "usuario/obras/carteleraObras";
    }

    @PostMapping("/BusquedaYFiltros")
    String busqueda(@RequestParam(value = "restriccionEdad", defaultValue = "") String restriccionEdad,
                    @RequestParam(value = "genero", defaultValue = "") String genero,
                    @RequestParam(value = "busqueda", defaultValue = "") String busqueda,
                    @RequestParam(value = "pag", defaultValue = "0") String pag,
                    RedirectAttributes attr) {

        System.out.println("Valores recibidos en POST:");
        System.out.println("restriccionedad: " + restriccionEdad);
        System.out.println("busqueda: " + busqueda);
        System.out.println("genero: " + genero);

        return "redirect:/cartelera?restriccionEdad=" + restriccionEdad + "&genero=" + genero + "&busqueda=" + busqueda + "&pag=" + pag;
    }

    @GetMapping("/DetallesObra")
    String detallesObra(@RequestParam("id") int id, Model model, RedirectAttributes a) {

        Optional<Funcion> opt = funcionRepository.findById(id);
        if (opt.isPresent()) {
            Funcion funcion = opt.get();
            List<Foto> listaFotos = fotoRepository.fotosFuncion(id);

            //Mandar los generos de la obra de la funcion
            ArrayList<Genero> listaGeneros = new ArrayList<>();
            List<Funciongenero> listaFuncionesGenero = funcionGeneroRepository.buscarFuncionGenero(id);
            for (Funciongenero f : listaFuncionesGenero) {
                listaGeneros.add(f.getIdgenero());
            }

            //Mandar director y actores
            LinkedHashMap<Persona, ArrayList<Persona>> listaDirectorYActores = new LinkedHashMap<>();
            List<Funcionelenco> listaFuncionelenco = funcionElencoRepository.buscarFuncionElenco(id);
            Persona director = new Persona();
            for (Funcionelenco f : listaFuncionelenco) {
                if (f.getIdpersona().getIdrol().getId() == 4) {
                    director = f.getIdpersona();
                }
            }
            ArrayList<Persona> listaActores = new ArrayList<>();
            for (Funcionelenco f : listaFuncionelenco) {
                if (f.getIdpersona().getIdrol().getId() != 4) {
                    listaActores.add(f.getIdpersona());
                }
            }
            listaDirectorYActores.put(director ,listaActores);

            model.addAttribute("funcion", funcion);
            model.addAttribute("listaFotos", listaFotos);
            model.addAttribute("listaGeneros", listaGeneros);
            model.addAttribute("listaDirectorYActores", listaDirectorYActores);
            return "usuario/obras/carteleraObraDetalles";
        } else {
            a.addFlashAttribute("msg", -1);
            return "redirect:/cartelera";
        }

    }

}
