package ar.edu.utn.consorcios.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.consorcios.dto.ConsorcioRequest;
import ar.edu.utn.consorcios.dto.ConsorcioResponse;
import ar.edu.utn.consorcios.dto.UnidadResponse;
import ar.edu.utn.consorcios.dto.UnidadesRequest;
import ar.edu.utn.consorcios.service.ConsorcioService;
import ar.edu.utn.consorcios.service.UnidadFuncionalService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/consorcios")
public class ConsorcioController {

    private final ConsorcioService consorcioService;
    private final UnidadFuncionalService unidadService;

    public ConsorcioController(ConsorcioService consorcioService, UnidadFuncionalService unidadService) {
        this.consorcioService = consorcioService;
        this.unidadService = unidadService;
    }

    @GetMapping
    public List<ConsorcioResponse> listar(Authentication auth) {
        return consorcioService.listar(auth);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMINISTRADOR','ADMINISTRADOR_CONSORCIO')")
    public ConsorcioResponse obtener(Authentication auth, @PathVariable UUID id) {
        return consorcioService.obtener(auth, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
    public ConsorcioResponse crear(@Valid @RequestBody ConsorcioRequest request) {
        return consorcioService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMINISTRADOR','ADMINISTRADOR_CONSORCIO')")
    public ConsorcioResponse actualizar(Authentication auth, @PathVariable UUID id,
            @Valid @RequestBody ConsorcioRequest request) {
        return consorcioService.actualizar(auth, id, request);
    }

    @GetMapping("/{id}/unidades")
    @PreAuthorize("hasAnyRole('SUPERADMINISTRADOR','ADMINISTRADOR_CONSORCIO')")
    public List<UnidadResponse> unidades(Authentication auth, @PathVariable UUID id) {
        return unidadService.listar(auth, id);
    }

    /** Reemplaza el conjunto completo de unidades: la suma de coeficientes debe dar 100. */
    @PutMapping("/{id}/unidades")
    @PreAuthorize("hasAnyRole('SUPERADMINISTRADOR','ADMINISTRADOR_CONSORCIO')")
    public List<UnidadResponse> reemplazarUnidades(Authentication auth, @PathVariable UUID id,
            @Valid @RequestBody UnidadesRequest request) {
        return unidadService.reemplazar(auth, id, request);
    }
}
