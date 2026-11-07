
package ar.edu.utn.consorcios.dto;

import ar.edu.utn.consorcios.entity.TipoGasto;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GastoRequest(

        @NotBlank
        @Size(max = 300)
        String descripcion,

        @NotNull
        @DecimalMin(value = "0.00", inclusive = false)
        @Digits(integer = 16, fraction = 2)
        BigDecimal monto,

        @NotNull
        TipoGasto tipo,

        @NotNull
        LocalDate fecha,

        @Size(max = 500)
        String comprobante

) {}
