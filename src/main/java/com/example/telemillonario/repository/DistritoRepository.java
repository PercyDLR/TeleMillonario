package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Distrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DistritoRepository extends JpaRepository<Distrito,Integer> {
}
