package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Genero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneroRepository extends JpaRepository<Genero,Integer> {



    @Query(value = "SELECT idgenero FROM telemillonario.funciongenero where idfuncion=?1 and estado=1;",nativeQuery = true)
    List<String> generosPorFuncion(int idfuncion);

    @Query(value = "SELECT idgenero FROM telemillonario.obragenero where idobra=?1 and estado=1;",nativeQuery = true)
    List<String> generosPorObra(int idobra);

}
