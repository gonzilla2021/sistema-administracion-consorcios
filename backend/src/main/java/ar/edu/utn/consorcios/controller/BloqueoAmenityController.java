package ar.edu.utn.consorcios.controller;

import ar.edu.utn.consorcios.dto.BloqueoAmenityRequest;
import ar.edu.utn.consorcios.dto.BloqueoAmenityResponse;
import ar.edu.utn.consorcios.service.BloqueoAmenityService;
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
@RequestMapping(
    "/api/consorcios/{consorcioId}"
        + "/amenities/{amenityId}/bloqueos"
)
@RequiredArgsConstructor
@PreAuthorize(
    "hasAnyRole('SUPERADMINISTRADOR', "
        + "'ADMINISTRADOR_CONSORCIO')"
)
public class BloqueoAmenityController {

    private final BloqueoAmenityService bloqueoAmenityService;

    @Operation(
        summary = "Listar bloqueos del amenity"
    )
    @GetMapping
    public List<BloqueoAmenityResponse> listar(
        @PathVariable UUID consorcioId,
        @PathVariable UUID amenityId,
        Authentication authentication
    ) {
        return bloqueoAmenityService.listar(
            consorcioId,
            amenityId,
            authentication
        );
    }

    @Operation(
        summary = "Bloquear un turno disponible",
        description = """
            Impide reservas para una fecha y turno.
            No permite bloquear un turno con reserva activa.
            """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BloqueoAmenityResponse crear(
        @PathVariable UUID consorcioId,
        @PathVariable UUID amenityId,
        @Valid @RequestBody BloqueoAmenityRequest request,
        Authentication authentication
    ) {
        return bloqueoAmenityService.crear(
            consorcioId,
            amenityId,
            request,
            authentication
        );
    }

    @Operation(
        summary = "Desbloquear un turno"
    )
    @PatchMapping("/{bloqueoId}/desbloquear")
    public BloqueoAmenityResponse desbloquear(
        @PathVariable UUID consorcioId,
        @PathVariable UUID amenityId,
        @PathVariable UUID bloqueoId,
        Authentication authentication
    ) {
        return bloqueoAmenityService.desbloquear(
            consorcioId,
            amenityId,
            bloqueoId,
            authentication
        );
    }
}