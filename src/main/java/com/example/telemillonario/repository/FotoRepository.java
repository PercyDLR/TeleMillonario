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

//    List<Foto> findByIdsedeOrderByNumero(int id);

    @Query(nativeQuery = true, value = "select * from fotos where " +
            "idsede=?1 and idfuncion IS NOT NULL group by idfuncion " +
            "limit ?2,?3")
    List<Foto> buscarFotoFunciones(int idsede, int pag, int salasporpag);

    @Query(nativeQuery = true, value = "select * from telemillonario.fotos fo inner join funcion fu on (fu.id=fo.idfuncion) where " +
            "fo.idsede=?1 and idfuncion IS NOT NULL and fu.nombre like %?2% group by idfuncion " +
            "limit ?3,?4")
    List<Foto> buscarFotoFuncionesPorNombre(int idsede,String parametro, int pag, int salasporpag);

    @Query(nativeQuery = true, value = "select * from telemillonario.fotos where " +
            "idfuncion=?1 ")
    List<Foto> buscarFotosFuncion(int idfuncion);

    @Query(nativeQuery = true, value = "select * from telemillonario.fotos where " +
            "idsede=?1 and idfuncion IS NOT NULL and estado=1 group by idfuncion")
    List<Foto> buscarFuncionesParaContar(int idsede);

    @Query(nativeQuery = true, value = "select * from telemillonario.fotos fo inner join funcion fu on (fu.id=fo.idfuncion) where " +
            "fo.idsede=?1 and idfuncion IS NOT NULL and fo.estado=1 and fu.nombre like %?2% group by idfuncion")
    List<Foto> buscarFuncionesParaContarPorNombre(int idsede,String parametro);


    @Query(nativeQuery = true, value = "select * from fotos where idfuncion IS NULL and idsede is not null and estado=?1 group by idsede")
    List<Foto> listadoSedes(int estado);


    @Query(nativeQuery = true, value = "select fo.* from fotos fo inner join sede s on (fo.idsede=s.id)\n" +
            "inner join distrito d on (s.iddistrito=d.id) \n" +
            "where idfuncion IS NULL and idsede is not null and lower(s.nombre) like %?1% and fo.estado=?2\n" +
            "group by idsede " +
            "limit ?3,?4")
    List<Foto> buscarSedePorNombre(String nombre,int estado,int pag, int salasporpag);

    @Query(nativeQuery = true, value = "select fo.* from fotos fo inner join sede s on (fo.idsede=s.id)\n" +
            "inner join distrito d on (s.iddistrito=d.id) \n" +
            "where idfuncion IS NULL and idsede is not null and lower(d.nombre) like %?1% and fo.estado=?2\n" +
            "group by idsede " +
            "limit ?3,?4")
    List<Foto> buscarSedePorDistrito(String distrito,int estado,int pag, int salasporpag);

}
