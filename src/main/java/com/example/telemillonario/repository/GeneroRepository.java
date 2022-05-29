package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Genero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneroRepository extends JpaRepository<Genero,Integer> {



    @Query(value = "SELECT idgenero FROM telemillonario.funciongenero where idfuncion=?1 and estado=1;",nativeQuery = true)
    List<Integer> generosPorFuncion(int idfuncion);
}
