
package ar.edu.utn.consorcios.dto;

import ar.edu.utn.consorcios.entity.EstadoLiquidacion;
import ar.edu.utn.consorcios.entity.EstadoPago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MiLiquidacionResponse(
        UUID liquidacionId,
        UUID unidadFuncionalId,
        String unidad,
        String periodo,
        EstadoLiquidacion estadoLiquidacion,
        BigDecimal importe,
        EstadoPago estadoPago,
        LocalDate fechaPago
) {}
