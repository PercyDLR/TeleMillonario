package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Foto;
import com.example.telemillonario.entity.Funcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FotoRepository extends JpaRepository<Foto,Integer> {

    List<Foto> findByIdpersonaOrderByNumero(int id);

    List<Foto> findByIdsedeOrderByNumero(int id);

    @Query(nativeQuery = true, value = "select * from fotos where " +
            "idsede=?1 group by idfuncion " +
            "limit ?2,?3")
    List<Foto> buscarFotoFunciones(int idsede, int pag, int salasporpag);

    @Query(nativeQuery = true, value = "select * from telemillonario.fotos fo inner join funcion fu on (fu.id=fo.idfuncion) where " +
            "fo.idsede=?1 and fu.nombre like %?2% group by idfuncion " +
            "limit ?3,?4")
    List<Foto> buscarFotoFuncionesPorNombre(int idsede,String parametro, int pag, int salasporpag);

    @Query(nativeQuery = true, value = "select * from telemillonario.fotos where " +
            "idfuncion=?1")
    List<Foto> buscarFotosFuncion(int idfuncion);

    @Query(nativeQuery = true, value = "select * from telemillonario.fotos where " +
            "idsede=?1 and estado=1 group by idfuncion")
    List<Foto> buscarFuncionesParaContar(int idsede);

    @Query(nativeQuery = true, value = "select * from telemillonario.fotos fo inner join funcion fu on (fu.id=fo.idfuncion) where " +
            "fo.idsede=?1 and fo.estado=1 and fu.nombre like %?2% group by idfuncion")
    List<Foto> buscarFuncionesParaContarPorNombre(int idsede,String parametro);



}
