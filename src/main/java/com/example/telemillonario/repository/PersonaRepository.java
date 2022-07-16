package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Foto;
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
    Persona findFirstByNombresOrderByIdDesc(String correo);

    //Actores
    @Query(nativeQuery = true, value = "SELECT * FROM persona " +
            "where idrol=5 and estado=1 and (lower(concat(nombres,' ',apellidos)) like %?1%) " +
            "limit ?2,?3")
    List<Persona> listarActores(String busqueda, int pag, int actoresxpagina);

    @Query(nativeQuery = true, value = "SELECT count(*) FROM persona " +
            "where idrol=5 and estado=1 and (lower(concat(nombres,' ',apellidos)) like %?1%) ")
    Integer cantActores(String busqueda);

    @Query(value = "SELECT fe.idpersona FROM funcionelenco fe\n" +
            "inner join persona p on (fe.idpersona=p.id)\n" +
            "where fe.idfuncion=?1 and fe.estado=1 and p.idrol=5",nativeQuery = true)
    List<String> actoresPorFuncion(int idfuncion);



    // Directores
    @Query(nativeQuery = true, value = "SELECT * FROM persona " +
            "where idrol=4 and estado=1 and (lower(concat(nombres,' ',apellidos)) like %?1%) " +
            "limit ?2,?3")
    List<Persona> listarDirectoresFiltrado(String busqueda, int pag, int directoresxpagina);

    @Query(nativeQuery = true, value = "SELECT count(*) FROM persona " +
            "where idrol=4 and estado=1 and (lower(concat(nombres,' ',apellidos)) like %?1%) ")
    Integer cantDirectores(String busqueda);


    @Query(value = "SELECT * FROM persona where idrol=4 and estado=1",nativeQuery = true)
    List<Persona> listarDirectores();
    @Query(value = "SELECT fe.idpersona FROM funcionelenco fe\n" +
            "inner join persona p on (fe.idpersona=p.id)\n" +
            "where fe.idfuncion=?1 and fe.estado=1 and p.idrol=4",nativeQuery = true)
    List<String> directoresPorFuncion(int idfuncion);

    //Busca a un usuario por su token de password
    @Query(value = "select * from persona where persona.passwordtoken = ?1",nativeQuery = true)
    Persona buscarPersonaPorTokensito(String tokensito);


    @Query(nativeQuery = true, value = "SELECT count(*) FROM telemillonario.persona " +
            "where idsede=?1 and estado=1 ")
    Integer valborrSedePers(int idsede);

    //Director
    @Query(nativeQuery = true, value = "Select * from telemillonario.persona\n" +
            "where (idrol = 4) and (estado = 1) and (lower(concat(nombres,' ',apellidos)) like %?1%)\n" +
            "limit ?2, ?3")
    List<Persona> buscarDirectores(String busqueda, int paginaxdirectores, int directores);

    @Query(nativeQuery = true, value = "Select * from telemillonario.persona\n" +
            "where (idrol = 4) and (estado = 1) and (lower(concat(nombres,' ',apellidos)) like %?1%)")
    List<Persona> cantidadDirectores(String busqueda);

    @Query(nativeQuery = true, value = "Select * from telemillonario.persona\n" +
            "where (idrol = 4) and (estado = 1) and (lower(concat(nombres,' ',apellidos)) like %?1%)\n" +
            "order by nombres")
    List<Persona> buscarDirectores1(String busqueda, int paginaxdirectores, int directores);

    @Query(nativeQuery = true, value = "Select * from telemillonario.persona\n" +
            "where (idrol = 4) and (estado = 1) and (lower(concat(nombres,' ',apellidos)) like %?1%)\n" +
            "order by nombres")
    List<Persona> cantidadDirectores1(String busqueda);

    @Query(nativeQuery = true, value = "Select * from telemillonario.persona\n" +
            "where (idrol = 4) and (estado = 1) and (lower(concat(nombres,' ',apellidos)) like %?1%)\n" +
            "order by nombres DESC\n" +
            "limit ?2, ?3")
    List<Persona> buscarDirectores2(String busqueda, int paginaxdirectores, int directores);

    @Query(nativeQuery = true, value = "Select * from telemillonario.persona\n" +
            "where (idrol = 4) and (estado = 1) and (lower(concat(nombres,' ',apellidos)) like %?1%)\n" +
            "order by nombres DESC")
    List<Persona> cantidadDirectores2(String busqueda);

    @Query(nativeQuery = true, value = "Select * from telemillonario.persona\n" +
            "where (idrol = 4) and (estado = 1) and (lower(concat(nombres,' ',apellidos)) like %?1%)\n" +
            "order by calificacion DESC\n" +
            "limit ?2, ?3")
    List<Persona> buscarDirectores3(String busqueda, int paginaxdirectores, int directores);

    @Query(nativeQuery = true, value = "Select * from telemillonario.persona\n" +
            "where (idrol = 4) and (estado = 1) and (lower(concat(nombres,' ',apellidos)) like %?1%)\n" +
            "order by calificacion DESC")
    List<Persona> cantidadDirectores3(String busqueda);

    //Actores
    @Query(nativeQuery = true, value = "Select * from telemillonario.persona\n" +
            "where (idrol = 5) and (estado = 1) and (lower(concat(nombres,' ',apellidos)) like %?1%)\n" +
            "limit ?2, ?3")
    List<Persona> buscarActores(String busqueda, int paginaxactores, int actores);

    @Query(nativeQuery = true, value = "Select * from telemillonario.persona\n" +
            "where (idrol = 5) and (estado = 1) and (lower(concat(nombres,' ',apellidos)) like %?1%)")
    List<Persona> cantidadActores(String busqueda);

    @Query(nativeQuery = true, value = "Select * from telemillonario.persona\n" +
            "where (idrol = 5) and (estado = 1) and (lower(concat(nombres,' ',apellidos)) like %?1%)\n" +
            "order by nombres\n" +
            "limit ?2, ?3")
    List<Persona> buscarActores1(String busqueda, int paginaxactores, int actores);

    @Query(nativeQuery = true, value = "Select * from telemillonario.persona\n" +
            "where (idrol = 5) and (estado = 1) and (lower(concat(nombres,' ',apellidos)) like %?1%)\n" +
            "order by nombres")
    List<Persona> cantidadActores1(String busqueda);

    @Query(nativeQuery = true, value = "Select * from telemillonario.persona\n" +
            "where (idrol = 5) and (estado = 1) and (lower(concat(nombres,' ',apellidos)) like %?1%)\n" +
            "order by nombres DESC\n" +
            "limit ?2, ?3")
    List<Persona> buscarActores2(String busqueda, int paginaxactores, int actores);

    @Query(nativeQuery = true, value = "Select * from telemillonario.persona\n" +
            "where (idrol = 5) and (estado = 1) and (lower(concat(nombres,' ',apellidos)) like %?1%)\n" +
            "order by nombres DESC")
    List<Persona> cantidadActores2(String busqueda);

    @Query(nativeQuery = true, value = "Select * from telemillonario.persona\n" +
            "where (idrol = 5) and (estado = 1) and (lower(concat(nombres,' ',apellidos)) like %?1%)\n" +
            "order by calificacion DESC\n" +
            "limit ?2, ?3")
    List<Persona> buscarActores3(String busqueda, int paginaxactores, int actores);

    @Query(nativeQuery = true, value = "Select * from telemillonario.persona\n" +
            "where (idrol = 5) and (estado = 1) and (lower(concat(nombres,' ',apellidos)) like %?1%)\n" +
            "order by calificacion DESC")
    List<Persona> cantidadActores3(String busqueda);

}