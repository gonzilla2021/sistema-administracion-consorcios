
package ar.edu.utn.consorcios.dto;

import ar.edu.utn.consorcios.entity.TipoVinculo;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

// Datos necesarios para vincular una persona con una unidad.
public record VinculacionRequest(

        @NotNull
        UUID personaId,

        @NotNull
        TipoVinculo tipo,

        @NotNull
        LocalDate fechaInicio,

        LocalDate fechaFin

) {}
