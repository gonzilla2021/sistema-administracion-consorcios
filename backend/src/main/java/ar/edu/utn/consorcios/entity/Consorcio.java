package ar.edu.utn.consorcios.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Consorcios")
@Getter
@Setter
@NoArgsConstructor
public class Consorcio {

    @Id
    @GeneratedValue
    @Column(name = "Id")
    private UUID id;

    @Column(name = "Nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "Direccion", nullable = false, length = 250)
    private String direccion;

    @Column(name = "TotalCoeficientes", nullable = false, precision = 9, scale = 6)
    private BigDecimal totalCoeficientes = new BigDecimal("100");

    @Column(name = "FechaAlta", nullable = false)
    private LocalDate fechaAlta = LocalDate.now();
}
