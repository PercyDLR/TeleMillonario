package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra,Integer> {

    @Query(nativeQuery = true,value = "select c.* from telemillonario.compra c\n" +
            "inner join telemillonario.pago p on (p.idcompra = c.id)\n" +
            "where c.idpersona = ?1\n" +
            "order by p.fechapago desc;")
    List<Compra> historialCompras(int idPersona);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update telemillonario.compra\n" +
            "set estado = ?1\n" +
            "where idpersona = ?2 and id = ?3")
    void actualizacionEstadoCompra(String estado, Integer idpersona, Integer idcompra);

    @Query(nativeQuery = true,value = "select c.* from telemillonario.compra c\n" +
            "inner join telemillonario.pago p on (p.idcompra = c.id)\n" +
            "order by p.fechapago desc " +
            "limit ?1,?2 ")
    List<Compra> historialComprasGeneral(int pag, int salasporpag);


    @Query(nativeQuery = true, value = "SELECT count(*) FROM telemillonario.compra ")
    Integer cantComprasTotal();



    @Query(nativeQuery = true,value = "select c.* from telemillonario.compra c\n" +
            "inner join telemillonario.pago p on (p.idcompra = c.id)\n" +
            "inner join telemillonario.persona per on (per.id = c.idpersona)\n" +
            "where lower(per.dni) like %?1%\n" +
            "order by p.fechapago desc " +
            "limit ?2,?3 ")
    List<Compra> buscarCompraPorDni(String nombre,int pag, int salasporpag);

    @Query(nativeQuery = true,value = "select * from telemillonario.compra where idfuncion = ?1")
    List<Compra> buscarCompraPorFuncion(Integer id);

    @Query(nativeQuery = true,value = "select count(*) from telemillonario.compra c\n" +
            "inner join telemillonario.pago p on (p.idcompra = c.id)\n" +
            "inner join telemillonario.persona per on (per.id = c.idpersona)\n" +
            "where lower(per.dni) like %?1%\n" +
            "order by p.fechapago desc ")
    Integer cantComprasPorDni(String nombre);


    @Query(nativeQuery = true,value = "select c.* from telemillonario.compra c\n" +
            "inner join telemillonario.pago p on (p.idcompra = c.id)\n" +
            "inner join telemillonario.persona per on (per.id = c.idpersona)\n" +
            "where lower(per.nombres) like %?1%\n" +
            "order by p.fechapago desc " +
            "limit ?2,?3 ")
    List<Compra> buscarCompraPorNombre(String nombre,int pag, int salasporpag);

    @Query(nativeQuery = true,value = "select count(*) from telemillonario.compra c\n" +
            "inner join telemillonario.pago p on (p.idcompra = c.id)\n" +
            "inner join telemillonario.persona per on (per.id = c.idpersona)\n" +
            "where lower(per.nombres) like %?1%\n" +
            "order by p.fechapago desc ")
    Integer cantComprasPorNombre(String nombre);

    @Query(nativeQuery = true,value = "select c.* from telemillonario.compra c\n" +
            "inner join telemillonario.pago p on (p.idcompra = c.id)\n" +
            "inner join telemillonario.persona per on (per.id = c.idpersona)\n" +
            "where lower(per.apellidos) like %?1%\n" +
            "order by p.fechapago desc " +
            "limit ?2,?3 ")
    List<Compra> buscarCompraPorApellido(String nombre,int pag, int salasporpag);

    @Query(nativeQuery = true,value = "select count(*) from telemillonario.compra c\n" +
            "inner join telemillonario.pago p on (p.idcompra = c.id)\n" +
            "inner join telemillonario.persona per on (per.id = c.idpersona)\n" +
            "where lower(per.apellidos) like %?1%\n" +
            "order by p.fechapago desc ")
    Integer cantComprasPorApellido(String nombre);


    @Query(nativeQuery = true,value = "select c.* from telemillonario.compra c\n" +
            "inner join telemillonario.pago p on (p.idcompra = c.id)\n" +
            "inner join telemillonario.persona per on (per.id = c.idpersona)\n" +
            "where lower(per.telefono) like %?1%\n" +
            "order by p.fechapago desc " +
            "limit ?2,?3 ")
    List<Compra> buscarCompraPorNumero(String nombre,int pag, int salasporpag);


    @Query(nativeQuery = true,value = "select count(*) from telemillonario.compra c\n" +
            "inner join telemillonario.pago p on (p.idcompra = c.id)\n" +
            "inner join telemillonario.persona per on (per.id = c.idpersona)\n" +
            "where lower(per.telefono) like %?1%\n" +
            "order by p.fechapago desc ")
    Integer cantComprasPorNumero(String nombre);

    @Query(nativeQuery = true,value = "select c.* from telemillonario.compra c\n" +
            "inner join telemillonario.pago p on (p.idcompra = c.id)\n" +
            "inner join telemillonario.funcion f on (f.id = c.idfuncion)\n" +
            "inner join telemillonario.sala sa on (sa.id = f.idsala)\n" +
            "inner join telemillonario.sede sed on (sed.id = sa.idsede)\n" +
            "where lower(sed.nombre) like %?1%\n" +
            "order by p.fechapago desc " +
            "limit ?2,?3 ")
    List<Compra> buscarCompraPorSede(String nombre,int pag, int salasporpag);


    @Query(nativeQuery = true,value = "select count(*) from telemillonario.compra c\n" +
            "inner join telemillonario.pago p on (p.idcompra = c.id)\n" +
            "inner join telemillonario.funcion f on (f.id = c.idfuncion)\n" +
            "inner join telemillonario.sala sa on (sa.id = f.idsala)\n" +
            "inner join telemillonario.sede sed on (sed.id = sa.idsede)\n" +
            "where lower(sed.nombre) like %?1%\n" +
            "order by p.fechapago desc ")
    Integer cantComprasPorSede(String nombre);



}
