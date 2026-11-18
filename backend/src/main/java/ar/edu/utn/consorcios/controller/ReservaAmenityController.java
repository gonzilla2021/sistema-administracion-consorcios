package ar.edu.utn.consorcios.controller;

import ar.edu.utn.consorcios.dto.ReservaAmenityRequest;
import ar.edu.utn.consorcios.dto.ReservaAmenityResponse;
import ar.edu.utn.consorcios.service.ReservaAmenityService;
import ar.edu.utn.consorcios.dto.DisponibilidadAmenityResponse;
import ar.edu.utn.consorcios.dto.AmenityResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/mi-cuenta")
@RequiredArgsConstructor
@PreAuthorize(
    "hasAnyRole('PROPIETARIO', 'INQUILINO')"
)
public class ReservaAmenityController {

    private final ReservaAmenityService reservaAmenityService;

    @Operation(
        summary = "Crear una reserva",
        description = """
            Reserva un turno disponible para la persona asociada
            a la cuenta autenticada.
            """
    )
    @PostMapping("/amenities/{amenityId}/reservas")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaAmenityResponse crear(
        @PathVariable UUID amenityId,
        @Valid @RequestBody ReservaAmenityRequest request,
        Authentication authentication
    ) {
        return reservaAmenityService.crear(
            amenityId,
            request,
            authentication
        );
    }

    @Operation(
        summary = "Consultar mis reservas",
        description = """
            Devuelve las reservas de la persona asociada
            a la cuenta autenticada.
            """
    )
    @GetMapping("/reservas")
    public List<ReservaAmenityResponse> listarPropias(
        Authentication authentication
    ) {
        return reservaAmenityService.listarPropias(
            authentication
        );
    }

    @Operation(
        summary = "Cancelar una reserva propia",
        description = """
            Cancela una reserva activa perteneciente a la persona
            autenticada. No permite cancelar reservas ajenas.
            """
    )
    @PatchMapping("/reservas/{reservaId}/cancelar")
    public ReservaAmenityResponse cancelarPropia(
        @PathVariable UUID reservaId,
        Authentication authentication
    ) {
        return reservaAmenityService.cancelarPropia(
            reservaId,
            authentication
        );
    }

    @Operation(
    summary = "Consultar disponibilidad de un amenity",
    description = """
        Devuelve los turnos activos dentro de un rango máximo
        de 31 días.

        Los estados posibles son DISPONIBLE, RESERVADO,
        RESERVA_PROPIA y BLOQUEADO.
        """
    )
    @GetMapping("/amenities/{amenityId}/disponibilidad")
    public List<DisponibilidadAmenityResponse> consultarDisponibilidad(
        @PathVariable UUID amenityId,

        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate desde,

        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate hasta,

        Authentication authentication
    ) {
        return reservaAmenityService.consultarDisponibilidad(
            amenityId,
            desde,
            hasta,
            authentication
        );
    }

    @Operation(
    summary = "Consultar amenities disponibles para mi cuenta",
    description = """
        Devuelve los amenities activos de los consorcios en los
        que la persona autenticada tiene una vinculación vigente.

        Una cuenta PROPIETARIO utiliza vinculaciones PROPIETARIO
        y una cuenta INQUILINO utiliza vinculaciones INQUILINO.
        """
    )
    @GetMapping("/amenities")
    public List<AmenityResponse> listarAmenitiesAccesibles(
        Authentication authentication
    ) {
        return reservaAmenityService.listarAmenitiesAccesibles(
            authentication
        );
    }
}