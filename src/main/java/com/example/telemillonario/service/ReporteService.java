package com.example.telemillonario.service;

import com.example.telemillonario.dto.BalanceDto;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.Optional;

@Service
@Component
public class ReporteService {

    @Autowired
    FileService fileService;

    public File generarReporte(Optional<String> periodicidad, Optional<String> periodo, List<BalanceDto> data,String nombre) throws IOException {
        //Creo el cuaderno
        XSSFWorkbook workbook = new XSSFWorkbook();
        //Creo la hoja
        XSSFSheet hoja = workbook.createSheet("Reporte"+(periodicidad.isPresent()?periodicidad.get():"")+" por sede");
        //objeto que formatea las celdas
        CreationHelper createHelper = workbook.getCreationHelper();
        //formato para la fecha
        CellStyle formatoFecha = workbook.createCellStyle();
        //formato para la hora
        CellStyle formatoHora = workbook.createCellStyle();
        //creacion de los formatos
        formatoFecha.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));
        formatoHora.setDataFormat(createHelper.createDataFormat().getFormat("hh:mm:ss"));
        if(data.size()==0){
            //No llego informacio
            hoja.createRow(0);
            hoja.getRow(0).createCell(0).setCellValue("No se ha encontrado información de funciones y obras con los filtros aplicados");
            //guardo la informacion
        }else{
            //si en caso ha llegado la informacion
            int index=0;
            //creo filas y asigno las celdas
            hoja.createRow(index);
            index++;
            hoja.createRow(index);
            hoja.getRow(index).createCell(0).setCellValue("Reporte de ganancias de la sede: "+data.get(0).getSede());
            index++;
            hoja.createRow(index);
            index++;
            if(periodicidad.isPresent()&&periodo.isPresent()){
                hoja.createRow(index);
                hoja.getRow(index).createCell(0).setCellValue("Filtros usados: ");
                index++;
                hoja.createRow(index);
                hoja.getRow(index).createCell(0).setCellValue("Tipo: ");
                hoja.getRow(index).createCell(1).setCellValue(periodicidad.get());
                index++;
                hoja.createRow(index);
                hoja.getRow(index).createCell(0).setCellValue("Periodo: ");
                hoja.getRow(index).createCell(1).setCellValue(periodo.get());
                index++;
            }
            hoja.createRow(index);
            index++;
            //creacion de las cabeceras
            hoja.createRow(index);
            hoja.getRow(index).createCell(0).setCellValue("N°");
            hoja.getRow(index).createCell(1).setCellValue("Sede");
            hoja.getRow(index).createCell(2).setCellValue("Nombre de la obra");
            hoja.getRow(index).createCell(3).setCellValue("Numero de la sala");
            hoja.getRow(index).createCell(4).setCellValue("Fecha");
            hoja.getRow(index).createCell(5).setCellValue("Hora de inicio");
            hoja.getRow(index).createCell(6).setCellValue("Hora de fin");
            hoja.getRow(index).createCell(7).setCellValue("Precio por entrada");
            hoja.getRow(index).createCell(8).setCellValue("Stock");
            hoja.getRow(index).createCell(9).setCellValue("Entradas vendidas");
            hoja.getRow(index).createCell(10).setCellValue("Calificacion");
            hoja.getRow(index).createCell(11).setCellValue("Restriccion");
            hoja.getRow(index).createCell(12).setCellValue("Porcentaje de asistencia");
            hoja.getRow(index).createCell(13).setCellValue("Total de ventas por funcion");
            index++;
            //iteracion en la data
            int j=1;
            for(BalanceDto balanceDto : data){
                hoja.createRow(index);
                //Asignacion de estilos de acuerdo a las celda
                hoja.getRow(index).createCell(4).setCellStyle(formatoFecha);
                hoja.getRow(index).createCell(5).setCellStyle(formatoHora);
                hoja.getRow(index).createCell(6).setCellStyle(formatoHora);
                //seteo de valores
                hoja.getRow(index).createCell(0).setCellValue(j);
                hoja.getRow(index).createCell(1).setCellValue(balanceDto.getSede());
                hoja.getRow(index).createCell(2).setCellValue(balanceDto.getNombre());
                hoja.getRow(index).createCell(3).setCellValue(balanceDto.getSalanumero());
                hoja.getRow(index).createCell(4).setCellValue(balanceDto.getFecha().toString());//formatear
                hoja.getRow(index).createCell(5).setCellValue(balanceDto.getHorainicio().toString());//formatear
                hoja.getRow(index).createCell(6).setCellValue(balanceDto.getHorafin().toString());//formatear
                hoja.getRow(index).createCell(7).setCellValue(balanceDto.getPreciounitario());
                hoja.getRow(index).createCell(8).setCellValue(balanceDto.getStock());
                hoja.getRow(index).createCell(9).setCellValue(balanceDto.getAsistencia());
                hoja.getRow(index).createCell(10).setCellValue(balanceDto.getCalificacion());
                hoja.getRow(index).createCell(11).setCellValue(balanceDto.getRestriccion()==1?"Si":"No");
                hoja.getRow(index).createCell(12).setCellValue((((float) balanceDto.getAsistencia())/balanceDto.getStock())*100);
                hoja.getRow(index).createCell(13).setCellValue(balanceDto.getAsistencia()*balanceDto.getPreciounitario());
                index++;
                j++;
            }
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        workbook.close();
        byte[] bytes = byteArrayOutputStream.toByteArray();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        MultipartFile multipartFile = new MockMultipartFile(nombre,nombre,"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",inputStream);
        File convFile = new File(multipartFile.getOriginalFilename());
        multipartFile.transferTo(convFile);
        return convFile;
    }
}
