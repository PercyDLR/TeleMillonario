package com.example.telemillonario.controller.Administrador;

import com.example.telemillonario.entity.Compra;
import com.example.telemillonario.entity.Foto;
import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.repository.CompraRepository;
import com.example.telemillonario.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.time.Duration;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;

@Controller
@RequestMapping("/admin/clientes")
public class ClienteController {

    @Autowired
    PersonaRepository personaRepository;

    @Autowired
    CompraRepository compraRepository;

    float comprasporpagina=6;
    @GetMapping("/lista")
    public String listadoClientes(Model model, @RequestParam(value = "pag",required = false) String pag,
                                  @RequestParam(value="parametro",required = false,defaultValue = "") String parametro,
                                  @RequestParam(value="buscador",required = false,defaultValue = "") String buscador) {

        int pagina;

        try{
            pagina = Integer.parseInt(pag);
        }catch(Exception e) {
            pagina=0;
        }



        if (parametro.equals("")) { // verifica que no esté vacío

            List<Compra> listcomprasgeneral= compraRepository.historialComprasGeneral((int)comprasporpagina*pagina, (int)comprasporpagina);
            int cantcompras = compraRepository.cantComprasTotal();
            model.addAttribute("historialComprasClientes",listcomprasgeneral);
            model.addAttribute("pagTotal",(int) Math.ceil(cantcompras/comprasporpagina));
            duracionFuncion(model, listcomprasgeneral);
        }else{
            parametro = parametro.toLowerCase();
            List<Compra> listCompras = switch (buscador) {
                case "Dni" -> compraRepository.buscarCompraPorDni(parametro, (int)comprasporpagina*pagina, (int)comprasporpagina);
                case "Nombre" -> compraRepository.buscarCompraPorNombre(parametro, (int)comprasporpagina*pagina, (int)comprasporpagina);
                case "Apellido" -> compraRepository.buscarCompraPorApellido(parametro, (int)comprasporpagina*pagina, (int)comprasporpagina);
                case "Numero" -> compraRepository.buscarCompraPorNumero(parametro,(int)comprasporpagina*pagina, (int)comprasporpagina);
                case "Sede" -> compraRepository.buscarCompraPorSede(parametro, (int)comprasporpagina*pagina, (int)comprasporpagina);

                default -> compraRepository.historialComprasGeneral((int)comprasporpagina*pagina, (int)comprasporpagina);
            };

            Integer cantcompras=switch (buscador){
                case "Dni" -> compraRepository.cantComprasPorDni(parametro);
                case "Nombre" -> compraRepository.cantComprasPorNombre(parametro);
                case "Apellido" -> compraRepository.cantComprasPorApellido(parametro);
                case "Numero" -> compraRepository.cantComprasPorNumero(parametro);
                case "Sede" -> compraRepository.cantComprasPorSede(parametro);
                default -> compraRepository.cantComprasTotal();
            };

            model.addAttribute("pagTotal",(int) Math.ceil(cantcompras/comprasporpagina));
            duracionFuncion(model, listCompras);
            model.addAttribute("historialComprasClientes",listCompras);
        }

        model.addAttribute("parametro", parametro);
        model.addAttribute("buscador", buscador);

        model.addAttribute("pagActual",pagina);
        return "Administrador/Cliente/listaClientes";
    }

    private void duracionFuncion(Model model, List<Compra> listCompras) {
        LinkedHashMap<Compra, String> duracionFuncioncompraGeneral = new LinkedHashMap<>();
        for (Compra c : listCompras) {
            LocalTime inicio = c.getFuncion().getInicio();
            LocalTime fin = c.getFuncion().getFin();
            Duration duracion = Duration.between(inicio, fin);
            Long duracionHora = duracion.getSeconds()/(60*60);
            String duracionHoraStr = duracionHora.toString();
            if (duracionHora < 10) {
                duracionHoraStr = "0" + duracionHora.toString();
            }
            Long duracionMinutos = duracion.getSeconds()%60;
            String duracionMinutosStr = duracionMinutos.toString();
            if (duracionMinutos < 10) {
                duracionMinutosStr = "0" + duracionMinutos.toString();
            }
            duracionFuncioncompraGeneral.put(c, duracionHoraStr + ":" + duracionMinutosStr + "h");
        }
        model.addAttribute("duracionFuncioncompraGeneral", duracionFuncioncompraGeneral);
    }


    @PostMapping(value = {"/buscar"})
    public String buscarCompra(@RequestParam("parametro") String parametro, @RequestParam("buscador") String buscador,
                             @RequestParam(value = "pag",required = false) String pag, RedirectAttributes attr){

        return "redirect:/admin/clientes/lista?parametro="+parametro+"&buscador="+buscador+"&pag="+pag;
    }

//    @GetMapping(value = "/clientes/pruebalista",produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
//    public List<Compra> pruebaListado() {
//
//        return compraRepository.historialComprasGeneral();
//    }

}
