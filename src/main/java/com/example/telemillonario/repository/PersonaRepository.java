package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Integer> {
    //metodos generales
    @Query(value= "select persona.dni from persona where persona.estado=1",nativeQuery = true)
    List<String> obtenerDnis();
    //Filtros para Operadores
    @Query(value = "select * from persona where persona.idrol = 3 and persona.estado=1",nativeQuery = true)
    //@Query(value = "select * from persona where persona.idrol = 1 and persona.estado=1",nativeQuery = true)
    List<Persona> listarOperadores();
    @Query(value = "select * from persona where (persona.apellidos like %?1% or persona.nombres like %?1%) and idrol = 3 and persona.estado=1",nativeQuery = true)
    List<Persona> listarOperadoresPorNombre(String nombre);
    @Query(value = "select * from persona where persona.idrol=3 and persona.estado=1 and idsede =?1",nativeQuery = true)
    List<Persona> listarOperadoresPorFiltro(String filtro);
    @Query(value = "select * from persona where (persona.apellidos like %?1% or persona.nombres like %?1%) and idrol = 3 and persona.estado=1 and idsede = ?2",nativeQuery = true)
    List<Persona> listarOperadoresPorFiltroyNombre(String nombre,String filtro);
    //Filtro para Usuarios
    //Filtro para Administradores
    //Filtro para Actores
    //Filtro para Directores


    //Busca a un usuario por correo
    Persona findByCorreo(String correo);

    //Actores
    @Query(nativeQuery = true, value = "SELECT * FROM persona " +
            "where idrol=5 and estado=1 and (lower(concat(nombres,' ',apellidos)) like %?1%) " +
            "limit ?2,?3")
    List<Persona> listarActores(String busqueda, int pag, int actoresxpagina);

    @Query(nativeQuery = true, value = "SELECT count(*) FROM persona " +
            "where idrol=5 and estado=1 and (lower(concat(nombres,' ',apellidos)) like %?1%) ")
    Integer cantActores(String busqueda);


    // Directores
    @Query(value = "SELECT * FROM persona where idrol=4",nativeQuery = true)
    List<Persona> listarDirectores();

    //Busca a un usuario por su token de password
    @Query(value = "select * from persona where persona.passwordtoken = ?1",nativeQuery = true)
    Persona buscarPersonaPorTokensito(String tokensito);


}