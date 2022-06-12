package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Funcion;
import com.example.telemillonario.entity.Obra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ObraRepository extends JpaRepository<Obra, Integer> {

    @Query(nativeQuery = true,value = "SELECT * FROM obra WHERE estado = 1 ORDER BY calificacion DESC LIMIT 12")
    List<Obra> obtenerObrasDestacadasPaginaPrincipal();

}