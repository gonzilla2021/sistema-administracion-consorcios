package ar.edu.utn.consorcios.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ReservaAmenityRequest(

    @NotNull(message = "El turno es obligatorio")
    UUID turnoId,

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(
        message = "La fecha no puede ser anterior a hoy"
    )
    LocalDate fecha

) {
}