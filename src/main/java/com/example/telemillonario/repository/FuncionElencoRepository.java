package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Funcionelenco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FuncionElencoRepository extends JpaRepository<Funcionelenco, Integer> {


}
