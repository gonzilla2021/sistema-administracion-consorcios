package ar.edu.utn.consorcios.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record BloqueoAmenityRequest(

    @NotNull(message = "El turno es obligatorio")
    UUID turnoId,

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(
        message = "La fecha no puede ser anterior a hoy"
    )
    LocalDate fecha,

    @NotBlank(message = "El motivo es obligatorio")
    @Size(
        max = 500,
        message = "El motivo no puede superar los 500 caracteres"
    )
    String motivo

) {
}