package ar.edu.utn.consorcios.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record DisponibilidadAmenityResponse(
    UUID turnoId,
    String turno,

    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", example = "18:00")
    LocalTime horaInicio,

    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", example = "23:59")
    LocalTime horaFin,

    LocalDate fecha,
    boolean disponible,
    String estado
) {
}