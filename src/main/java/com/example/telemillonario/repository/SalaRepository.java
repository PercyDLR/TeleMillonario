package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Sala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaRepository extends JpaRepository<Sala,Integer> {

    @Query(nativeQuery = true, value = "SELECT * FROM telemillonario.sala where idsede= ?1")
    List<Sala> buscarSalaPorSede(int idsede);

    @Query(nativeQuery = true, value = "select * from telemillonario.sala where numero= ?1 and idsede= ?2")
    List<Sala> buscarPorNumero(int numero,int idsede);

    @Query(nativeQuery = true, value = "select * from telemillonario.sala where estado= ?1 and idsede= ?2")
    List<Sala> buscarPorEstado(int estado,int idsede);

    @Query(nativeQuery = true, value = "SELECT * FROM telemillonario.sala where idsede=?1 order by aforo asc;")
    List<Sala> sortMayor(int idsede);

    @Query(nativeQuery = true, value = "SELECT * FROM telemillonario.sala where idsede=?1 order by aforo desc;")
    List<Sala> sortMenor(int idsede);
}
