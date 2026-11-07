package ar.edu.utn.consorcios.repository;

import ar.edu.utn.consorcios.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PersonaRepository extends JpaRepository<Persona, UUID> {

    // Personas de un consorcio, ordenadas por apellido y nombre.
    List<Persona> findByConsorcioIdOrderByApellidoAscNombreAsc(UUID consorcioId);

    // Comprueba si el DNI ya está registrado en el consorcio.
    boolean existsByConsorcioIdAndDni(UUID consorcioId, String dni);
}
