
package ar.edu.utn.consorcios.dto;

import ar.edu.utn.consorcios.entity.EstadoLiquidacion;
import java.math.BigDecimal;
import java.util.UUID;

public record LiquidacionResponse(

        UUID id,
        UUID consorcioId,
        String periodo,
        EstadoLiquidacion estado,
        BigDecimal totalGastos,
        int cantidadUnidades

) {}
