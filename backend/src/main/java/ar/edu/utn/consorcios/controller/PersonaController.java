package ar.edu.utn.consorcios.controller;

import ar.edu.utn.consorcios.dto.PersonaRequest;
import ar.edu.utn.consorcios.dto.PersonaResponse;
import ar.edu.utn.consorcios.service.PersonaService;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// Personas de un consorcio: las gestiona su administrador (o el superadministrador).
@RestController
@RequestMapping("/api/consorcios/{consorcioId}/personas")
@PreAuthorize(
        "hasAnyRole('SUPERADMINISTRADOR','ADMINISTRADOR_CONSORCIO')"
)
public class PersonaController {

    private final PersonaService personaService;

    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
    }

    // Registra una persona en el consorcio.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PersonaResponse crear(
            Authentication auth,
            @PathVariable UUID consorcioId,
            @Valid @RequestBody PersonaRequest request) {
        return personaService.crear(auth, consorcioId, request);
    }

    // Lista las personas del consorcio.
    @GetMapping
    public List<PersonaResponse> listar(
            Authentication auth,
            @PathVariable UUID consorcioId) {
        return personaService.listar(auth, consorcioId);
    }

    // Consulta una persona del consorcio.
    @GetMapping("/{id}")
    public PersonaResponse obtener(
            Authentication auth,
            @PathVariable UUID consorcioId,
            @PathVariable UUID id) {
        return personaService.obtener(auth, consorcioId, id);
    }

    // Actualiza una persona del consorcio.
    @PutMapping("/{id}")
    public PersonaResponse actualizar(
            Authentication auth,
            @PathVariable UUID consorcioId,
            @PathVariable UUID id,
            @Valid @RequestBody PersonaRequest request) {
        return personaService.actualizar(auth, consorcioId, id, request);
    }
}
