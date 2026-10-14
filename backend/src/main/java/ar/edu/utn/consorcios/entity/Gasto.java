package ar.edu.utn.consorcios.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Gastos")
@Getter
@Setter
@NoArgsConstructor
public class Gasto {

    @Id
    @GeneratedValue
    @Column(name = "Id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ConsorcioId", nullable = false)
    private Consorcio consorcio;

    @Column(name = "Descripcion", nullable = false, length = 300)
    private String descripcion;

    @Column(name = "Monto", nullable = false, precision = 18, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "Tipo", nullable = false, length = 15)
    private TipoGasto tipo;

    /** Fecha de imputacion: su mes define la liquidacion en que entra. */
    @Column(name = "Fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "Comprobante", length = 500)
    private String comprobante;
}
