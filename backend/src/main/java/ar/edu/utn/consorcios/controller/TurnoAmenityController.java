package ar.edu.utn.consorcios.controller;

import ar.edu.utn.consorcios.dto.EstadoActivoRequest;
import ar.edu.utn.consorcios.dto.TurnoAmenityRequest;
import ar.edu.utn.consorcios.dto.TurnoAmenityResponse;
import ar.edu.utn.consorcios.service.TurnoAmenityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
    "/api/consorcios/{consorcioId}/amenities/{amenityId}/turnos"
)
@RequiredArgsConstructor
@PreAuthorize(
    "hasAnyRole('SUPERADMINISTRADOR', 'ADMINISTRADOR_CONSORCIO')"
)
public class TurnoAmenityController {

    private final TurnoAmenityService turnoAmenityService;

    @GetMapping
    public List<TurnoAmenityResponse> listar(
        @PathVariable UUID consorcioId,
        @PathVariable UUID amenityId,
        Authentication authentication
    ) {
        return turnoAmenityService.listar(
            consorcioId,
            amenityId,
            authentication
        );
    }

    @GetMapping("/{turnoId}")
    public TurnoAmenityResponse obtener(
        @PathVariable UUID consorcioId,
        @PathVariable UUID amenityId,
        @PathVariable UUID turnoId,
        Authentication authentication
    ) {
        return turnoAmenityService.obtener(
            consorcioId,
            amenityId,
            turnoId,
            authentication
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TurnoAmenityResponse crear(
        @PathVariable UUID consorcioId,
        @PathVariable UUID amenityId,
        @Valid @RequestBody TurnoAmenityRequest request,
        Authentication authentication
    ) {
        return turnoAmenityService.crear(
            consorcioId,
            amenityId,
            request,
            authentication
        );
    }

    @PutMapping("/{turnoId}")
    public TurnoAmenityResponse actualizar(
        @PathVariable UUID consorcioId,
        @PathVariable UUID amenityId,
        @PathVariable UUID turnoId,
        @Valid @RequestBody TurnoAmenityRequest request,
        Authentication authentication
    ) {
        return turnoAmenityService.actualizar(
            consorcioId,
            amenityId,
            turnoId,
            request,
            authentication
        );
    }

    @PatchMapping("/{turnoId}/estado")
    public TurnoAmenityResponse cambiarEstado(
        @PathVariable UUID consorcioId,
        @PathVariable UUID amenityId,
        @PathVariable UUID turnoId,
        @Valid @RequestBody EstadoActivoRequest request,
        Authentication authentication
    ) {
        return turnoAmenityService.cambiarEstado(
            consorcioId,
            amenityId,
            turnoId,
            request.activo(),
            authentication
        );
    }
}