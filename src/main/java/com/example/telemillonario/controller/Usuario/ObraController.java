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

import java.util.List;

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

    //Variables Importantes
    int funcionesxpagina=12;

    @GetMapping(value = "")
    public String listarFuncionesGenero(Model model,
                                @RequestParam(value = "busqueda",defaultValue = "") String busqueda,
                                @RequestParam(value = "pag",defaultValue = "0") String pag){
        int pagina;
        try{
            pagina = Integer.parseInt(pag);
        }catch(Exception e) {
            pagina=0;
        }

        int cantidadFuncionesGenero = funcionGeneroRepository.buscarFuncionGeneroPorFiltros("","","").size();
        List<Funciongenero> listaFuncionGenero = funcionGeneroRepository.buscarFuncionGeneroPorFiltrosYLimite("", "", "", pagina*funcionesxpagina, funcionesxpagina);

        model.addAttribute("pagActual",pagina);
        model.addAttribute("pagTotal",(int) Math.ceil(cantidadFuncionesGenero/funcionesxpagina));
        model.addAttribute("listaFuncionesGenero", listaFuncionGenero);
        model.addAttribute("generos", generoRepository.findAll());
        return "usuario/obras/carteleraObras";
    }

    @PostMapping(value = "/BusquedaYFiltros")
    public String listarFuncionesGeneroFiltros(@ModelAttribute("restriccionEdad") Integer restriccionEdad,
                                               @ModelAttribute("genero") Genero genero,
                                               @ModelAttribute("busqueda") String busqueda,
                                               @RequestParam(value = "pag",defaultValue = "0") String pag,
                                               Model model) {

        int pagina;
        try{
            pagina = Integer.parseInt(pag);
        }catch(Exception e) {
            pagina=0;
        }

        int cantidadFuncionesGenero = funcionGeneroRepository.buscarFuncionGeneroPorFiltros(String.valueOf(restriccionEdad),String.valueOf(genero.getId()),busqueda).size();
        List<Funciongenero> listaFuncionGenero = funcionGeneroRepository.buscarFuncionGeneroPorFiltrosYLimite(String.valueOf(restriccionEdad),String.valueOf(genero.getId()),busqueda, pagina*funcionesxpagina, funcionesxpagina);

        model.addAttribute("pagActual",pagina);
        model.addAttribute("pagTotal",(int) Math.ceil(cantidadFuncionesGenero/funcionesxpagina));
        model.addAttribute("listaFuncionesGenero", listaFuncionGenero);
        model.addAttribute("generos", generoRepository.findAll());
        return "usuario/obras/carteleraObras";
    }

}
