package com.example.telemillonario.entity;

import com.example.telemillonario.validation.Elenco;
import com.example.telemillonario.validation.Operador;
import com.example.telemillonario.validation.Usuario;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@Entity
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "estado")
    private String estado;

    @Column(name = "idtarjeta")
    private int idTarjeta;//Aca va la entidad tarjeta

    @Column(name = "numerotarjeta")
    private String numeroTarjeta;

    @Column(name = "fechapago")
    private LocalDate fechaPago;

    @ManyToOne
    @JoinColumn(name = "idcompra")
    private Compra idCompra;

    @Column(name = "qrlink")
    private String qr; //se pondra como arreglo de bytes?

    @Column(name = "codigo")
    private String codigo;
}
