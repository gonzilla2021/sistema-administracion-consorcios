package ar.edu.utn.consorcios.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record TurnoAmenityRequest(

    @NotBlank(message = "El nombre del turno es obligatorio")
    @Size(
        max = 60,
        message = "El nombre no puede superar los 60 caracteres"
    )
    String nombre,

    @NotNull(message = "La hora de inicio es obligatoria")
    @JsonFormat(pattern = "HH:mm")
    @Schema(
        type = "string",
        format = "time",
        example = "18:00"
    )
    LocalTime horaInicio,

    @NotNull(message = "La hora de finalización es obligatoria")
    @JsonFormat(pattern = "HH:mm")
    @Schema(
        type = "string",
        format = "time",
        example = "23:59"
    )
    LocalTime horaFin,

    boolean activo
) {
}