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

import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
@RequestMapping("/sedes")
public class SedesController {

    @Autowired
    PersonaRepository personaRepository;

    @Autowired
    FotoRepository fotoRepository;

    @Autowired
    FuncionRepository funcionRepository;

    @Autowired
    GeneroRepository generoRepository;

    @Autowired
    SedeRepository sedeRepository;

    @Autowired
    ObraGeneroRepository obraGeneroRepository;

    @Autowired
    DistritoRepository distritoRepository;

    @Autowired
    CalificacionesRepository calificacionesRepository;
    int sedesxpagina=12;
    int obraxpagina = 12;

    @GetMapping("")
    public String listaSedesUsuario(Model model,
                                    @RequestParam(value = "busqueda",defaultValue = "") String busqueda,
                                    @RequestParam(value = "pag",defaultValue = "0") String pag,
                                    @RequestParam(value = "filtro", required = false, defaultValue = "0") String filtro, HttpSession session){
        //lista de sedes habilitadas
        int pagina;
        int estado=1;

        try{
            pagina = Integer.parseInt(pag);
        }catch(Exception e) {
            pagina=0;
        }
        int fil;
        try {
            fil = Integer.parseInt(filtro);
        } catch (Exception e) {
            fil = 0;
        }

        System.out.println(fil);

        int cantSedes;

        List<Sede> listaSedes = new ArrayList<>();
        if (fil == 0) {
            listaSedes = sedeRepository.listaSedesBusqueda(busqueda.toLowerCase(), sedesxpagina*pagina, sedesxpagina);
            cantSedes = sedeRepository.cantSede(busqueda.toLowerCase());
        } else {
            listaSedes = sedeRepository.listaSedesFiltro(busqueda.toLowerCase(), fil, sedesxpagina*pagina, sedesxpagina);
            cantSedes = sedeRepository.cantSedefiltro(busqueda.toLowerCase(), fil);
        }

        List<Foto> listaFotosSedes = new ArrayList<>();
        for (Sede s : listaSedes) {
            listaFotosSedes.add(fotoRepository.fotoSede(s.getId()));
        }

        model.addAttribute("busqueda", busqueda);
        model.addAttribute("filtro", fil);
        model.addAttribute("listaSedes", listaSedes);
        model.addAttribute("listaFotos", listaFotosSedes);
        model.addAttribute("listaDistritos", distritoRepository.findAll());

        model.addAttribute("pagActual", pagina);

        model.addAttribute("pagTotal", (int) Math.ceil((float) cantSedes / (float) sedesxpagina));
        return "usuario/sedes/listaSedes";
    }

    @PostMapping("/BusquedaYFiltros")
    String busqueda(@RequestParam(value = "filtro", required = false, defaultValue = "") String filtro,
                    @RequestParam(value = "busqueda", required = false, defaultValue = "") String busqueda,
                    @RequestParam(value = "pag", required = false, defaultValue = "0") String pag) {

        return "redirect:/sedes?busqueda=" + busqueda + "&filtro=" + filtro + "&pag=" + pag;
    }

    @GetMapping("/DetallesSede")
    String detallesObra(@RequestParam("id") int id, Model model, RedirectAttributes a,
                        @RequestParam(value = "restriccionEdad", required = false, defaultValue = "") String restriccionEdad,
                        @RequestParam(value = "genero", required = false, defaultValue = "") String genero,
                        @RequestParam(value = "busqueda", required = false, defaultValue = "") String busqueda,
                        @RequestParam(value = "pag", required = false, defaultValue = "0") String pag, HttpSession session) {

        int pagina;
        try {
            pagina = Integer.parseInt(pag);
        } catch (Exception e) {
            pagina = 0;
        }

        Optional<Sede> opt = sedeRepository.findById(id);
        if (opt.isPresent()) {

            model.addAttribute("sede", opt.get());

            //Sacado de ObraController
            List<Obragenero> listaFuncionGenero = obraGeneroRepository.buscarFuncionGeneroPorFiltrosSede(id, restriccionEdad, genero, busqueda);

            LinkedHashMap<Obra, ArrayList<Genero>> funcionGenero = new LinkedHashMap<>();
            for (Obragenero f : listaFuncionGenero) {
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
                if (i >= pagina * obraxpagina && i < pagina * obraxpagina + obraxpagina && i < tamanhoLista) {
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

            System.out.println("Paginas totales: " + (int) Math.ceil(tamanhoLista / (float) obraxpagina));

            model.addAttribute("pagTotal", (int) Math.ceil(tamanhoLista / (float) obraxpagina));
            model.addAttribute("listaCaratulas", listaCaratulas);
            model.addAttribute("generos", generoRepository.findAll());

            model.addAttribute("listaFotos", fotoRepository.fotosSede(id));
            //Envio de Reseñas de la obra con nombre de la persona + calificacion+foto del usuario

            LinkedHashMap<Calificaciones,String> reseniasconurlfotousuario= new LinkedHashMap<>();
            List<Calificaciones> ListResenias = calificacionesRepository.buscarReseñasSede(id);
            for (Calificaciones calf :ListResenias){
                List<Foto> fotosEnDB = fotoRepository.findByIdpersonaOrderByNumero(calf.getPersona().getId());
                if(fotosEnDB.size()>0){
                    reseniasconurlfotousuario.put(calf,fotosEnDB.get(0).getRuta());
                }else{
                    reseniasconurlfotousuario.put(calf,"/img/user.png");
                }
            }
            model.addAttribute("reseniasconfoto",reseniasconurlfotousuario);

            //Envio de la calificacion promedio de la sede
            model.addAttribute("califpromsede",calificacionesRepository.PromCalificacionSede(id));
            return "usuario/sedes/sedeDetalles";

        } else {
            a.addFlashAttribute("msg", -1);
            return "redirect:/sedes";
        }

    }

    @PostMapping("/BusquedaYFiltrosDetalles")
    String busqueda(@RequestParam("id") int id,
                    @RequestParam(value = "restriccionEdad", defaultValue = "") String restriccionEdad,
                    @RequestParam(value = "genero", defaultValue = "") String genero,
                    @RequestParam(value = "busqueda", defaultValue = "") String busqueda,
                    @RequestParam(value = "pag", defaultValue = "0") String pag) {

        return "redirect:/sedes/DetallesSede?id=" + id + "&restriccionEdad=" + restriccionEdad + "&genero=" + genero + "&busqueda=" + busqueda + "&pag=" + pag;
    }


}
