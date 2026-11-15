package ar.edu.utn.consorcios.controller;

import ar.edu.utn.consorcios.dto.ReservaAmenityRequest;
import ar.edu.utn.consorcios.dto.ReservaAmenityResponse;
import ar.edu.utn.consorcios.service.ReservaAmenityService;
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

import java.util.List;
import java.util.UUID;

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
}