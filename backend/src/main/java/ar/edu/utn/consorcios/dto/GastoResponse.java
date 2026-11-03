
package ar.edu.utn.consorcios.dto;

import ar.edu.utn.consorcios.entity.TipoGasto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GastoResponse(

        UUID id,
        UUID consorcioId,
        String descripcion,
        BigDecimal monto,
        TipoGasto tipo,
        LocalDate fecha,
        String comprobante

) {}
