package ar.edu.utn.consorcios.dto;

import ar.edu.utn.consorcios.entity.TurnoAmenity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.UUID;

public record TurnoAmenityResponse(
    UUID id,
    UUID amenityId,
    String nombre,

    @JsonFormat(pattern = "HH:mm")
    @Schema(
        type = "string",
        format = "time",
        example = "18:00"
    )
    LocalTime horaInicio,

    @JsonFormat(pattern = "HH:mm")
    @Schema(
        type = "string",
        format = "time",
        example = "23:59"
    )
    LocalTime horaFin,

    boolean activo
) {

    public static TurnoAmenityResponse desde(
        TurnoAmenity turno
    ) {
        return new TurnoAmenityResponse(
            turno.getId(),
            turno.getAmenity().getId(),
            turno.getNombre(),
            turno.getHoraInicio(),
            turno.getHoraFin(),
            turno.isActivo()
        );
    }
}