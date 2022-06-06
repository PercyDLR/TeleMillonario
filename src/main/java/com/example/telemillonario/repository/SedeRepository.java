package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Sede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SedeRepository extends JpaRepository<Sede,Integer> {

    Sede findTopByOrderByIdDesc();

    List<Sede> findByEstado(int estado);

    @Query(nativeQuery = true, value = "Select count(*) from " +
            "(select * from fotos where idfuncion IS NULL and idsede is not null and estado=1 group by idsede ) as Result;")
    Integer cantSedesTotal();

    @Query(nativeQuery = true, value = "Select count(*) from " +
            "(select fo.* from fotos fo inner join sede s on (fo.idsede=s.id) inner join distrito d on (s.iddistrito=d.id) " +
            "where idfuncion IS NULL and idsede is not null and lower(s.nombre) like %?1% and fo.estado=?2 group by idsede ) as Result;")
    Integer cantSedesFiltr(String nombre,int estado);


    @Query(nativeQuery = true, value = "SELECT * FROM telemillonario.sede where estado=?1")
    List<Sede> listaSedesHabilitadas(int estado);

}
