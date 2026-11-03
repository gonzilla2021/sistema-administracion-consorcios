
package ar.edu.utn.consorcios.dto;

import ar.edu.utn.consorcios.entity.EstadoPago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PagoResponse(

        UUID id,
        UUID liquidacionId,
        UUID unidadFuncionalId,
        String identificadorUnidad,
        BigDecimal importe,
        EstadoPago estadoPago,
        LocalDate fechaPago

) {}
