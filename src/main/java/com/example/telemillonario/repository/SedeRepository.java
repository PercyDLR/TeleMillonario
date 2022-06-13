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
            "(select any_value(fo.id) from telemillonario.fotos fo where fo.idobra IS NULL and fo.idsede is not null and fo.estado=1 group by idsede ) as Result")
    Integer cantSedesTotalAdmin();

    @Query(nativeQuery = true, value = "Select count(*) from " +
            "(select fo.* from fotos fo inner join sede s on (fo.idsede=s.id) where s.estado=1 and  fo.idfuncion IS NULL and fo.idsede is not null and fo.estado=1 group by idsede ) as Result;")
    Integer cantSedesTotalUsuar();

    @Query(nativeQuery = true, value = "Select count(*) from " +
            "(select fo.* from fotos fo inner join sede s on (fo.idsede=s.id) inner join distrito d on (s.iddistrito=d.id) " +
            "where idfuncion IS NULL and idsede is not null and lower(s.nombre) like %?1% and fo.estado=?2 group by idsede ) as Result;")
    Integer cantSedesFiltr(String nombre,int estado);


    @Query(nativeQuery = true, value = "SELECT * FROM telemillonario.sede where estado=?1")
    List<Sede> listaSedesHabilitadas(int estado);

    @Query(nativeQuery = true, value = "select s.* from sede s\n" +
            "inner join sala sa on (sa.idsede = s.id)\n" +
            "inner join funcion f on (f.idsala = sa.id)\n" +
            "where (s.estado = 1) and (sa.estado = 1) and (f.estado = 1) and (f.idobra = ?1);")
    List<Sede> listaSedesConObra(int idobra);

}
