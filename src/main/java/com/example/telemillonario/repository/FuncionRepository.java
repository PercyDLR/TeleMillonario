package com.example.telemillonario.repository;

import com.example.telemillonario.entity.Funcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface FuncionRepository extends JpaRepository<Funcion, Integer> {


    Funcion findTopByOrderByIdDesc();

    @Query(nativeQuery = true, value = "select f.* from funcion f " +
            "inner join sala s on (s.id = f.idsala) " +
            "where f.estado=1 and s.idsede=?1 " +
            "limit ?2,?3")
    List<Funcion> buscarFuncionesPorSede( int idsede, int pag, int salasporpag);

    @Query(nativeQuery = true, value = "select * from telemillonario.funcion where " +
            "estado=?1 " +
            "limit ?2,?3")
    List<Funcion> buscarFuncion( int estado, int pag, int salasporpag);


   /*Validacion si la funcion existe en esa sede a dicha hora*/
    @Query(nativeQuery = true,value = "SELECT funcion.id,funcion.estado,funcion.fecha,funcion.inicio,funcion.fin,funcion.precioentrada,funcion.stockentradas,funcion.cantidadasistentes,funcion.idsala,funcion.idobra FROM funcion INNER JOIN sala ON ( funcion.idsala = sala.id) INNER JOIN sede ON ( sala.idsede = sede.id) INNER JOIN obra ON ( funcion.idobra = obra.id) WHERE obra.id = ?1 AND sede.id = ?2 AND funcion.fecha = ?3 AND funcion.inicio = ?4")
    Funcion encontrarFuncionHoraSede(int idObra, int idSede, LocalDate fecha, LocalTime hora);

    @Query(nativeQuery = true, value = "select f.* from sede s\n" +
            "inner join sala sa on (sa.idsede = s.id)\n" +
            "inner join funcion f on (f.idsala = sa.id)\n" +
            "where (s.estado = 1) and (sa.estado = 1) and (f.estado = 1) and (f.idobra = ?1) and (sa.idsede = ?2) and (f.idsala = ?3);")
    List<Funcion> listaFuncionesConObra(int idobra, int idsede, int idsala);

    @Query(nativeQuery = true, value = "SELECT count(*) FROM telemillonario.funcion where idobra=?1 ")
    Integer valCantFuncionConObra(int idobra);


    @Query(nativeQuery = true, value = "select f.* from funcion f " +
            "inner join sala s on (s.id = f.idsala) inner join obra o on (o.id=f.idobra) " +
            "where f.estado=1 and s.idsede=?1 and lower(o.nombre) like %?2% " +
            "limit ?3,?4")
    List<Funcion> buscarFuncionesPorSedeUsuar( int idsede,String nombre, int pag, int salasporpag);


    @Query(nativeQuery = true, value = "select f.* from funcion f " +
            "inner join sala s on (s.id = f.idsala) inner join obra o on (o.id=f.idobra) " +
            "where f.estado=1 and s.idsede=?1 and lower(o.nombre) like %?2% ")
    List<Funcion> FuncTotalSedeUsuar( int idsede,String nombre);


    @Query(nativeQuery = true, value = "select f.* from funcion f " +
            "inner join sala s on (s.id = f.idsala) inner join obra o on (o.id=f.idobra) " +
            "where f.estado=1 and s.idsede=?1 and lower(o.nombre) like %?2% and (o.restriccionedad=?3 or ?3>=2) " +
            "limit ?4,?5")
    List<Funcion> buscarFuncionesPorSedeUsuarFiltr( int idsede,String nombre,int restriccion, int pag, int salasporpag);


    @Query(nativeQuery = true, value = "select f.* from funcion f " +
            "inner join sala s on (s.id = f.idsala) inner join obra o on (o.id=f.idobra) " +
            "where f.estado=1 and s.idsede=?1 and lower(o.nombre) like %?2% and (o.restriccionedad=?3 or ?3>=2) ")
    List<Funcion> FuncTotalSedeUsuarFiltr( int idsede,String nombre,int restriccion);


 }
