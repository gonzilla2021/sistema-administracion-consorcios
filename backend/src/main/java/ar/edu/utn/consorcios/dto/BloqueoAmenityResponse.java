package ar.edu.utn.consorcios.dto;

import ar.edu.utn.consorcios.entity.BloqueoAmenity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record BloqueoAmenityResponse(
    UUID id,
    UUID amenityId,
    String amenity,
    UUID turnoId,
    String turno,

    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", example = "18:00")
    LocalTime horaInicio,

    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", example = "23:59")
    LocalTime horaFin,

    LocalDate fecha,
    String motivo,
    LocalDateTime fechaCreacion,
    boolean activo
) {

    public static BloqueoAmenityResponse desde(
        BloqueoAmenity bloqueo
    ) {
        return new BloqueoAmenityResponse(
            bloqueo.getId(),
            bloqueo.getAmenity().getId(),
            bloqueo.getAmenity().getNombre(),
            bloqueo.getTurno().getId(),
            bloqueo.getTurno().getNombre(),
            bloqueo.getTurno().getHoraInicio(),
            bloqueo.getTurno().getHoraFin(),
            bloqueo.getFecha(),
            bloqueo.getMotivo(),
            bloqueo.getFechaCreacion(),
            bloqueo.isActivo()
        );
    }
}