
package ar.edu.utn.consorcios.dto;

import ar.edu.utn.consorcios.entity.TipoVinculo;
import java.time.LocalDate;
import java.util.UUID;

// Información que devolvemos al frontend.
public record VinculacionResponse(

        UUID id,
        UUID unidadFuncionalId,
        UUID personaId,
        String nombre,
        String apellido,
        TipoVinculo tipo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        boolean vigente

) {}
