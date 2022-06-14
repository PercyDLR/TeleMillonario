package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Foto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FotoRepository extends JpaRepository<Foto,Integer> {

    List<Foto> findByIdpersonaOrderByNumero(Integer id);

//    List<Foto> findByIdsedeOrderByNumero(int id);

    @Query(nativeQuery = true, value = "select fo.* from fotos fo " +
            "inner join funcion fu on fu.idobra = fo.idobra " +
            "inner join sala s on (s.id = fu.idsala) " +
            "where s.idsede=5 and fo.numero=0")
    List<Foto> buscarFotoObrasPorSede(int idsede);

    @Query(nativeQuery = true, value = "select * from fotos fo inner join obra o on (fo.idfuncion=o.id) where " +
            "fo.idsede=?1 and fo.estado=1 and lower(o.nombre) like %?2% and fo.idobra IS NOT NULL  group by fo.idobra limit ?3,?4")
    List<Foto> buscarFotoFuncionesPorPag(int idsede,String nombre,int pag, int salasporpag);

    @Query(nativeQuery = true, value = "select * from fotos fo " +
                        "inner join obra o on (fo.idobra=o.id) where idsede=?1 and (o.restriccionedad=?2 or ?2>=2)and lower(o.nombre) like %?3% and fo.estado=1 and idobra IS NOT NULL  group by idobra limit ?4,?5")
    List<Foto> buscarFotoFuncionesFiltrPorPag(int idsede,int restriccion,String nombre,int pag, int salasporpag);



    @Query(nativeQuery = true, value = "select * from fotos fo inner join obras o on (fo.idobra=o.id) where " +
            "fo.idsede=?1 and fo.estado=1 and lower(o.nombre) like %?2% and o.idfuncion IS NOT NULL  group by fo.idobra")
    List<Foto> fotFuncTotal(int idsede,String nombre);

    @Query(nativeQuery = true, value = "select * from fotos fo " +
            "inner join obra o on (fo.idobra=o.id) where idsede=?1 and (o.restriccionedad=?2 or ?2>=2)and lower(o.nombre) like %?3% and fo.estado=1 and idobra IS NOT NULL  group by idobra ")
    List<Foto> fotFuncFiltrTotal(int idsede,int restriccion,String nombre);



    @Query(nativeQuery = true, value = "select * from telemillonario.fotos fo inner join obra o on (o.id=fo.idobra) where " +
            "fo.idsede=?1 and fo.idobra IS NOT NULL and o.nombre like %?2% group by idobra " +
            "limit ?3,?4")
    List<Foto> buscarFotoFuncionesPorNombre(int idsede,String parametro, int pag, int salasporpag);

    @Query(nativeQuery = true, value = "select * from telemillonario.fotos " +
            "where idobra=?1 and estado=1 order by numero  ")
    List<Foto> buscarFotosFuncion(int idfuncion);

    @Query(nativeQuery = true, value = "select count(DISTINCT idobra) from telemillonario.fotos where " +
            "idsede=?1 and idobra IS NOT NULL and estado=1")
    Integer contarFunciones(int idsede);

    @Query(nativeQuery = true, value = "select * from telemillonario.fotos fo inner join obra o on (o.id=fo.idobra) where " +
            "fo.idsede=?1 and idobra IS NOT NULL and fo.estado=1 and fu.nombre like %?2% group by idobra")
    List<Foto> buscarFuncionesParaContarPorNombre(int idsede,String parametro);


    @Query(nativeQuery = true, value = "select any_value(fo.id) as id,fo.estado,any_value(fo.ruta) as ruta,any_value(fo.numero) as numero,any_value(fo.idpersona) as idpersona,any_value(fo.idsede) as idsede,any_value(fo.idobra) as idobra" +
            " from fotos fo where idobra IS NULL and idsede is not null and fo.estado=?1 " +
            "group by idsede "+
            "limit ?2,?3")
    List<Foto> listadoSedesAdmin(int estado,int pag, int salasporpag);

    @Query(nativeQuery = true, value = "select any_value(fo.id) as id,fo.estado,any_value(fo.ruta) as ruta,any_value(fo.numero) as numero,any_value(fo.idpersona) as idpersona,any_value(fo.idsede) as idsede,any_value(fo.idobra) as idobra from fotos fo inner join sede s on (fo.idsede=s.id) where s.estado=1 and idobra IS NULL and idsede is not null and fo.estado=?1 " +
            "group by idsede "+
            "limit ?2,?3")
    List<Foto> listadoSedesUsuar(int estado,int pag, int salasporpag);

    @Query(nativeQuery = true, value = "select any_value(fo.id) as id,fo.estado,any_value(fo.ruta) as ruta,any_value(fo.numero) as numero,any_value(fo.idpersona) as idpersona,any_value(fo.idsede) as idsede,any_value(fo.idobra) as idobra from fotos fo inner join sede s on (fo.idsede=s.id)\n" +
            "inner join distrito d on (s.iddistrito=d.id) \n" +
            "where idobra IS NULL and idsede is not null and lower(s.nombre) like %?1% and fo.estado=?2\n" +
            "group by idsede " +
            "limit ?3,?4")
    List<Foto> buscarSedePorNombre(String nombre,int estado,int pag, int salasporpag);

    @Query(nativeQuery = true, value = "select any_value(fo.id) as id,fo.estado,any_value(fo.ruta) as ruta,any_value(fo.numero) as numero,any_value(fo.idpersona) as idpersona,any_value(fo.idsede) as idsede,any_value(fo.idobra) as idobra from fotos fo inner join sede s on (fo.idsede=s.id)\n" +
            "inner join distrito d on (s.iddistrito=d.id) \n" +
            "where idobra IS NULL and idsede is not null and lower(d.nombre) like %?1% and fo.estado=?2\n" +
            "group by idsede " +
            "limit ?3,?4")
    List<Foto> buscarSedePorDistrito(String distrito,int estado,int pag, int salasporpag);

    @Query(nativeQuery = true, value = "select * from telemillonario.fotos where " +
            "idsede=?1 and idobra IS NULL and estado=1")
    List<Foto> buscarFotosSede(int idsede);

    @Query(nativeQuery = true, value = "SELECT * FROM telemillonario.fotos where (idobra = ?1) and (numero = 0);")
    Foto caratulaDeObra(int idobra);

    @Query(nativeQuery = true, value = "select * from telemillonario.fotos where idobra = ?1 order by numero;")
    List<Foto> fotosObra(int idobra);


    @Query(nativeQuery = true, value = "SELECT * FROM telemillonario.fotos where idobra IS NULL and estado=1 and idsede IS NOT NULL group by idsede")
    List<Foto> listSedesHabilitadas();

    @Query(nativeQuery = true, value = "select any_value(fo.id) as id,fo.estado,any_value(fo.ruta) as ruta,any_value(fo.numero) as numero,any_value(fo.idpersona) as idpersona,any_value(fo.idsede) as idsede,any_value(fo.idobra) as idobra from fotos fo where " +
            "fo.estado=?1 and fo.idobra IS NOT NULL group by fo.idobra " +
            "limit ?2,?3")
    List<Foto> listadoObrasFotoAdmin(int estado, int pag, int salasporpag);

    @Query(nativeQuery = true, value = "select any_value(fo.id) as id,fo.estado,any_value(fo.ruta) as ruta,any_value(fo.numero) as numero,any_value(fo.idpersona) as idpersona,any_value(fo.idsede) as idsede,any_value(fo.idobra) as idobra from fotos fo inner join obra o on (fo.idfuncion=o.id) where " +
            " fo.estado=?1 and lower(o.nombre) like %?2% and fo.idobra IS NOT NULL  group by fo.idobra limit ?3,?4")
    List<Foto> buscarObrasFotoPorNombreAdmin(int estado,String nombre,int pag, int salasporpag);

    @Query(nativeQuery = true, value = "select * from telemillonario.fotos where " +
            "idobra=?1 and estado=1")
    List<Foto> buscarFotosObra(int idobra);


    //Una foto cualquiera de la obra
    @Query(nativeQuery = true,value = "SELECT * FROM fotos group by idobra HAVING estado = 1")
    List<Foto> unaFotoPorObra();


}
