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

    @Query(nativeQuery = true, value = "select * from employees where identificador= ?1")
    List<Sala> buscarPorIden(String identificador);

    @Query(nativeQuery = true, value = "select * from employees where numero= ?1")
    List<Sala> buscarPorNumero(String numero);

    @Query(nativeQuery = true, value = "select * from employees where estado= ?1")
    List<Sala> buscarPorEstado(int estado);
}
