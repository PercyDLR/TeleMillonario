package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago,Integer> {

    @Query(nativeQuery = true,value = "SELECT * FROM pago WHERE idcompra = ?1")
    Pago encontrarPagoPorIdCompra(int idCompra);

    @Query(nativeQuery = true, value = "select * from telemillonario.pago\n" +
            "where codigo = ?1")
    List<Pago> listaPago(String codigo);

}
