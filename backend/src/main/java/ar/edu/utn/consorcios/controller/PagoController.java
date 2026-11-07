
package ar.edu.utn.consorcios.controller;

import ar.edu.utn.consorcios.dto.*;
import ar.edu.utn.consorcios.service.PagoService;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/consorcios/{consorcioId}/pagos")
@PreAuthorize(
        "hasAnyRole('SUPERADMINISTRADOR','ADMINISTRADOR_CONSORCIO')"
)
public class PagoController {

    private final PagoService service;

    public PagoController(PagoService service) {
        this.service = service;
    }

    // Consulta los estados de pago.
    @GetMapping
    public List<PagoResponse> listar(
            Authentication auth,
            @PathVariable UUID consorcioId) {

        return service.listar(auth, consorcioId);
    }

    // Registra un pago.
    @PatchMapping("/{detalleId}/pagar")
    public PagoResponse registrar(
            Authentication auth,
            @PathVariable UUID consorcioId,
            @PathVariable UUID detalleId,
            @Valid @RequestBody PagoRequest request) {

        return service.registrar(
                auth, consorcioId, detalleId, request
        );
    }

    // Revierte un pago.
    @PatchMapping("/{detalleId}/revertir")
    public PagoResponse revertir(
            Authentication auth,
            @PathVariable UUID consorcioId,
            @PathVariable UUID detalleId) {

        return service.revertir(
                auth, consorcioId, detalleId
        );
    }
}

