package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PersonaRepository extends JpaRepository<Persona, Integer> {
    //metodos generales
    @Query(value= "select persona.dni from persona where persona.estado=1",nativeQuery = true)
    List<String> obtenerDnis();
    //Filtros para Operadores
    @Query(value = "select * from persona where persona.idrol = 3 and persona.estado=1",nativeQuery = true)
    //@Query(value = "select * from persona where persona.idrol = 1 and persona.estado=1",nativeQuery = true)
    List<Persona> listarOperadores();
    @Query(value = "select * from persona where (persona.apellidos like '%?1%' or persona.nombres like '%?1%') and idrol = 3",nativeQuery = true)
    List<Persona> listarOperadoresPorNombre(String nombre);
    @Query(value = "select * from persona where persona.idrol=3 group by ?1",nativeQuery = true)
    List<Persona> listarOperadoresPorFiltro(String filtro);
    @Query(value = "select * from persona where (persona.apellidos like '%?1%' or persona.nombres like '%?1%') and idrol = 3 group by ?2",nativeQuery = true)
    List<Persona> listarOperadoresPorFiltroyNombre(String nombre,String filtro);
    //Filtro para Usuarios
    //Filtro para Administradores
    //Filtro para Actores
    //Filtro para Directores
}