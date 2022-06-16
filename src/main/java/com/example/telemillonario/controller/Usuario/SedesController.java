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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    int sedesxpagina=9;
    @GetMapping("")
    public String listaSedesUsuario(Model model,
                                    @RequestParam(value = "busqueda",defaultValue = "") String busqueda,
                                    @RequestParam(value = "pag",defaultValue = "0") String pag,
                                    @RequestParam(value = "filtro", required = false, defaultValue = "0") String filtro){
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

        int cantSedes = sedeRepository.cantSede(busqueda);
        List<Sede> listaSedes = new ArrayList<>();
        if (fil == 0) {
            listaSedes = sedeRepository.listaSedesBusqueda(busqueda.toLowerCase(), sedesxpagina*pagina, sedesxpagina);
        } else {
            listaSedes = sedeRepository.listaSedesFiltro(busqueda.toLowerCase(), fil, sedesxpagina*pagina, sedesxpagina);
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
        model.addAttribute("pagTotal", (int) Math.ceil(cantSedes / sedesxpagina));
        return "usuario/sedes/listaSedes";

//        if (busqueda.equals("")) { // verifica que no esté vacío
//
//            List<Foto> listSedesConFoto= fotoRepository.listadoSedesUsuar(estado,(int)sedesxpagina*pagina, (int)sedesxpagina);
//            int cantSedes = sedeRepository.cantSedesTotalUsuar();
//            model.addAttribute("pagTotal",(int) Math.ceil(cantSedes/sedesxpagina));
//            model.addAttribute("listSedes",listSedesConFoto);
//        }else{
//            List<Foto> listSedesConFoto= fotoRepository.buscarSedePorNombre(busqueda, estado, (int)sedesxpagina*pagina, (int)sedesxpagina);
//            int cantSedes = sedeRepository.cantSedesFiltr(busqueda,estado);
//            model.addAttribute("pagTotal",(int) Math.ceil(cantSedes/sedesxpagina));
//            model.addAttribute("listSedes",listSedesConFoto);
//        }
//
//        model.addAttribute("pagActual",pagina);
//
//
//        model.addAttribute("busqueda", busqueda);
    }

//    @PostMapping("/buscar")
//    String busquedaSedesUsuario(@RequestParam(value="busqueda", defaultValue = "") String busqueda){
//
//        return "redirect:/sedes?busqueda="+busqueda;
//    }

    @PostMapping("/BusquedaYFiltros")
    String busqueda(@RequestParam(value = "filtro", required = false, defaultValue = "") String filtro,
                    @RequestParam(value = "busqueda", required = false, defaultValue = "") String busqueda,
                    @RequestParam(value = "pag", required = false, defaultValue = "0") String pag) {

        return "redirect:/sedes?busqueda=" + busqueda + "&filtro=" + filtro + "&pag=" + pag;
    }




    float funcionesporpagina=5;
    @GetMapping("/sede")
    public String infoSedeDetalles(Model model,@RequestParam(value = "idsede") String idsedeStr,
                                   @RequestParam(value = "busqueda",defaultValue = "") String busqueda,
                                   @RequestParam(value="publico",required = false,defaultValue = "") String publico,
                                   @RequestParam(value = "pag",defaultValue = "0") String pag,RedirectAttributes attr){

        int pagina;
        try{
            pagina = Integer.parseInt(pag);
        }catch(Exception e) {
            pagina=0;
        }
//        int gen = !genero.isBlank() ? Integer.parseInt(genero) : 0;
        int restriccion = publico.equals("todos") ? 0 : publico.equals("mayores") ? 1 : 2;
        String busq=busqueda.toLowerCase();

        // Se verifica que el ID sea un número
        int idsede = 0;
        try{
            idsede = Integer.parseInt(idsedeStr);
        } catch (NumberFormatException e){
            attr.addFlashAttribute("err", "El ID ingresado no es inválido");
            return "redirect:/sedes";
        }
        List<Foto> fotosSede= fotoRepository.buscarFotosSede(idsede);
        model.addAttribute("imagenes",fotosSede);




//        LinkedHashMap<Foto, List<Obragenero>> fotoObraGenero = new LinkedHashMap<>();
        HashMap<Funcion,Foto> funcionesSedeConFoto = new HashMap<>();
        if(restriccion==2){
            List<Funcion> listFunciones = funcionRepository.buscarFuncionesPorSedeUsuar(idsede,busq,(int)funcionesporpagina*pagina, (int)funcionesporpagina);
            List<Foto> listFotosObra = fotoRepository.buscarFotoObrasPorSede(idsede);

            for (Funcion funcion : listFunciones){
                for (Foto foto : listFotosObra){
                    if (foto.getIdobra() == funcion.getIdobra()){
                        funcionesSedeConFoto.put(funcion,foto);
                        break;
                    }
                }
            }

            int cantfunc= funcionRepository.FuncTotalSedeUsuar(idsede,busqueda).size();
            model.addAttribute("pagTotal",(int) Math.ceil(cantfunc/funcionesporpagina));
        }else{
            List<Funcion> listFunciones = funcionRepository.buscarFuncionesPorSedeUsuarFiltr(idsede,busq,restriccion,(int)funcionesporpagina*pagina, (int)funcionesporpagina);
            List<Foto> listFotosObra = fotoRepository.buscarFotoObrasPorSede(idsede);

            for (Funcion funcion : listFunciones){
                for (Foto foto : listFotosObra){
                    if (foto.getIdobra() == funcion.getIdobra()){
                        funcionesSedeConFoto.put(funcion,foto);
                        break;
                    }
                }
            }

            int cantfunc= funcionRepository.FuncTotalSedeUsuarFiltr(idsede,busqueda,restriccion).size();
            model.addAttribute("pagTotal",(int) Math.ceil(cantfunc/funcionesporpagina));
        }




        model.addAttribute("funcionessede",funcionesSedeConFoto);










        model.addAttribute("busqueda", busqueda);
        model.addAttribute("pagActual",pagina);

//        model.addAttribute("funcionessede",fotoObraGenero);
        model.addAttribute("sede",sedeRepository.findById(idsede).get());
        model.addAttribute("idsede",idsede);
//        model.addAttribute("listGeneros",generoRepository.findAll());
//        model.addAttribute("genero", genero);
        model.addAttribute("publico", publico);
        return "usuario/sedes/sedeDetalles";
    }



    @PostMapping("/sede/filtrar")
    String busqueda(@RequestParam(value = "busqueda",defaultValue = "") String busqueda,
                    @RequestParam(value="genero",required = false,defaultValue = "") String genero,
                    @RequestParam(value="publico",required = false,defaultValue = "") String publico,
                    @RequestParam(value = "pag",defaultValue = "0") String pag,
                    @RequestParam(value = "idsede",required = false) String idsede,
                    RedirectAttributes attr){

        return "redirect:/sedes/sede?busqueda="+busqueda+"&genero="+genero+"&publico="+publico+"&idsede="+idsede+"&pag="+pag;
    }


}
