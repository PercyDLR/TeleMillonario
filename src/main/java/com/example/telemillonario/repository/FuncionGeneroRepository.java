package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Funcionelenco;
import com.example.telemillonario.entity.Funciongenero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuncionGeneroRepository extends JpaRepository<Funciongenero,Integer> {

    @Query(nativeQuery = true, value = "select * from telemillonario.funciongenero where " +
            "idfuncion=?1")
    List<Funciongenero> buscarFuncionGenero(int idfuncion);
}
