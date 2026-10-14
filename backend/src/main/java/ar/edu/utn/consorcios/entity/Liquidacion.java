package ar.edu.utn.consorcios.entity;

import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Table(name = "Liquidaciones")
@Getter
@Setter
@NoArgsConstructor
public class Liquidacion {

    @Id
    @GeneratedValue
    @Column(name = "Id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ConsorcioId", nullable = false)
    private Consorcio consorcio;

    /** Formato YYYY-MM. */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "Periodo", nullable = false, length = 7, columnDefinition = "char(7)")
    private String periodo;

    @Column(name = "FechaGeneracion", nullable = false)
    private LocalDate fechaGeneracion = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "Estado", nullable = false, length = 10)
    private EstadoLiquidacion estado = EstadoLiquidacion.ABIERTA;

    public void cerrar() {
        this.estado = EstadoLiquidacion.CERRADA;
    }
}
