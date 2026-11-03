
package ar.edu.utn.consorcios.controller;

import ar.edu.utn.consorcios.dto.*;
import ar.edu.utn.consorcios.service.VinculacionUnidadService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/consorcios/{consorcioId}/unidades/{unidadId}/vinculaciones"
)
@PreAuthorize(
        "hasAnyRole('SUPERADMINISTRADOR','ADMINISTRADOR_CONSORCIO')"
)
public class VinculacionUnidadController {

    private final VinculacionUnidadService service;

    public VinculacionUnidadController(
            VinculacionUnidadService service) {
        this.service = service;
    }

    // Registra una vinculación.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VinculacionResponse crear(
            Authentication auth,
            @PathVariable UUID consorcioId,
            @PathVariable UUID unidadId,
            @Valid @RequestBody VinculacionRequest request) {

        return service.crear(
                auth, consorcioId, unidadId, request
        );
    }

    // Consulta las vinculaciones de una unidad.
    @GetMapping
    public List<VinculacionResponse> listar(
            Authentication auth,
            @PathVariable UUID consorcioId,
            @PathVariable UUID unidadId) {

        return service.listar(auth, consorcioId, unidadId);
    }

    // Finaliza una vinculación.
    @PatchMapping("/{vinculacionId}/finalizar")
    public VinculacionResponse finalizar(
            Authentication auth,
            @PathVariable UUID consorcioId,
            @PathVariable UUID unidadId,
            @PathVariable UUID vinculacionId,
            @Valid @RequestBody FinalizarVinculacionRequest request) {

        return service.finalizar(
                auth,
                consorcioId,
                unidadId,
                vinculacionId,
                request.fechaFin()
        );
    }
}
