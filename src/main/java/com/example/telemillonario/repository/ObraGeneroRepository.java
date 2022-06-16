package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Obragenero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObraGeneroRepository extends JpaRepository<Obragenero,Integer> {

    @Query(nativeQuery = true, value = "select * from telemillonario.obragenero where " +
            "idobra=?1")
    List<Obragenero> buscarFuncionGenero(int idfuncion);

    @Query(nativeQuery = true, value = "SELECT og.* FROM telemillonario.obragenero og\n" +
            "inner join telemillonario.obra o on (o.id = og.idobra)\n" +
            "inner join telemillonario.genero g on (og.idgenero = g.id)\n" +
            "where (o.estado = 1) and (o.restriccionedad like %?1%) and (g.id like %?2%) and (o.nombre like %?3%)\n" +
            "order by og.idobra;")
    List<Obragenero> buscarFuncionGeneroPorFiltros(String restriccionEdad, String genero, String nombreObra);

    @Query(nativeQuery = true, value = "SELECT og.* FROM telemillonario.funcion f\n" +
            "inner join telemillonario.sala sa on (f.idsala = sa.id)\n" +
            "inner join telemillonario.sede se on (sa.idsede = se.id)\n" +
            "inner join telemillonario.obra o on (f.idobra = o.id)\n" +
            "inner join telemillonario.obragenero og on (o.id = og.idobra)\n" +
            "inner join telemillonario.genero g on (og.idgenero = g.id)\n" +
            "where (o.estado = 1) and (se.id = ?1) and (o.restriccionedad like %?2%) and (g.id like %?3%) and (o.nombre like %?4%)\n" +
            "order by og.idobra;")
    List<Obragenero> buscarFuncionGeneroPorFiltrosSede(Integer idSede ,String restriccionEdad, String genero, String nombreObra);


    @Query(nativeQuery = true, value = "select * from telemillonario.obragenero where " +
            "idobra=?1")
    List<Obragenero> buscarObraGenero(int idobra);



}
