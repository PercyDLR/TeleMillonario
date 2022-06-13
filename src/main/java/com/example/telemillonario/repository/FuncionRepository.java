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

    @Query(nativeQuery = true, value = "select f.* from funcion f " +
            "inner join sala s on (s.id = f.idsala) " +
            "where f.estado=1 and s.idsede=?1 " +
            "limit ?2,?3")
    List<Funcion> buscarFuncionesPorSede( int idsede, int pag, int salasporpag);

    @Query(nativeQuery = true, value = "select * from telemillonario.funcion where " +
            "estado=?1 " +
            "limit ?2,?3")
    List<Funcion> buscarFuncion( int estado, int pag, int salasporpag);


   /*Validacion si la funcion existe en esa sede a dicha hora*/
    @Query(nativeQuery = true,value = "SELECT funcion.id FROM funcion INNER JOIN sala ON ( funcion.idsala = sala.id) INNER JOIN sede ON ( sala.idsede = sede.id) WHERE funcion.id = ?1 AND sede.id = ?2 AND funcion.fecha = ?3 AND funcion.inicio = ?4")
    Funcion encontrarFuncionHoraSede(int idFuncion, int idSede, LocalDate fecha, LocalTime hora);

 }
