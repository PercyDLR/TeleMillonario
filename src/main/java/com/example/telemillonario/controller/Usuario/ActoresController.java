package com.example.telemillonario.controller.Usuario;

import com.example.telemillonario.entity.Foto;
import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.repository.FotoRepository;
import com.example.telemillonario.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/actores")
public class ActoresController {

    @Autowired
    PersonaRepository personaRepository;

    @Autowired
    FotoRepository fotoRepository;

    //Variables Importantes
    int actoresxpagina = 12;

    @GetMapping(value = "")
    public String listaActores(Model model,
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

        int cantActores = personaRepository.cantActores(busqueda.toLowerCase());

        List<Persona> listaActores = personaRepository.buscarActoresFiltros(busqueda.toLowerCase(), fil, actoresxpagina*pagina, actoresxpagina);

        List<Foto> listaFotosActores = new ArrayList<>();
        for (Persona p : listaActores) {
            listaFotosActores.add(fotoRepository.fotoActor(p.getId()));
        }

        model.addAttribute("busqueda", busqueda);
        model.addAttribute("filtro", filtro);
        model.addAttribute("listaActores", listaActores);
        model.addAttribute("listaFotos", listaFotosActores);

        model.addAttribute("pagActual", pagina);
        model.addAttribute("pagTotal", (int) Math.ceil(cantActores / actoresxpagina));
        return "usuario/actores/listaActores";

    }

    @PostMapping("/BusquedaYFiltros")
    String busqueda(@RequestParam(value = "filtro", required = false, defaultValue = "") String filtro,
                    @RequestParam(value = "busqueda", required = false, defaultValue = "") String busqueda,
                    @RequestParam(value = "pag", required = false, defaultValue = "0") String pag) {

        return "redirect:/actores?busqueda=" + busqueda + "&filtro=" + filtro + "&pag=" + pag;
    }

}
