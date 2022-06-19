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
    List<Compra> buscarReseñasObra(int idobra);

    @Query(nativeQuery = true,value = "SELECT * FROM calificaciones WHERE idsede = ?1 and estado=1 ")
    List<Compra> buscarReseñasSede(int idsede);


    @Query(nativeQuery = true,value = "SELECT TRUNCATE(AVG(calificacion),1) FROM calificaciones WHERE idobra = ?1 and estado=1 ")
    Integer PromCalificacionOBra(int idobra);

    @Query(nativeQuery = true,value = "SELECT TRUNCATE(AVG(calificacion),1) FROM calificaciones WHERE idpersona = ?1 and estado=1 ")
    Integer PromCalificacionElenco(int idpersona);


    @Query(nativeQuery = true,value = "SELECT TRUNCATE(AVG(calificacion),1) FROM calificaciones WHERE idsede = ?1 and estado=1 ")
    Integer PromCalificacionSede(int idpersona);
}
