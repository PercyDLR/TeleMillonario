package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    List<Rol> findByEstado(int estado);
    Rol findByNombre(String rol);
}