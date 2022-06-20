package com.example.telemillonario.controller.Operador;

import com.example.telemillonario.dto.BalanceDto;
import com.example.telemillonario.dto.EstadisticaFuncionDto;
import com.example.telemillonario.entity.Persona;
import com.example.telemillonario.repository.FuncionRepository;
import com.example.telemillonario.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/operador/reportes")
public class ReportesController {

    @Autowired
    FuncionRepository funcionRepository;

    @Autowired
    ReporteService reporteService;

    @GetMapping(value = "/reportes")
    public String obtenerEstadisticas(@RequestParam("periodicidad") Optional<String> opt_periodicidad,
                                      @RequestParam("periodo") Optional<String> opt_periodo,
                                      HttpSession session, Model model, RedirectAttributes attr) {

        //Se obtiene la sede desde donde se realizarán todas las consultas para el reporte
        Persona persona = (Persona) session.getAttribute("usuario");
        int sede = persona.getIdsede().getId();

        //Si no se han enviado la periodicidad y el periodo
        if (!opt_periodo.isPresent() && !opt_periodicidad.isPresent()) {
            model.addAttribute("funcionMasVista", funcionRepository.obtenerFuncionMasVistaxSede(sede));
            model.addAttribute("funcionMenosVista", funcionRepository.obtenerFuncionMenosVistaxSede(sede));
            model.addAttribute("funcionMejorCalificada", funcionRepository.obtenerFuncionMejorCalificadaxSede(sede));
            model.addAttribute("funcionesPorcentajeAsistencia", funcionRepository.obtenerFuncionesxAsistenciaxSede(sede));
            model.addAttribute("actoresMejorCalificados", funcionRepository.obtenerActoresMejorCalificadosxSede(sede));
            model.addAttribute("directoresMejorCalificados", funcionRepository.obtenerDirectoresMejorCalificadosxSede(sede));

            return "/Operador/reportes";
        }

        Optional<EstadisticaFuncionDto> funcionMasVista = null;
        Optional<EstadisticaFuncionDto> funcionMenosVista = null;
        Optional<EstadisticaFuncionDto> funcionMejorCalificada = null;
        Optional<List<EstadisticaFuncionDto>> funcionesVistas = null;

        int periodo = 0;

        try{
            periodo = Integer.parseInt(opt_periodo.get());

        } catch (NumberFormatException e){
            attr.addFlashAttribute("msg", "Se envió un periodo inválido");
            return "redirect:/operador/funciones/reportes";
        }

        switch(opt_periodicidad.get()){
            case "Mensual":
                // Halla información del mes
                funcionMasVista = funcionRepository.obtenerFuncionMasVistaxMesxSede(sede, periodo);
                funcionMenosVista = funcionRepository.obtenerFuncionMenosVistaxMesxSede(sede, periodo);
                funcionMejorCalificada = funcionRepository.obtenerFuncionMejorCalificadaxMesxSede(sede, periodo);
                funcionesVistas = funcionRepository.obtenerFuncionesMejorCalificadasxMesxSede(sede, periodo);
                break;

            case "Anual":
                // Halla información del año
                funcionMasVista = funcionRepository.obtenerFuncionMasVistaxAnioxSede(sede, periodo);
                funcionMenosVista = funcionRepository.obtenerFuncionMenosVistaxAñoxSede(sede, periodo);
                funcionMejorCalificada = funcionRepository.obtenerFuncionMejorCalificadaxAnioxSede(sede, periodo);
                funcionesVistas = funcionRepository.obtenerFuncionesMejorCalificadasxAnioxSede(sede, periodo);
                break;

            default:
                //Se envio una periodicidad inválida
                attr.addFlashAttribute("msg", "Se envió una periodicad inválida");
                return "redirect:/operador/funciones/reportes";

        }

        // Si no hay información de ese periodo, manda error
        if (funcionMasVista.isEmpty() || funcionMenosVista.isEmpty() ||
                funcionMejorCalificada.isEmpty() || funcionesVistas.isEmpty()) {
            attr.addFlashAttribute("msg", "No se encontraron estadisticas de las funciones para el mes solicitado");
            return "redirect:/operador/funciones/reportes";
        }

        model.addAttribute("funcionMasVista", funcionMasVista.get());
        model.addAttribute("funcionMenosVista", funcionMenosVista.get());
        model.addAttribute("funcionMejorCalificada", funcionMejorCalificada.get());
        model.addAttribute("funcionesPorcentajeAsistencia", funcionesVistas.get());
        model.addAttribute("actoresMejorCalificados", funcionRepository.obtenerActoresMejorCalificadosxSede(sede));
        model.addAttribute("directoresMejorCalificados", funcionRepository.obtenerDirectoresMejorCalificadosxSede(sede));

        return "/Operador/reportes";
    }

    @GetMapping(value = "/exportar",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InputStreamResource> generarReporte(@RequestParam("periodicidad") Optional<String> opt_periodicidad,
                                                              @RequestParam("periodo") Optional<String> opt_periodo,
                                                              RedirectAttributes attr, HttpSession session) throws IOException {

        //Se obtiene la sede desde donde se realizarán todas las consultas para el reporte
        Persona persona = (Persona) session.getAttribute("usuario");
        int sede = persona.getIdsede().getId();

        // Variables Importantes
        Optional<List<BalanceDto>> balancexSede = Optional.empty();
        List<BalanceDto> balanceAux = new ArrayList<>();
        InputStreamResource resource = null;
        int periodo = 0;

        // Se genera el archivo
        String name = "reporte_"+ LocalDate.now() + ".xlsx";
        File file = null;

        //Si no se han enviado la periodicidad ni el periodo, se manda por defecto el balance general
        if (!opt_periodo.isPresent() && !opt_periodicidad.isPresent()) {

            List<BalanceDto> balancexSedeAux = funcionRepository.obtenerBalancexSede(sede);
            file = reporteService.generarReporte(opt_periodicidad,opt_periodo,balancexSedeAux,name);

            resource = new InputStreamResource(new FileInputStream(file));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + name + "\"")
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        }

        // Se comprueba que el periodo sea válido, caso contrario se envía una lista vacía
        try{
            periodo = Integer.parseInt(opt_periodo.get());

        } catch (NumberFormatException e){
            System.out.println(e.getMessage());
        }

        // Se halla el balance para el periodo dado
        switch(opt_periodicidad.get()){

            case "Mensual":
                balancexSede = funcionRepository.obtenerBalancexSedexMes(sede,periodo);
                break;

            case "Anual":
                balancexSede = funcionRepository.obtenerBalancexSedexAnio(sede,periodo);
                break;
        }

        // Se valida que se haya obtenido información
        if(balancexSede.isEmpty()){
            List<BalanceDto> balancexSedeAux = new ArrayList<>();
            file = reporteService.generarReporte(opt_periodicidad,opt_periodo,balancexSedeAux,name);
        } else {
            file = reporteService.generarReporte(opt_periodicidad,opt_periodo,balancexSede.get(),name);
        }

        // Se envía el resultado
        resource = new InputStreamResource(new FileInputStream(file));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + name + "\"")
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}
