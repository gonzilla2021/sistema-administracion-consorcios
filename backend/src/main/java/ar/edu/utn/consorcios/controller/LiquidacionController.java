
package ar.edu.utn.consorcios.controller;

import ar.edu.utn.consorcios.dto.*;
import ar.edu.utn.consorcios.service.LiquidacionService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/consorcios/{consorcioId}/liquidaciones")
@PreAuthorize(
        "hasAnyRole('SUPERADMINISTRADOR','ADMINISTRADOR_CONSORCIO')"
)
public class LiquidacionController {

    private final LiquidacionService service;

    public LiquidacionController(LiquidacionService service) {
        this.service = service;
    }

    // Genera la liquidación mensual.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LiquidacionResponse generar(
            Authentication auth,
            @PathVariable UUID consorcioId,
            @Valid @RequestBody LiquidacionRequest request) {

        return service.generar(auth, consorcioId, request);
    }

    // Consulta importes por unidad.
    @GetMapping("/{liquidacionId}/unidades")
    public List<LiquidacionUnidadResponse> consultarDetalle(
            Authentication auth,
            @PathVariable UUID consorcioId,
            @PathVariable UUID liquidacionId) {

        return service.consultarDetalle(
                auth, consorcioId, liquidacionId
        );
    }

    // Cierra una liquidación.
    @PatchMapping("/{liquidacionId}/cerrar")
    public LiquidacionResponse cerrar(
            Authentication auth,
            @PathVariable UUID consorcioId,
            @PathVariable UUID liquidacionId) {

        return service.cerrar(
                auth, consorcioId, liquidacionId
        );
    }
}
