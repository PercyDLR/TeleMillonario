package com.example.telemillonario.controller.Usuario;

import com.example.telemillonario.entity.*;
import com.example.telemillonario.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/directores")
public class DirectoresController {

    @Autowired
    PersonaRepository personaRepository;

    @Autowired
    FotoRepository fotoRepository;

    @Autowired
    FuncionElencoRepository funcionElencoRepository;

    @Autowired
    ObraGeneroRepository obraGeneroRepository;

    @Autowired
    GeneroRepository generoRepository;

    //Variables Importantes
    int directoresxpagina = 12;
    int funcionesxpagina = 12;

    @GetMapping(value = "")
    public String listaDirectores(Model model,
                               @RequestParam(value = "filtro", required = false, defaultValue = "") String filtro,
                               @RequestParam(value = "busqueda", required = false, defaultValue = "") String busqueda,
                               @RequestParam(value = "pag", required = false, defaultValue = "0") String pag) {

        int pagina;
        try {
            pagina = Integer.parseInt(pag);
        } catch (Exception e) {
            pagina = 0;
        }
        int fil;
        try {
            fil = Integer.parseInt(filtro);
        } catch (Exception e) {
            fil = 0;
        }

        List<Persona> cantDirectores = personaRepository.cantidadDirectoresFiltro(busqueda.toLowerCase(), fil);
        List<Persona> listaDirectores = personaRepository.buscarDirectoresFiltros(busqueda.toLowerCase(), fil, directoresxpagina*pagina, directoresxpagina);
        List<Foto> listaFotosDirectores = new ArrayList<>();
        for (Persona p : listaDirectores) {
            listaFotosDirectores.add(fotoRepository.fotoDirector(p.getId()));
        }
        System.out.println("Tamanio lista fotos: " + listaFotosDirectores.size());

        model.addAttribute("busqueda", busqueda);
        model.addAttribute("filtro", filtro);
        model.addAttribute("listaDirectores", listaDirectores);
        model.addAttribute("listaFotos", listaFotosDirectores);

        model.addAttribute("pagActual", pagina);
        model.addAttribute("pagTotal", (int) Math.ceil(cantDirectores.size() / directoresxpagina));
        return "usuario/directores/listaDirectores";

    }

    @PostMapping("/BusquedaYFiltros")
    String busqueda(@RequestParam(value = "filtro", required = false, defaultValue = "") String filtro,
                    @RequestParam(value = "busqueda", required = false, defaultValue = "") String busqueda,
                    @RequestParam(value = "pag", required = false, defaultValue = "0") String pag) {

        return "redirect:/directores?busqueda=" + busqueda + "&filtro=" + filtro + "&pag=" + pag;
    }

    @GetMapping("/DetallesDirector")
    String detallesObra(@RequestParam("id") int id, Model model, RedirectAttributes a,
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

        Optional<Persona> opt = personaRepository.findById(id);
        if (opt.isPresent()) {

            model.addAttribute("director", opt.get());
            List<Funcionelenco> listaFuncionelenco = funcionElencoRepository.buscarFuncionElencoPorActorDirector(id);

            //Sacado de ObraController
            List<Obragenero> listaFuncionGenero = obraGeneroRepository.buscarFuncionGeneroPorFiltros(restriccionEdad, genero, busqueda);

            //Mandar obras en las que participe el director
            List<Obragenero> listaFuncionGeneroPersona = new ArrayList<>();
            for (Funcionelenco l : listaFuncionelenco) {
                for (Obragenero o : listaFuncionGenero) {
                    if (l.getIdfuncion().getIdobra().getId() == o.getIdobra().getId()) {
                        listaFuncionGeneroPersona.add(o);
                    }
                }
            }
            //Mandar obras en las que participe el director

            LinkedHashMap<Obra, ArrayList<Genero>> funcionGenero = new LinkedHashMap<>();
            for (Obragenero f : listaFuncionGeneroPersona) {
                //System.out.println("Turno de: " + f.getIdfuncion().getNombre());
                //System.out.println("Guardo: " + !funcionGenero.containsKey(f.getIdfuncion()));
                if (!funcionGenero.containsKey(f.getIdobra())) {
                    //System.out.println("He guardado: " + f.getIdobra().getNombre());
                    funcionGenero.put(f.getIdobra(), new ArrayList<>());
                }
                if (!funcionGenero.get(f.getIdobra()).contains(f.getIdgenero())) {
                    ArrayList<Genero> listaAGuardar = funcionGenero.get(f.getIdobra());
                    listaAGuardar.add(f.getIdgenero());
                    funcionGenero.put(f.getIdobra(), listaAGuardar);
                }
            }
            LinkedHashMap<String, Integer> listaNombres = new LinkedHashMap<>();
            for (Map.Entry<Obra, ArrayList<Genero>> h : funcionGenero.entrySet()) {
                Obra funcionAEvaluar = h.getKey();
                if (!listaNombres.containsKey(funcionAEvaluar.getNombre())) {
                    listaNombres.put(funcionAEvaluar.getNombre(), 0);
                }
            }

            float tamanhoLista = funcionGenero.size();
            System.out.println("Tamanio de la lista: " + tamanhoLista);
            LinkedHashMap<Obra, ArrayList<Genero>> listaObraGeneroAEnviar = new LinkedHashMap<>();
            int i = 0;
            for (Map.Entry<Obra, ArrayList<Genero>> h : funcionGenero.entrySet()) {
                if (i >= pagina * directoresxpagina && i < pagina * directoresxpagina + directoresxpagina && i < tamanhoLista) {
                    listaObraGeneroAEnviar.put(h.getKey(), h.getValue());
                }
                i = i + 1;
            }

            ArrayList<Foto> listaCaratulas = new ArrayList<>();
            for (Map.Entry<Obra, ArrayList<Genero>> h : listaObraGeneroAEnviar.entrySet()) {
                listaCaratulas.add(fotoRepository.caratulaDeObra(h.getKey().getId()));
            }
            //Sacado de ObraController


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

            model.addAttribute("listaObraGenero", listaObraGeneroAEnviar);
            model.addAttribute("pagActual", pagina);
            model.addAttribute("id", id);

            System.out.println("Paginas totales: " + (int) Math.ceil(tamanhoLista / (float) funcionesxpagina));

            model.addAttribute("pagTotal", (int) Math.ceil(tamanhoLista / (float) funcionesxpagina));
            model.addAttribute("listaCaratulas", listaCaratulas);
            model.addAttribute("generos", generoRepository.findAll());

            model.addAttribute("listaFotos", fotoRepository.fotosDirector(id));
            return "usuario/directores/detallesDirector";

        } else {
            a.addFlashAttribute("msg", -1);
            return "redirect:/directores";
        }

    }

    @PostMapping("/BusquedaYFiltrosDetalles")
    String busqueda(@RequestParam("id") int id,
                    @RequestParam(value = "restriccionEdad", defaultValue = "") String restriccionEdad,
                    @RequestParam(value = "genero", defaultValue = "") String genero,
                    @RequestParam(value = "busqueda", defaultValue = "") String busqueda,
                    @RequestParam(value = "pag", defaultValue = "0") String pag) {

        return "redirect:/directores/DetallesDirector?id=" + id + "&restriccionEdad=" + restriccionEdad + "&genero=" + genero + "&busqueda=" + busqueda + "&pag=" + pag;
    }

}
