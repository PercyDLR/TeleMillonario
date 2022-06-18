package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoRepository extends JpaRepository<Pago,Integer> {

    @Query(nativeQuery = true,value = "SELECT * FROM pago WHERE idcompra = ?1")
    Pago encontrarPagoPorIdCompra(int idCompra);


}
