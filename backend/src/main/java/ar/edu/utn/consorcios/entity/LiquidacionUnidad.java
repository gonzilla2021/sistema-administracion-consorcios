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
@Table(name = "LiquidacionesUnidad")
@Getter
@Setter
@NoArgsConstructor
public class LiquidacionUnidad {

    @Id
    @GeneratedValue
    @Column(name = "Id")
    private UUID id;

    /** Redundante con liquidacion y unidad: la BD lo usa para garantizar que ambas sean del mismo consorcio. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ConsorcioId", nullable = false)
    private Consorcio consorcio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "LiquidacionId", nullable = false)
    private Liquidacion liquidacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "UnidadFuncionalId", nullable = false)
    private UnidadFuncional unidadFuncional;

    @Column(name = "Importe", nullable = false, precision = 18, scale = 2)
    private BigDecimal importe;

    @Enumerated(EnumType.STRING)
    @Column(name = "EstadoPago", nullable = false, length = 10)
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;

    @Column(name = "FechaPago")
    private LocalDate fechaPago;

    @Column(name = "VisibleParaInquilino", nullable = false)
    private boolean visibleParaInquilino = false;

    public void marcarPagada(LocalDate fecha) {
        this.estadoPago = EstadoPago.PAGADO;
        this.fechaPago = fecha;
    }
}
