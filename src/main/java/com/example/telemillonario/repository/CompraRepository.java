package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra,Integer> {

    @Query(nativeQuery = true,value = "SELECT * FROM compra WHERE idpersona = ?1")
    List<Compra> historialCompras( int idPersona);
}
