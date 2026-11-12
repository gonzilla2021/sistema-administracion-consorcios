package ar.edu.utn.consorcios.controller;

import ar.edu.utn.consorcios.dto.AmenityRequest;
import ar.edu.utn.consorcios.dto.AmenityResponse;
import ar.edu.utn.consorcios.dto.EstadoActivoRequest;
import ar.edu.utn.consorcios.service.AmenityService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/consorcios/{consorcioId}/amenities")
@RequiredArgsConstructor
@PreAuthorize(
    "hasAnyRole('SUPERADMINISTRADOR', 'ADMINISTRADOR_CONSORCIO')"
)
public class AmenityController {

    private final AmenityService amenityService;

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
}
