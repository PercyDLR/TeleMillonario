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

}
