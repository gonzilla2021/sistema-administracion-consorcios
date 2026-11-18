package ar.edu.utn.consorcios.controller;

import ar.edu.utn.consorcios.dto.AmenityRequest;
import ar.edu.utn.consorcios.dto.AmenityResponse;
import ar.edu.utn.consorcios.dto.EstadoActivoRequest;
import ar.edu.utn.consorcios.service.AmenityService;
import ar.edu.utn.consorcios.service.ReservaAmenityService;
import io.swagger.v3.oas.annotations.Operation;
import ar.edu.utn.consorcios.dto.ReservaAmenityResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
@RequestMapping("/api/consorcios/{consorcioId}/amenities")
@RequiredArgsConstructor
@PreAuthorize(
    "hasAnyRole('SUPERADMINISTRADOR', 'ADMINISTRADOR_CONSORCIO')"
)
public class AmenityController {

    private final AmenityService amenityService;
    private final ReservaAmenityService reservaAmenityService;

    @GetMapping
    public List<AmenityResponse> listar(
        @PathVariable UUID consorcioId,
        Authentication authentication
    ) {
        return amenityService.listar(
            consorcioId,
            authentication
        );
    }

    @GetMapping("/{amenityId}")
    public AmenityResponse obtener(
        @PathVariable UUID consorcioId,
        @PathVariable UUID amenityId,
        Authentication authentication
    ) {
        return amenityService.obtener(
            consorcioId,
            amenityId,
            authentication
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AmenityResponse crear(
        @PathVariable UUID consorcioId,
        @Valid @RequestBody AmenityRequest request,
        Authentication authentication
    ) {
        return amenityService.crear(
            consorcioId,
            request,
            authentication
        );
    }

    @PutMapping("/{amenityId}")
    public AmenityResponse actualizar(
        @PathVariable UUID consorcioId,
        @PathVariable UUID amenityId,
        @Valid @RequestBody AmenityRequest request,
        Authentication authentication
    ) {
        return amenityService.actualizar(
            consorcioId,
            amenityId,
            request,
            authentication
        );
    }

    @PatchMapping("/{amenityId}/estado")
    public AmenityResponse cambiarEstado(
        @PathVariable UUID consorcioId,
        @PathVariable UUID amenityId,
        @Valid @RequestBody EstadoActivoRequest request,
        Authentication authentication
    ) {
        return amenityService.cambiarEstado(
            consorcioId,
            amenityId,
            request.activo(),
            authentication
        );
    }

    @Operation(
    summary = "Consultar reservas de un amenity",
    description = """
        Devuelve las reservas activas y canceladas del amenity
        dentro de un rango máximo de 31 días.

        La operación es únicamente de consulta. El administrador
        no puede cancelar ni modificar reservas.
        """
    )
    @GetMapping("/{amenityId}/reservas")
    public List<ReservaAmenityResponse> listarReservas(
        @PathVariable UUID consorcioId,
        @PathVariable UUID amenityId,

        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate desde,

        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate hasta,

        Authentication authentication
    ) {
        return reservaAmenityService.listarAdministrativas(
            consorcioId,
            amenityId,
            desde,
            hasta,
            authentication
        );
    }
}
