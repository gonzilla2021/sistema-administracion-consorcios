package ar.edu.utn.consorcios.dto;

import jakarta.validation.constraints.NotNull;

public record EstadoActivoRequest(

    @NotNull(message = "El estado activo es obligatorio")
    Boolean activo

) {
}