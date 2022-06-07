package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Obra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ObraRepository extends JpaRepository<Obra, Integer> {

}