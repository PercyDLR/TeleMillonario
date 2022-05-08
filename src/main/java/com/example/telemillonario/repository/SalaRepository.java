package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Sala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaRepository extends JpaRepository<Sala,Integer> {

    @Query(nativeQuery = true, value = "select count(*) from telemillonario.sala where " +
            "idsede=?1 and (numero=?2 or ?2=0) and (estado=?3 or ?3>=2)")
    Integer cantSalas(int sede,int numero, int estado);

    @Query(nativeQuery = true, value = "select * from telemillonario.sala where " +
            "idsede=?1 and (numero=?2 or ?2=0) and (estado=?3 or ?3>=2)" +
            "limit ?4,?5")
    List<Sala> buscarSalas(int sede,int numero, int estado, int pag, int salasporpag);

    @Query(nativeQuery = true, value = "select * from telemillonario.sala where " +
            "idsede=?1 and (numero=?2 or ?2=0) and (estado=?3 or ?3>=2)" +
            "order by aforo " +
            "limit ?4,?5")
    List<Sala> buscarSalasAsc(int sede,int numero, int estado, int pag, int salasporpag);

    @Query(nativeQuery = true, value = "select * from telemillonario.sala where " +
            "idsede=?1 and (numero=?2 or ?2=0) and (estado=?3 or ?3>=2)" +
            "order by aforo desc " +
            "limit ?4,?5")
    List<Sala> buscarSalasDesc(int sede,int numero, int estado, int pag, int salasporpag);

    @Query(nativeQuery = true, value = "select * from telemillonario.sala where " +
            "idsede=?1 and (estado=?2)")
    List<Sala> buscarSalasTotal(int sede, int estado);

}
