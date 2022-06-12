package com.example.telemillonario.controller.Administrador;

import com.example.telemillonario.entity.Foto;
import com.example.telemillonario.entity.Obra;
import com.example.telemillonario.entity.Sede;
import com.example.telemillonario.repository.FotoRepository;
import com.example.telemillonario.repository.ObraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/admin/obras")
public class ObraOperController {

    @Autowired
    ObraRepository obraRepository;

    @Autowired
    FotoRepository fotoRepository;

    float obrasporpagina=6;

    @GetMapping(value = {"", "/","/lista"})
    public String listarObras(Model model,@RequestParam(value = "busqueda",defaultValue = "") String busqueda,
                              @RequestParam(value = "pag",defaultValue = "0") String pag, HttpSession session){

        int pagina;
        int estado=1;
        try{
            pagina = Integer.parseInt(pag);
        }catch(Exception e) {
            pagina=0;
        }


        if (busqueda.equals("")) { // verifica que no esté vacío

            List<Foto> listObraConFoto= fotoRepository.listadoObrasFotoAdmin(estado,(int)obrasporpagina*pagina, (int)obrasporpagina);
            int cantObras = obraRepository.cantObrasTotalAdmin();
            model.addAttribute("pagTotal",(int) Math.ceil(cantObras/obrasporpagina));
            model.addAttribute("listObras",listObraConFoto);
        }else{
            List<Foto> listObraConFoto= fotoRepository.buscarObrasFotoPorNombreAdmin(estado,busqueda , (int)obrasporpagina*pagina, (int)obrasporpagina);
            int cantObras = obraRepository.cantObrasTotalFiltrAdmin(estado,busqueda);
            model.addAttribute("pagTotal",(int) Math.ceil(cantObras/obrasporpagina));
            model.addAttribute("listObras",listObraConFoto);
        }

        model.addAttribute("pagActual",pagina);
        model.addAttribute("busqueda", busqueda);
        return "Administrador/Obra/listaObras";
    }

    @GetMapping(value = {"/crear"})
    public String crearSedeForm(@ModelAttribute("obra") Obra obra, Model model){


        return "Administrador/Obra/editarObra";
    }


}
