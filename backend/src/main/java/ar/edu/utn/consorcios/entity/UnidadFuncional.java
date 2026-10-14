package ar.edu.utn.consorcios.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "UnidadesFuncionales")
@Getter
@Setter
@NoArgsConstructor
public class UnidadFuncional {

    @Id
    @GeneratedValue
    @Column(name = "Id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ConsorcioId", nullable = false)
    private Consorcio consorcio;

    @Column(name = "Identificador", nullable = false, length = 20)
    private String identificador;

    @Column(name = "CoeficientePropiedad", nullable = false, precision = 9, scale = 6)
    private BigDecimal coeficientePropiedad;
}
