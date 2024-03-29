package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Calificaciones;
import com.example.telemillonario.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalificacionesRepository extends JpaRepository<Calificaciones,Integer> {


    @Query(nativeQuery = true,value = "SELECT * FROM calificaciones WHERE idobra = ?1 and estado=1 ")
    List<Calificaciones> buscarReseñasObra(int idobra);

    @Query(nativeQuery = true,value = "SELECT * FROM calificaciones WHERE idsede = ?1 and estado=1 ")
    List<Calificaciones> buscarReseñasSede(int idsede);


    @Query(nativeQuery = true,value = "SELECT TRUNCATE(AVG(calificacion),1) FROM calificaciones WHERE idobra = ?1 and estado=1 ")
    Double PromCalificacionOBra(int idobra);

    @Query(nativeQuery = true,value = "SELECT TRUNCATE(AVG(calificacion),1) FROM calificaciones WHERE idelenco = ?1 and estado=1 ")
    Double PromCalificacionPersonaElenco(int idpersona);


    @Query(nativeQuery = true,value = "SELECT TRUNCATE(AVG(calificacion),1) FROM calificaciones WHERE idsede = ?1 and estado=1 ")
    Double PromCalificacionSede(int idsede);


    @Query(nativeQuery = true,value = "SELECT count(*) FROM calificaciones WHERE idsede = ?1 and estado=1 and calificacion>=3 ")
    Integer CantResePosi(int idsede);

    @Query(nativeQuery = true,value = "SELECT count(*) FROM calificaciones WHERE idsede = ?1 and estado=1 and calificacion<3 ")
    Integer CantReseNega(int idsede);

    @Query(nativeQuery = true, value = "SELECT * FROM telemillonario.calificaciones\n" +
            "where (estado = 1) and (idpersona = ?1);")
    List<Calificaciones> listaCalificacionesUsuario(Integer idpersona);

}
