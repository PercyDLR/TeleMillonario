package com.example.telemillonario.repository;

import com.example.telemillonario.dto.EstadisticaFuncionDto;
import com.example.telemillonario.dto.EstadisticasPersonaDto;
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

    //Querys para estadisticas de la funcion
    //funcion mas vista
    @Query(nativeQuery = true, value = "select o.nombre as nombre,funcion.id as funcionid,o.id as obraid,round((funcion.cantidadasistentes/funcion.stockentradas)*100,0) as pasistencia,o.calificacion,s.idsede as sedeid, f.ruta as url from funcion inner join obra o on funcion.idobra = o.id\n" +
            "inner join sala s on funcion.idsala = s.id\n" +
            "inner join fotos f on o.id = f.idobra\n" +
            "where s.idsede=?1 order by pasistencia desc limit 1")
    EstadisticaFuncionDto obtenerFuncionMasVistaxSede(int idsede);
    //funcion menos vista
    @Query(nativeQuery = true,value = "select o.nombre as nombre,funcion.id as funcionid,o.id as obraid,round((funcion.cantidadasistentes/funcion.stockentradas)*100,0) as pasistencia,o.calificacion,s.idsede as sedeid, f.ruta as url from funcion inner join obra o on funcion.idobra = o.id\n" +
            "inner join sala s on funcion.idsala = s.id\n" +
            "inner join fotos f on o.id = f.idobra\n" +
            "where s.idsede=?1 order by pasistencia asc limit 1;")
    EstadisticaFuncionDto obtenerFuncionMenosVistaxSede(int idsede);
    //funcion mejor calificada
    @Query(nativeQuery = true,value = "select o.nombre as nombre,funcion.id as funcionid,o.id as obraid,round((funcion.cantidadasistentes/funcion.stockentradas)*100,0) as pasistencia,o.calificacion,s.idsede as sedeid, f.ruta as url from funcion inner join obra o on funcion.idobra = o.id\n" +
            "inner join sala s on funcion.idsala = s.id\n" +
            "inner join fotos f on o.id = f.idobra\n" +
            "where s.idsede=?1 order by calificacion desc limit 1")
    EstadisticaFuncionDto obtenerFuncionMejorCalificadaxSede(int idsede);
    //funciones agrupadas por el porcentaje de asistencia
    @Query(nativeQuery = true,value = "select o.nombre as nombre,funcion.id as funcionid,o.id as obraid,round((funcion.cantidadasistentes/funcion.stockentradas)*100,0) as pasistencia,o.calificacion,s.idsede as sedeid,f.ruta as url from funcion inner join obra o on funcion.idobra = o.id\n" +
            "inner join sala s on funcion.idsala = s.id\n" +
            "inner join fotos f on o.id = f.idobra\n" +
            "where s.idsede=?1 and f.ruta = (select fotos.ruta from fotos where fotos.idobra=o.id limit 1) order by pasistencia desc")
    List<EstadisticaFuncionDto> obtenerFuncionesxAsistenciaxSede(int idsede);
    //Actores con mejor calificacion de acuerdo a sede
   @Query(nativeQuery = true,value = "select persona.id as personaid,persona.nombres as nombres,persona.apellidos as apellidos,persona.calificacion as calificacion,f3.ruta as url,s2.id as sedeid from persona\n" +
           "inner join funcionelenco f on persona.id = f.idpersona\n" +
           "inner join funcion f2 on f.idfuncion = f2.id\n" +
           "inner join sala s on f2.idsala = s.id\n" +
           "inner join sede s2 on s.idsede = s2.id\n" +
           "inner join fotos f3 on persona.id = f3.idpersona\n" +
           "where s2.id = ?1 and persona.idrol = 5 and f3.ruta = (select fotos.ruta from fotos where fotos.idpersona=persona.id limit 1) and f2.id = (select funcion.id from funcion\n" +
           "inner join funcionelenco f on funcion.id = f.idfuncion\n" +
           "inner join  persona p on f.idpersona = p.id\n" +
           "inner join sala s on funcion.idsala = s.id\n" +
           "inner join sede s2 on s.idsede = s2.id\n" +
           "where p.id = persona.id and p.idrol=5 and s2.id=?1 limit 1) order by persona.calificacion desc")
   List<EstadisticasPersonaDto> obtenerActoresMejorCalificadosxSede(int sede);
   //Directores con mejor calificaicon de acuerdo a sede
   @Query(nativeQuery = true,value = "select persona.id as personaid,persona.nombres as nombres,persona.apellidos as apellidos,persona.calificacion as calificacion,f3.ruta as url,s2.id as sedeid from persona\n" +
           "inner join funcionelenco f on persona.id = f.idpersona\n" +
           "inner join funcion f2 on f.idfuncion = f2.id\n" +
           "inner join sala s on f2.idsala = s.id\n" +
           "inner join sede s2 on s.idsede = s2.id\n" +
           "inner join fotos f3 on persona.id = f3.idpersona\n" +
           "where s2.id = ?1 and persona.idrol = 4 and f3.ruta = (select fotos.ruta from fotos where fotos.idpersona=persona.id limit 1) and f2.id = (select funcion.id from funcion\n" +
           "inner join funcionelenco f on funcion.id = f.idfuncion\n" +
           "inner join  persona p on f.idpersona = p.id\n" +
           "inner join sala s on funcion.idsala = s.id\n" +
           "inner join sede s2 on s.idsede = s2.id\n" +
           "where p.id = persona.id and p.idrol=4 and s2.id=?1 limit 1) order by persona.calificacion desc")
   List<EstadisticasPersonaDto> obtenerDirectoresMejorCalificadosxSede(int sede);
   //Nota se puede mejorar y optimizar si se considera un parámetro de entrada
 }
