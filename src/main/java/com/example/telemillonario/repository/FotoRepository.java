package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Foto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FotoRepository extends JpaRepository<Foto,Integer> {

    List<Foto> findByIdpersonaOrderByNumero(int id);
    List<Foto> findByIdsedeOrderByNumero(int id);
    List<Foto> findByIdfuncionOrderByNumero(int id);
}
