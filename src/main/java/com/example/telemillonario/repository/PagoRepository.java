package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Compra;
import com.example.telemillonario.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoRepository extends JpaRepository<Pago,Integer> {



}
