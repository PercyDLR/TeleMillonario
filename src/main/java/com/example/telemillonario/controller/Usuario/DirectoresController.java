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

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/directores")
public class DirectoresController {

    @Autowired
    PersonaRepository personaRepository;

    @Autowired
    FotoRepository fotoRepository;

    //Variables Importantes
    int directoresxpagina = 12;

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

        int cantDirectores = personaRepository.cantDirectores(busqueda.toLowerCase());
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
        model.addAttribute("pagTotal", (int) Math.ceil(cantDirectores / directoresxpagina));
        return "usuario/directores/listaDirectores";

    }

    @PostMapping("/BusquedaYFiltros")
    String busqueda(@RequestParam(value = "filtro", required = false, defaultValue = "") String filtro,
                    @RequestParam(value = "busqueda", required = false, defaultValue = "") String busqueda,
                    @RequestParam(value = "pag", required = false, defaultValue = "0") String pag) {

        return "redirect:/directores?busqueda=" + busqueda + "&filtro=" + filtro + "&pag=" + pag;
    }

}
