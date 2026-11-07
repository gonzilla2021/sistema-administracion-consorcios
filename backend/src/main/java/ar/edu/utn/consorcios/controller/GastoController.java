
package ar.edu.utn.consorcios.controller;

import ar.edu.utn.consorcios.dto.*;
import ar.edu.utn.consorcios.service.GastoService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/consorcios/{consorcioId}/gastos")
@PreAuthorize(
        "hasAnyRole('SUPERADMINISTRADOR','ADMINISTRADOR_CONSORCIO')"
)
public class GastoController {

    private final GastoService service;

    public GastoController(GastoService service) {
        this.service = service;
    }

    // Registra un gasto.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GastoResponse crear(
            Authentication auth,
            @PathVariable UUID consorcioId,
            @Valid @RequestBody GastoRequest request) {

        return service.crear(auth, consorcioId, request);
    }

    // Consulta gastos.
    @GetMapping
    public List<GastoResponse> listar(
            Authentication auth,
            @PathVariable UUID consorcioId) {

        return service.listar(auth, consorcioId);
    }

    // Actualiza un gasto.
    @PutMapping("/{gastoId}")
    public GastoResponse actualizar(
            Authentication auth,
            @PathVariable UUID consorcioId,
            @PathVariable UUID gastoId,
            @Valid @RequestBody GastoRequest request) {

        return service.actualizar(
                auth, consorcioId, gastoId, request
        );
    }

    // Elimina un gasto.
    @DeleteMapping("/{gastoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
            Authentication auth,
            @PathVariable UUID consorcioId,
            @PathVariable UUID gastoId) {

        service.eliminar(auth, consorcioId, gastoId);
    }
}
