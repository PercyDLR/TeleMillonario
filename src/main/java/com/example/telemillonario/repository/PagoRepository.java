package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago,Integer> {

    @Query(nativeQuery = true,value = "SELECT * FROM pago WHERE idcompra = ?1")
    Pago encontrarPagoPorIdCompra(int idCompra);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update telemillonario.pago\n" +
            "set estado = 0\n" +
            "where idcompra = ?1")
    void cancelarPago(Integer idcompra);

    @Query(nativeQuery = true, value = "select * from telemillonario.pago\n" +
            "where codigo = ?1")
    List<Pago> listaPago(String codigo);

}
