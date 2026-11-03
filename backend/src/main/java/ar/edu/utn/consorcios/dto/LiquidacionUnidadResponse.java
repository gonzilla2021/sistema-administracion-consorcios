
package ar.edu.utn.consorcios.dto;

import ar.edu.utn.consorcios.entity.EstadoPago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LiquidacionUnidadResponse(

        UUID id,
        UUID unidadFuncionalId,
        String identificador,
        BigDecimal importe,
        EstadoPago estadoPago,
        LocalDate fechaPago,
        boolean visibleParaInquilino

) {}
