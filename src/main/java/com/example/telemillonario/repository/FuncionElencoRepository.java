package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Funcionelenco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuncionElencoRepository extends JpaRepository<Funcionelenco, Integer> {



    @Query(nativeQuery = true, value = "select * from telemillonario.funcionelenco where " +
            "idfuncion=?1")
    List<Funcionelenco> buscarFuncionElenco(int idfuncion);

    @Query(nativeQuery = true, value = "select * from telemillonario.funcionelenco where " +
            "idpersona=?1")
    List<Funcionelenco> buscarFuncionElencoPorActorDirector(int idPersona);

}
