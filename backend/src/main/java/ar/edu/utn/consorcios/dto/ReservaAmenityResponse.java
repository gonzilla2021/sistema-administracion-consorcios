package ar.edu.utn.consorcios.dto;

import ar.edu.utn.consorcios.entity.EstadoReservaAmenity;
import ar.edu.utn.consorcios.entity.ReservaAmenity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record ReservaAmenityResponse(
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

    UUID unidadFuncionalId,
    String unidad,
    LocalDate fecha,
    EstadoReservaAmenity estado,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaCancelacion
) {

    public static ReservaAmenityResponse desde(
        ReservaAmenity reserva
    ) {
        return new ReservaAmenityResponse(
            reserva.getId(),
            reserva.getAmenity().getId(),
            reserva.getAmenity().getNombre(),
            reserva.getTurno().getId(),
            reserva.getTurno().getNombre(),
            reserva.getTurno().getHoraInicio(),
            reserva.getTurno().getHoraFin(),
            reserva.getUnidadFuncional().getId(),
            reserva.getUnidadFuncional().getIdentificador(),
            reserva.getFecha(),
            reserva.getEstado(),
            reserva.getFechaCreacion(),
            reserva.getFechaCancelacion()
        );
    }
}