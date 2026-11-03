
package ar.edu.utn.consorcios.controller;

import ar.edu.utn.consorcios.dto.PersonaRequest;
import ar.edu.utn.consorcios.dto.PersonaResponse;
import ar.edu.utn.consorcios.service.PersonaService;
import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// Indica que esta clase es un controlador REST.
@RestController

// Ruta principal para las solicitudes de personas.
@RequestMapping("/api/personas")
public class PersonaController {

    // Servicio que contiene la lógica de negocio.
    private final PersonaService personaService;

    // Inyección de dependencias mediante el constructor.
    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
    }

    // Atiende solicitudes POST /api/personas.
    @PostMapping

    // Devuelve HTTP 201 cuando se crea correctamente.
    @ResponseStatus(HttpStatus.CREATED)

    // Por ahora, solo el superadministrador puede crear.
    @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
    public PersonaResponse crear(
            @Valid @RequestBody PersonaRequest request) {

        // Delegamos la operación al servicio.
        return personaService.crear(request);
    }


    // GET /api/personas
    // Devuelve todas las personas registradas.
    @GetMapping
    @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
    public List<PersonaResponse> listar() {

        return personaService.listar();
    }


    // Consulta una persona por ID.
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
    public PersonaResponse obtener(@PathVariable UUID id) {
        return personaService.obtener(id);
    }

    // Actualiza una persona.
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMINISTRADOR')")
    public PersonaResponse actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody PersonaRequest request) {

        return personaService.actualizar(id, request);
    }


}
