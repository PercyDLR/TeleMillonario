package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Funcion;
import com.example.telemillonario.entity.Funcionelenco;
import com.example.telemillonario.entity.Funciongenero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuncionGeneroRepository extends JpaRepository<Funciongenero,Integer> {

    @Query(nativeQuery = true, value = "select * from telemillonario.funciongenero where " +
            "idfuncion=?1")
    List<Funciongenero> buscarFuncionGenero(int idfuncion);

    @Query(nativeQuery = true, value = "SELECT fg.* FROM telemillonario.funciongenero fg\n" +
            "inner join telemillonario.funcion f on (f.id = fg.idfuncion)\n" +
            "inner join telemillonario.genero g on (fg.idgenero = g.id)\n" +
            "where (f.estado = 1) and (f.restriccionedad like %?1%) and (g.id like %?2%) and (f.nombre like %?3%) \n" +
            "order by fg.idfuncion")
    List<Funciongenero> buscarFuncionGeneroPorFiltros(String restriccionEdad, String genero, String nombreFuncion);




}
