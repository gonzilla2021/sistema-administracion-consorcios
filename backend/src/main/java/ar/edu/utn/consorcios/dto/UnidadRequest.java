package ar.edu.utn.consorcios.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UnidadRequest(
        @NotBlank @Size(max = 20) String identificador,
        @NotNull @DecimalMin("0.000001") @DecimalMax("100") BigDecimal coeficientePropiedad) {
}
