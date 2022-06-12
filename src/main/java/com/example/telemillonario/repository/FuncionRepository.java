package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Funcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface FuncionRepository extends JpaRepository<Funcion, Integer> {


    Funcion findTopByOrderByIdDesc();


    @Query(nativeQuery = true, value = "select * from telemillonario.funcion where " +
            "estado=?1 " +
            "limit ?2,?3")
    List<Funcion> buscarFuncion( int estado, int pag, int salasporpag);


   @Query(nativeQuery = true,value = "SELECT * FROM funcion WHERE estado = 1 ORDER BY calificacion DESC LIMIT 4")
   List<Funcion> obtenerFuncionesDestacadasPaginaPrincipal();



   /*Validacion si la funcion existe en esa sede a dicha hora*/
    @Query(nativeQuery = true,value = "SELECT funcion.id,funcion.estado,funcion.fecha,funcion.inicio,funcion.fin,funcion.precioentrada,funcion.stockentradas,funcion.cantidadasistentes,funcion.idsala,funcion.idobra FROM funcion INNER JOIN sala ON ( funcion.idsala = sala.id) INNER JOIN sede ON ( sala.idsede = sede.id) INNER JOIN obra ON ( funcion.idobra = obra.id) WHERE obra.id = ?1 AND sede.id = ?2 AND funcion.fecha = ?3 AND funcion.inicio = ?4")
    Funcion encontrarFuncionHoraSede(int idObra, int idSede, LocalDate fecha, LocalTime hora);

    List<Funcion> findByNombre(String nombreObra);


 }
