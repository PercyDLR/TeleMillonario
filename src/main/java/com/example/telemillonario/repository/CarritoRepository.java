package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarritoRepository extends JpaRepository<Compra,Integer> {


    @Query(nativeQuery = true,value = "select * FROM carrito WHERE idpersona = ? AND estado = 1")
    List<Compra> obtenerCarritoUsuario(int idPersona);
}
