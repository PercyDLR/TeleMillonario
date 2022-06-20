package com.example.telemillonario.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public interface BalanceDto {
    String getSede();
    int getFuncionid();
    int getObraid();
    String getNombre();
    int getSalanumero();
    LocalDate getFecha();
    LocalTime getHorainicio();
    LocalTime getHorafin();
    Double getPreciounitario();
    int getStock();
    int getAsistencia();
    Double getCalificacion();
    int getRestriccion();
}
