package ar.edu.utn.consorcios.service;

import ar.edu.utn.consorcios.dto.PersonaRequest;
import ar.edu.utn.consorcios.dto.PersonaResponse;
import ar.edu.utn.consorcios.entity.Consorcio;
import ar.edu.utn.consorcios.entity.Persona;
import ar.edu.utn.consorcios.repository.PersonaRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PersonaService {

    private final PersonaRepository personas;
    private final ConsorcioService consorcioService;

    public PersonaService(
            PersonaRepository personas,
            ConsorcioService consorcioService) {
        this.personas = personas;
        this.consorcioService = consorcioService;
    }

    // Registra una persona dentro de un consorcio.
    @Transactional
    public PersonaResponse crear(
            Authentication auth,
            UUID consorcioId,
            PersonaRequest request) {

        Consorcio consorcio =
                consorcioService.exigirAcceso(auth, consorcioId);

        String dni = request.dni().trim();

        if (personas.existsByConsorcioIdAndDni(consorcioId, dni)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una persona con ese DNI en el consorcio"
            );
        }

        Persona persona = new Persona();
        persona.setConsorcio(consorcio);
        copiar(persona, request, dni);

        return convertir(personas.save(persona));
    }

    // Lista las personas del consorcio.
    @Transactional(readOnly = true)
    public List<PersonaResponse> listar(
            Authentication auth,
            UUID consorcioId) {

        consorcioService.exigirAcceso(auth, consorcioId);

        return personas
                .findByConsorcioIdOrderByApellidoAscNombreAsc(consorcioId)
                .stream()
                .map(this::convertir)
                .toList();
    }

    // Consulta una persona del consorcio.
    @Transactional(readOnly = true)
    public PersonaResponse obtener(
            Authentication auth,
            UUID consorcioId,
            UUID id) {

        return convertir(buscar(auth, consorcioId, id));
    }

    // Actualiza una persona del consorcio.
    @Transactional
    public PersonaResponse actualizar(
            Authentication auth,
            UUID consorcioId,
            UUID id,
            PersonaRequest request) {

        Persona persona = buscar(auth, consorcioId, id);

        String dni = request.dni().trim();

        if (!persona.getDni().equals(dni)
                && personas.existsByConsorcioIdAndDni(consorcioId, dni)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe otra persona con ese DNI en el consorcio"
            );
        }

        copiar(persona, request, dni);

        return convertir(personas.save(persona));
    }

    // Busca la persona y comprueba que sea del consorcio indicado.
    private Persona buscar(
            Authentication auth,
            UUID consorcioId,
            UUID id) {

        consorcioService.exigirAcceso(auth, consorcioId);

        Persona persona = personas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Persona inexistente"
                ));

        if (!persona.getConsorcio().getId().equals(consorcioId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Persona inexistente en este consorcio"
            );
        }

        return persona;
    }

    private void copiar(Persona persona, PersonaRequest request, String dni) {
        persona.setNombre(request.nombre().trim());
        persona.setApellido(request.apellido().trim());
        persona.setDni(dni);
        persona.setEmail(request.email());
        persona.setTelefono(request.telefono());
    }

    private PersonaResponse convertir(Persona persona) {
        return new PersonaResponse(
                persona.getId(),
                persona.getNombre(),
                persona.getApellido(),
                persona.getDni(),
                persona.getEmail(),
                persona.getTelefono()
        );
    }
}
