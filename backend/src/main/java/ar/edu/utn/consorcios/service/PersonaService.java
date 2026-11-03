package ar.edu.utn.consorcios.service;

import ar.edu.utn.consorcios.dto.PersonaRequest;
import ar.edu.utn.consorcios.dto.PersonaResponse;
import ar.edu.utn.consorcios.entity.Persona;
import ar.edu.utn.consorcios.repository.PersonaRepository;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PersonaService {

    // Repositorio para consultar y guardar personas.
    private final PersonaRepository personas;

    // Spring inyecta el repositorio automáticamente.
    public PersonaService(PersonaRepository personas) {
        this.personas = personas;
    }

    // Registra una nueva persona en la base de datos.
    @Transactional
    public PersonaResponse crear(PersonaRequest request) {

        // Quitamos espacios al principio y al final del DNI.
        String dni = request.dni().trim();

        // Comprobamos si el DNI ya está registrado.
        if (personas.existsByDni(dni)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una persona con ese DNI"
            );
        }

        // Creamos una nueva entidad Persona.
        Persona persona = new Persona();

        // Copiamos los datos recibidos desde el DTO.
        persona.setNombre(request.nombre().trim());
        persona.setApellido(request.apellido().trim());
        persona.setDni(dni);
        persona.setEmail(request.email());
        persona.setTelefono(request.telefono());

        // Guardamos la persona mediante JPA.
        Persona guardada = personas.save(persona);

        // Devolvemos los datos mediante el DTO de respuesta.
        return new PersonaResponse(
                guardada.getId(),
                guardada.getNombre(),
                guardada.getApellido(),
                guardada.getDni(),
                guardada.getEmail(),
                guardada.getTelefono()
        );
    }

    // Obtiene todas las personas registradas.
    // Es una operación de solo lectura.
    @Transactional(readOnly = true)
    public List<PersonaResponse> listar() {

        // Consultamos todas las personas mediante JPA.
        return personas.findAll()
                .stream()
                .map(persona -> new PersonaResponse(
                        persona.getId(),
                        persona.getNombre(),
                        persona.getApellido(),
                        persona.getDni(),
                        persona.getEmail(),
                        persona.getTelefono()
                ))
                .toList();
    }


    // Busca una persona por su identificador.
    @Transactional(readOnly = true)
    public PersonaResponse obtener(UUID id) {
        Persona persona = personas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Persona inexistente"
                ));

        return convertir(persona);
    }

    // Actualiza los datos de una persona existente.
    @Transactional
    public PersonaResponse actualizar(UUID id, PersonaRequest request) {
        Persona persona = personas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Persona inexistente"
                ));

        String dni = request.dni().trim();

        // Evita asignar el DNI de otra persona.
        if (!persona.getDni().equals(dni)
                && personas.existsByDni(dni)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe otra persona con ese DNI"
            );
        }

        persona.setNombre(request.nombre().trim());
        persona.setApellido(request.apellido().trim());
        persona.setDni(dni);
        persona.setEmail(request.email());
        persona.setTelefono(request.telefono());

        return convertir(personas.save(persona));
    }

    // Convierte una entidad JPA en un DTO de respuesta.
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
