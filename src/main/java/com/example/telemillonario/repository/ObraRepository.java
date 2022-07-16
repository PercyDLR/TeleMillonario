package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Funcion;
import com.example.telemillonario.entity.Obra;
import com.example.telemillonario.entity.Sede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ObraRepository extends JpaRepository<Obra, Integer> {

    Obra findTopByOrderByIdDesc();

    @Query(nativeQuery = true,value = "SELECT * FROM obra WHERE estado = 1 ORDER BY calificacion DESC LIMIT 12")
    List<Obra> obtenerObrasDestacadasPaginaPrincipal();


    @Query(nativeQuery = true, value = "Select count(*) from " +
            "(select any_value(fo.id) from telemillonario.fotos fo where fo.estado=1 and fo.idobra IS NOT NULL group by fo.idobra ) as Result")
    Integer cantObrasTotalAdmin();


    @Query(nativeQuery = true, value = "Select count(*) from " +
            "(select any_value(fo.id) from telemillonario.fotos fo inner join obra o on (fo.idobra=o.id) where " +
            "fo.estado=?1 and lower(o.nombre) like %?2% and fo.idobra IS NOT NULL  group by fo.idobra) as Result")
    Integer cantObrasTotalFiltrAdmin(int estado,String nombre);



}